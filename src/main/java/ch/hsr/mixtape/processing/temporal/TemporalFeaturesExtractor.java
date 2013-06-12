package ch.hsr.mixtape.processing.temporal;

import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.COMPLEX_DOMAIN;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.ENERGY;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.HIGH_FREQUENCY_CONTENT;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.KULLBACK_LIEBLER;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.MODIFIED_KULLBACK_LIEBLER;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.PHASE_FAST;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.SPECTRAL_DIFFERENCE;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.SPECTRAL_FLUX;
import static ch.hsr.mixtape.util.MathUtils.limit;
import static org.apache.commons.math3.util.FastMath.abs;

import java.util.ArrayList;
import java.util.Iterator;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import ch.hsr.mixtape.processing.FeatureExtractor;
import ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType;
import ch.hsr.mixtape.util.MathUtils;

import com.google.common.collect.Lists;

public class TemporalFeaturesExtractor implements
		FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

	private static final SpectralDescriptionType[] SPECTRAL_DESCRIPTION_TYPES = {
			ENERGY, SPECTRAL_DIFFERENCE, HIGH_FREQUENCY_CONTENT,
			COMPLEX_DOMAIN, PHASE_FAST, KULLBACK_LIEBLER,
			MODIFIED_KULLBACK_LIEBLER, SPECTRAL_FLUX };

	private static final int BPM_INDEX = 0;
	private static final int STANDARD_DEVIATION_INDEX = 1;
	private static final int RATIO_INDEX = 2;

	private static final double CLUSTER_STANDARD_DEVIATION_IN_BEATS = 5;

	private static final int CLUSTER_TEST_SET_SIZE_IN_PERCENT = 66;

	private static final double MIN_BPM = 20;
	private static final double MAX_BPM = 170;

	private static final int WINDOW_SIZE = 4096;
	private static final int HOP_SIZE = 512;
	private static final int WINDOW_OVERLAP = WINDOW_SIZE - HOP_SIZE;

	private static final int SAMPLE_RATE = 44100;

	private static final double SILENCE = -90.0;

	private PhaseVocoder phaseVocoder = new PhaseVocoder(WINDOW_SIZE,
			WINDOW_SIZE - WINDOW_OVERLAP);

	public TemporalFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		TemporalFeaturesOfWindow temporalFeaturesOfWindow = new TemporalFeaturesOfWindow();

		temporalFeaturesOfWindow.silent = detectSilence(windowOfSamples);
		temporalFeaturesOfWindow.fftgrain = phaseVocoder
				.computeSpectralFrame(windowOfSamples);

		return temporalFeaturesOfWindow;
	}

	private boolean detectSilence(double[] windowOfSamples) {
		double energy = 0.;
		for (int j = 0; j < windowOfSamples.length; j++)
			energy += MathUtils.square(windowOfSamples[j]);

		return 10.0 * Math.log10(energy / (double) windowOfSamples.length) >= SILENCE;
	}

	public TemporalFeaturesOfSong postprocess(
			Iterator<TemporalFeaturesOfWindow> featuresOfWindows) {
		Tempo[] tempos = initTempos();

		EM em = setupClusterer();
		Instances instances = setupClusterInstances();

		while (featuresOfWindows.hasNext()) {
			TemporalFeaturesOfWindow featureOfWindow = featuresOfWindows.next();

			for (int i = 0; i < tempos.length; i++) {
				Tempo tempo = tempos[i];
				tempo.extractTempo(featureOfWindow.silent,
						featureOfWindow.fftgrain);
				double bpm = tempo.getBPM();
				double confidence = tempo.getConfidence();
				Instance instance = setupClusterInstance(bpm, confidence);
				instances.add(instance);
				clusterInstance(em, instance);
			}
		}

		evaluate(em, getEvaluationInstances(instances));

		TemporalFeaturesOfSong featuresOfSong = new TemporalFeaturesOfSong();
		featuresOfSong.beats = getClusteredResults(em, instances);

		return featuresOfSong;
	}

	private Instances getEvaluationInstances(Instances instances) {
		int testSetSize = Math.round(instances.size()
				* CLUSTER_TEST_SET_SIZE_IN_PERCENT / 100);
		return new Instances(instances, 0, testSetSize);
	}

	private Tempo[] initTempos() {
		Tempo[] tempos = new Tempo[SPECTRAL_DESCRIPTION_TYPES.length];
		for (SpectralDescriptionType type : SPECTRAL_DESCRIPTION_TYPES)
			tempos[type.ordinal()] = new Tempo(type, WINDOW_SIZE, HOP_SIZE,
					SAMPLE_RATE);

		return tempos;
	}

	private EM setupClusterer() {
		EM em = new EM();

		em.setNumExecutionSlots(Runtime.getRuntime().availableProcessors() - 1);
		em.setMinStdDev(CLUSTER_STANDARD_DEVIATION_IN_BEATS);
		return em;
	}

	private Instances setupClusterInstances() {
		ArrayList<Attribute> attributes = Lists.newArrayList(new Attribute(
				"BPM"));
		return new Instances("TempoDataset", attributes, 0);
	}

	private Instance setupClusterInstance(double bpm, double confidence) {
		confidence = confidence != 0 ? 1 - (1 / confidence) : 0;
		return new DenseInstance(confidence, new double[] { bpm });
	}

	private void clusterInstance(EM em, Instance instance) {
		try {
			tryClusterInstance(em, instance);
		} catch (Exception e) {
			throw new RuntimeException("Error while clustering "
					+ "instance during tempo extraction.", e);
		}
	}

	private void tryClusterInstance(EM em, Instance instance) throws Exception {
		em.clusterInstance(instance);
	}

	private void evaluate(EM em, Instances instances) {
		try {
			tryEvaluate(em, instances);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error while evaluating cluster during tempo extraction.",
					e);
		}
	}

	private void tryEvaluate(EM em, Instances instances) throws Exception {
		ClusterEvaluation evaluation = new ClusterEvaluation();

		evaluation.setClusterer(em);
		evaluation.evaluateClusterer(new Instances(instances));
	}

	public double[][] getClusteredResults(EM em, Instances instances) {
		double[][][] clusters = em.getClusterModelsNumericAtts();
		double[][] results = new double[clusters.length][3];

		for (int i = 0; i < clusters.length; i++) {
			double[] cluster = clusters[i][0];
			double[] result = results[i];

			result[BPM_INDEX] = limit(cluster[BPM_INDEX], MIN_BPM, MAX_BPM);
			result[STANDARD_DEVIATION_INDEX] = cluster[STANDARD_DEVIATION_INDEX];
			result[RATIO_INDEX] = cluster[RATIO_INDEX] / instances.size();
		}

		return results;
	}

	public double distanceBetween(TemporalFeaturesOfSong x,
			TemporalFeaturesOfSong y) {
		return abs(bpm(x.beats) - bpm(y.beats)) / (MAX_BPM - MIN_BPM);
	}

	private double bpm(double[][] beats) {
		double bpm = 0;
		for (double[] beat : beats)
			bpm += beat[RATIO_INDEX] * beat[BPM_INDEX];

		return bpm;
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
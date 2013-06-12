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
import java.util.List;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import ch.hsr.mixtape.processing.FeatureExtractor;
import ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType;
import ch.hsr.mixtape.util.MathUtils;

import com.google.common.collect.Lists;

public class TemporalFeaturesExtractor implements FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

	private static final SpectralDescriptionType[] SPECTRAL_DESCRIPTION_TYPES = {
			ENERGY,
			SPECTRAL_DIFFERENCE,
			HIGH_FREQUENCY_CONTENT,
			COMPLEX_DOMAIN,
			PHASE_FAST,
			KULLBACK_LIEBLER,
			MODIFIED_KULLBACK_LIEBLER,
			SPECTRAL_FLUX
	};

	private static final int BPM_INDEX = 0;
	private static final int STANDARD_DEVIATION_INDEX = 1;
	private static final int RATIO_INDEX = 2;

	private static final double CLUSTER_STANDARD_DEVIATION_IN_BEATS = 5;

	private static final double MIN_BPM = 20;
	private static final double MAX_BPM = 170;

	private static final int WINDOW_SIZE = 4096;
	private static final int HOP_SIZE = 512;
	private static final int WINDOW_OVERLAP = WINDOW_SIZE - HOP_SIZE;

	private static final int SAMPLE_RATE = 44100;

	private static final double SILENCE = -90.0;

	private PhaseVocoder phaseVocoder = new PhaseVocoder(WINDOW_SIZE, WINDOW_SIZE - WINDOW_OVERLAP);

	public TemporalFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		TemporalFeaturesOfWindow temporalFeaturesOfWindow = new TemporalFeaturesOfWindow();

		temporalFeaturesOfWindow.silent = detectSilence(windowOfSamples);
		temporalFeaturesOfWindow.fftgrain = phaseVocoder.computeSpectralFrame(windowOfSamples);

		return temporalFeaturesOfWindow;
	}

	private boolean detectSilence(double[] windowOfSamples) {
		double energy = 0.;
		for (int j = 0; j < windowOfSamples.length; j++)
			energy += MathUtils.square(windowOfSamples[j]);

		return 10.0 * Math.log10(energy / (double) windowOfSamples.length) >= SILENCE;
	}

	public TemporalFeaturesOfSong postprocess(Iterator<TemporalFeaturesOfWindow> featuresOfWindows) {
		Tempo[] tempos = initTempos();

		List<Double[]> bpms = Lists.newArrayList();
		List<Double[]> confidences = Lists.newArrayList();

		while (featuresOfWindows.hasNext()) {
			TemporalFeaturesOfWindow featureOfWindow = featuresOfWindows.next();

			Double[] bpm = new Double[tempos.length];
			Double[] confidence = new Double[tempos.length];
			for (int i = 0; i < tempos.length; i++) {
				Tempo tempo = tempos[i];
				tempo.extractTempo(featureOfWindow.silent, featureOfWindow.fftgrain);

				bpm[i] = tempo.getBPM();
				confidence[i] = tempo.getConfidence();
			}

			bpms.add(bpm);
			confidences.add(confidence);
		}

		TemporalFeaturesOfSong featuresOfSong = new TemporalFeaturesOfSong();
		featuresOfSong.beats = getClusteredResults(bpms, confidences);

		return featuresOfSong;
	}

	private Tempo[] initTempos() {
		Tempo[] tempos = new Tempo[SPECTRAL_DESCRIPTION_TYPES.length];
		for (SpectralDescriptionType type : SPECTRAL_DESCRIPTION_TYPES)
			tempos[type.ordinal()] = new Tempo(type, WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE);

		return tempos;
	}

	public double[][] getClusteredResults(List<Double[]> bpms, List<Double[]> confidences) {
		try {
			return tryGetClusteredResults(bpms, confidences);
		} catch (Exception exception) {
			return new double[0][0];
		}
	}

	public double[][] tryGetClusteredResults(List<Double[]> bpms, List<Double[]> confidences) throws Exception {
		Instances instances = setupClusterInstances(bpms, confidences);

		double[][][] clusters = cluster(instances);
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

	private Instances setupClusterInstances(List<Double[]> bpms, List<Double[]> confidences) {
		ArrayList<Attribute> attributes = Lists.newArrayList(new Attribute("BPM"));
		Instances instances = new Instances("TempoDataset", attributes, bpms.size());

		double[] maxConfidences = getMaxConfidences(confidences);
		for (int i = 0; i < bpms.size(); i++) {
			for (int j = 0; j < SPECTRAL_DESCRIPTION_TYPES.length; j++) {
				double bpm = bpms.get(i)[j];
				double confidence = confidences.get(i)[j];

				if (bpm > 0 && confidence > 0)
					instances.add(new DenseInstance(confidence / maxConfidences[j], new double[] { bpm }));
			}
		}

		return instances;
	}

	private double[] getMaxConfidences(List<Double[]> confidences) {
		double[] maxConfidences = new double[SPECTRAL_DESCRIPTION_TYPES.length];
		for (Double[] confidence : confidences)
			for (int j = 0; j < confidence.length; j++)
				if (maxConfidences[j] < confidence[j])
					maxConfidences[j] = confidence[j];

		return maxConfidences;
	}

	private double[][][] cluster(Instances instances) throws Exception {
		EM em = new EM();

		em.setNumExecutionSlots(Runtime.getRuntime().availableProcessors() - 1);
		em.setMinStdDev(CLUSTER_STANDARD_DEVIATION_IN_BEATS);
		em.buildClusterer(new Instances(instances));

		evaluate(em, instances);

		return em.getClusterModelsNumericAtts();
	}

	private void evaluate(EM em, Instances instances) throws Exception {
		ClusterEvaluation evaluation = new ClusterEvaluation();

		evaluation.setClusterer(em);
		evaluation.evaluateClusterer(new Instances(instances));
	}

	public double distanceBetween(TemporalFeaturesOfSong x, TemporalFeaturesOfSong y) {
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
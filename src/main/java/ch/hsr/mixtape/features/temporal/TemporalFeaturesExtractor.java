package ch.hsr.mixtape.features.temporal;

import static ch.hsr.mixtape.MathUtils.limit;
import static org.apache.commons.math3.util.FastMath.abs;

import java.util.ArrayList;
import java.util.List;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import ch.hsr.mixtape.MathUtils;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.temporal.SpectralDescription.SpectralDescriptionType;

import com.google.common.collect.Lists;

public class TemporalFeaturesExtractor implements FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

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

	public TemporalFeaturesOfSong postprocess(List<TemporalFeaturesOfWindow> featuresOfWindows) {
		Tempo[] tempos = initTempos();

		double[][] bpms = new double[tempos.length][featuresOfWindows.size()];
		double[][] confidences = new double[tempos.length][featuresOfWindows.size()];

		for (int i = 0; i < tempos.length; i++) {
			Tempo tempo = tempos[i];
			for (int j = 0; j < featuresOfWindows.size(); j++) {
				TemporalFeaturesOfWindow featuresOfWindow = featuresOfWindows.get(j);
				tempo.extractTempo(featuresOfWindow.silent, featuresOfWindow.fftgrain);

				bpms[i][j] = tempo.getBPM();
				confidences[i][j] = tempo.getConfidence();
			}

		}

		TemporalFeaturesOfSong featuresOfSong = new TemporalFeaturesOfSong();
		featuresOfSong.beats = getClusteredResults(bpms, confidences);

		return featuresOfSong;
	}

	private Tempo[] initTempos() {
		SpectralDescriptionType[] types = SpectralDescriptionType.values();

		Tempo[] tempos = new Tempo[types.length];
		for (SpectralDescriptionType type : types)
			tempos[type.ordinal()] = new Tempo(type, WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE);

		return tempos;
	}

	public double[][] getClusteredResults(double[][] bpms, double[][] confidences) {
		try {
			return tryGetClusteredResults(bpms, confidences);
		} catch (Exception exception) {
			return new double[0][0];
		}
	}

	public double[][] tryGetClusteredResults(double[][] bpms, double[][] confidences) throws Exception {
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

	private Instances setupClusterInstances(double[][] bpms, double[][] confidences) {
		ArrayList<Attribute> attributes = Lists.newArrayList(new Attribute("BPM"));
		Instances instances = new Instances("TempoDataset", attributes, bpms.length);

		double[] maxConfidences = getMaxConfidences(confidences);
		for (int i = 0; i < bpms.length; i++) {
			for (int j = 0; j < bpms[i].length; i++) {
				double bpm = bpms[i][j];
				double confidence = confidences[i][j];

				if (bpm > 0 && confidence > 0)
					instances.add(new DenseInstance(confidence / maxConfidences[i], new double[] { bpm }));
			}
		}

		return instances;
	}

	private double[] getMaxConfidences(double[][] confidences) {
		double[] maxConfidences = new double[confidences.length];
		for (int i = 0; i < confidences.length; i++)
			for (int j = 0; j < confidences[i].length; j++)
				if (maxConfidences[i] < confidences[i][j])
					maxConfidences[i] = confidences[i][j];

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
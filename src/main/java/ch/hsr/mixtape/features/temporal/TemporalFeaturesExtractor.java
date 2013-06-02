package ch.hsr.mixtape.features.temporal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.temporal.SpectralDescription.SpectralDescriptionType;

// TODO: see TempoExtractionController!
public class TemporalFeaturesExtractor implements
		FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

	private static final int WINDOW_SIZE = 4096;
	private static final int HOP_SIZE = 512;
	private static final int SAMPLE_RATE = 44100;
	private static final int WINDOW_OVERLAP = 3584;
	private static final double CLUSTER_STANDARD_DEVIATION_IN_BEATS = 5;

	@Override
	public TemporalFeaturesOfWindow extractFrom(double[] windowOfSamples) {

		//TODO: hacky shit....
		ArrayList<double[]> samples = new ArrayList<double[]>();
		samples.add(windowOfSamples);

		TemporalFeaturesOfWindow temporalFeaturesOfWindow = new TemporalFeaturesOfWindow();

		try {
			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.COMPLEX_DOMAIN, samples,
							WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.ENERGY, samples,
							WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.HIGH_FREQUENCY_CONTENT,
							samples, WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.KULLBACK_LIEBLER, samples,
							WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.MODIFIED_KULLBACK_LIEBLER,
							samples, WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.PHASE_FAST, samples,
							WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.SPECTRAL_DIFFERENCE,
							samples, WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());

			temporalFeaturesOfWindow.extractedTempo
					.add(new TempoExtractionTask(
							SpectralDescriptionType.SPECTRAL_FLUX, samples,
							WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE).call());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return temporalFeaturesOfWindow;
	}

	@Override
	public TemporalFeaturesOfSong postprocess(
			List<TemporalFeaturesOfWindow> featuresOfWindows) {
		
		TemporalFeaturesOfSong temporalFeaturesOfSong = new TemporalFeaturesOfSong();
		
		//TODO: i dont get it....
		temporalFeaturesOfSong.bpms = new int[featuresOfWindows.size()];
		
		for (int i = 0; i < featuresOfWindows.size(); i++) {
			
			try {
				int[][] beats = getClusteredResults(featuresOfWindows.get(i));
				temporalFeaturesOfSong.bpms[i] = beats[0][0];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	private int[][] getClusteredResults(TemporalFeaturesOfWindow featuresOfWindow) throws Exception {
		
		Instances data = setupClusterInstances(featuresOfWindow.extractedTempo);
		if (data.isEmpty())
			return new int[0][0];

		// Clustering
		EM em = new EM();
		
		//TODO: erm jo....
		em.setNumExecutionSlots(Runtime.getRuntime().availableProcessors() - 1);
		em.setMinStdDev(CLUSTER_STANDARD_DEVIATION_IN_BEATS);
		em.buildClusterer(new Instances(data));

		// Evaluation
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(em);
		eval.evaluateClusterer(new Instances(data));

		// Fetching results
		double[][][] numericAttributes = em.getClusterModelsNumericAtts();
		int[][] beats = new int[numericAttributes.length][3];

		for (int i = 0; i < numericAttributes.length; i++) {
			// BPM
			beats[i][0] = (int) Math.round(numericAttributes[i][0][0]);
			// Standard Deviation in BPM
			beats[i][1] = (int) Math.round(numericAttributes[i][0][1]);
			// Percentage of Instances in Cluster
			beats[i][2] = (int) Math.round(numericAttributes[i][0][2] * 100
					/ data.size());
		}

		return beats;
	}


	private Instances setupClusterInstances(List<ExtractedTempo> extractedTempo) throws InterruptedException,
			ExecutionException {
		ArrayList<Double> beats = new ArrayList<Double>();
		ArrayList<Double> confidences = new ArrayList<Double>();
		for (ExtractedTempo t : extractedTempo) {
			beats.addAll(t.getRoundedBeatCollection(true));
			confidences.addAll(t.getNormalizedConfidences());
		}

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("BPM"));
		Instances data = new Instances("TempoDataset", attributes, beats.size());

		for (int i = 0; i < beats.size(); i++)
			data.add(new DenseInstance(confidences.get(i), new double[] { beats
					.get(i) }));
		// data.add(new DenseInstance(1.0, new double[] { beats.get(i) }));

		return data;
	}

	@Override
	public double distanceBetween(TemporalFeaturesOfSong x,
			TemporalFeaturesOfSong y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	@Override
	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}
}

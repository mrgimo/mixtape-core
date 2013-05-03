package ch.hsr.mixtape.features.aubio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import ch.hsr.mixtape.features.aubio.SpectralDescription.SpectralDescriptionType;

/**
 * Controller class for multi-threaded tempo extraction.
 * 
 * @author Stefan Derungs
 */
public class TempoExtractionController {

	private static final double CLUSTER_STANDARD_DEVIATION_IN_BEATS = 5;

	private ExecutorService threadPool;

	private LinkedList<Future<ExtractedTempo>> futures;

	private int windowSize = 4096;

	private int hopSize = 512;

	private int sampleRate;

	private ArrayList<double[]> windowedSamples;

	private ArrayList<ExtractedTempo> results;

	public TempoExtractionController(double[] samples, int windowSize,
			int hopSize, int sampleRate) {
		threadPool = Executors.newFixedThreadPool(getPoolSize());
		futures = new LinkedList<Future<ExtractedTempo>>();
		this.windowSize = windowSize;
		this.hopSize = hopSize;
		this.sampleRate = sampleRate;

		windowedSamples = new ArrayList<double[]>();
		int currentWindow = 0;
		while (currentWindow < samples.length) {
			windowedSamples.add(Arrays.copyOfRange(samples, currentWindow,
					currentWindow + windowSize));
			currentWindow += hopSize;
		}
	}

	/**
	 * This method calculates the needed threadpool size. If possible, always
	 * keep one processor free from threadpool allocation to ensure system
	 * responsiveness, i.e. if the number of tasks is greater than the number of
	 * available processors, all but one processor are allocated. Obvious
	 * exception is if there is only 1 processor in the system - then this is
	 * allocated.
	 */
	private int getPoolSize() {
		int numberOfTasks = SpectralDescriptionType.values().length;
		int procs = Runtime.getRuntime().availableProcessors();
		return numberOfTasks < procs ? numberOfTasks : procs > 1 ? procs - 1
				: 1;
	}

	public void start() {
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.COMPLEX_DOMAIN, windowedSamples,
				windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.ENERGY, windowedSamples, windowSize,
				hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.HIGH_FREQUENCY_CONTENT,
				windowedSamples, windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.KULLBACK_LIEBLER, windowedSamples,
				windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.MODIFIED_KULLBACK_LIEBLER,
				windowedSamples, windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.PHASE_FAST, windowedSamples,
				windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.SPECTRAL_DIFFERENCE, windowedSamples,
				windowSize, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.SPECTRAL_FLUX, windowedSamples,
				windowSize, hopSize, sampleRate)));
	}

	/**
	 * This method must be called at the end in order to shut down the
	 * application properly.
	 */
	public void shutdown() {
		threadPool.shutdown();
	}

	public ArrayList<ExtractedTempo> getAllResults()
			throws InterruptedException, ExecutionException {
		if (results == null) {
			results = new ArrayList<ExtractedTempo>();
			for (Future<ExtractedTempo> future : futures)
				results.add(future.get());
		}

		return results;
	}

	/**
	 * TODO: Pretty much of a nonsense...?!
	 * 
	 * @return Returns a single averaged Tempo for the song.
	 */
	public int getSingleResult() {
		try {
			ArrayList<ExtractedTempo> results = getAllResults();
			if (results.isEmpty())
				return 0;

			int[] array = new int[2 * results.size()];
			int currentResult = 0;
			for (int i = 0; i < 2 * results.size(); i += 2) {
				array[i] = (int) Math.round(results.get(currentResult)
						.getMedianBpm(true));
				array[i + 1] = (int) Math.round(results.get(currentResult++)
						.getMeanBpm(true));
			}
			Arrays.sort(array);

			return (int) Math
					.round(array.length % 2 == 0 ? (array[array.length / 2] + array[array.length / 2 - 1]) / 2.
							: array[(array.length - 1) / 2]);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @return Each array contains [0] the bpms, [1] the standard deviation and
	 *         [2] the percentage of that beats cluster over the song. If no
	 *         beats were extracted, null is returned.
	 * @throws Exception
	 */
	public int[][] getClusteredResults() throws Exception {
		Instances data = setupClusterInstances();
		if (data.isEmpty())
			return null;

		// Clustering
		EM em = new EM();
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

	private Instances setupClusterInstances() throws InterruptedException,
			ExecutionException {
		ArrayList<Double> beats = new ArrayList<Double>();
		ArrayList<Double> confidences = new ArrayList<Double>();
		for (ExtractedTempo t : getAllResults()) {
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
}

package ch.hsr.mixtape.features.aubio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.hsr.mixtape.features.aubio.SpectralDescription.SpectralDescriptionType;

/**
 * Controller class for multi-threaded tempo extraction.
 * 
 * @author Stefan Derungs
 */
public class TempoExtractionController {

	private ExecutorService threadPool;

	private LinkedList<Future<ExtractedTempo>> futures;

	private int hopSize;

	private int sampleRate;

	private ArrayList<double[]> windowedSamples;

	public TempoExtractionController(double[] samples, int windowSize,
			int hopSize, int sampleRate) {
		threadPool = Executors.newFixedThreadPool(getPoolSize());
		futures = new LinkedList<Future<ExtractedTempo>>();
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
				hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.ENERGY, windowedSamples, hopSize,
				sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.HIGH_FREQUENCY_CONTENT,
				windowedSamples, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.KULLBACK_LIEBLER, windowedSamples,
				hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.MODIFIED_KULLBACK_LIEBLER,
				windowedSamples, hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.PHASE_FAST, windowedSamples, hopSize,
				sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.SPECTRAL_DIFFERENCE, windowedSamples,
				hopSize, sampleRate)));
		futures.add(threadPool.submit(new TempoExtractionTask(
				SpectralDescriptionType.SPECTRAL_FLUX, windowedSamples,
				hopSize, sampleRate)));
	}

	/**
	 * This method must be called at the end in order to shut down the
	 * application properly.
	 */
	public void shutdown() {
		threadPool.shutdown();
	}

	public ArrayList<ExtractedTempo> getResults() throws InterruptedException,
			ExecutionException {
		ArrayList<ExtractedTempo> results = new ArrayList<ExtractedTempo>();
		for (Future<ExtractedTempo> future : futures)
			results.add(future.get());

		return results;
	}

	public int simpleTempoFeedback() {
		int tempo = 0;
		try {
			ArrayList<ExtractedTempo> results = getResults();

			int[] array = new int[2 * results.size()];
			int currentResult = 0;
			for (int i = 0; i < 2 * results.size(); i += 2) {
				array[i] = (int) Math.round(results.get(currentResult).getMedianBpm(true));
				array[i + 1] = (int) Math
						.round(results.get(currentResult++).getMeanBpm(true));
			}
			Arrays.sort(array);

			tempo = (int) Math
					.round(array.length % 2 == 0 ? (array[array.length / 2] + array[array.length / 2 - 1]) / 2.
							: array[(array.length - 1) / 2]);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return tempo;
	}
}

package ch.hsr.mixtape;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.collect.Lists;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private static final int ESTIMATED_NUMBER_OF_WINDOWS = 2048;

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private final List<List<Future<FeaturesOfWindow>>> featuresOfWindows;
	private final List<Future<FeaturesOfSong>> featuresOfSongs;

	private final int numberOfSongs;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor, int numberOfSongs) {
		this.featureExtractor = featureExtractor;
		this.featuresOfWindows = initExtractionTasks(numberOfSongs);
		this.featuresOfSongs = Lists.newArrayListWithCapacity(numberOfSongs);
		this.numberOfSongs = numberOfSongs;
	}

	private List<List<Future<FeaturesOfWindow>>> initExtractionTasks(int numberOfSongs) {
		List<List<Future<FeaturesOfWindow>>> extractionTasks = Lists.newArrayListWithCapacity(numberOfSongs);
		for (int i = 0; i < numberOfSongs; i++)
			extractionTasks.add(Lists
					.<Future<FeaturesOfWindow>> newArrayListWithExpectedSize(ESTIMATED_NUMBER_OF_WINDOWS));

		return extractionTasks;
	}

	public void process(Song song, double[] window) {
		featuresOfWindows.get(song.getId()).add(doExtract(window));
	}

	private Future<FeaturesOfWindow> doExtract(final double[] window) {
		return executor.submit(new Callable<FeaturesOfWindow>() {

			public FeaturesOfWindow call() throws Exception {
				return featureExtractor.extractFrom(window);
			}

		});
	}

	public int getWindowSize() {
		return featureExtractor.getWindowSize();
	}

	public int getWindowOverlap() {
		return featureExtractor.getWindowOverlap();
	}

	public void postprocess() {
		for (List<Future<FeaturesOfWindow>> featuresOfWindow : featuresOfWindows)
			featuresOfSongs.add(postprocess(featuresOfWindow));
	}

	private Future<FeaturesOfSong> postprocess(final List<Future<FeaturesOfWindow>> futures) {
		return executor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				List<FeaturesOfWindow> featuresOfWindows = Lists.newArrayListWithCapacity(futures.size());
				for (Future<FeaturesOfWindow> future : futures)
					try {
						featuresOfWindows.add(future.get());
					} catch (InterruptedException | ExecutionException exception) {
						continue;
					}

				return featureExtractor.postprocess(featuresOfWindows);
			}

		});
	}

	public List<List<Future<Double>>> getDistances() {
		List<List<Future<Double>>> distanceMatrix = Lists.newArrayListWithCapacity(numberOfSongs);
		for (int x = 0; x < numberOfSongs; x++)
			distanceMatrix.add(getDistances(x));

		return distanceMatrix;
	}

	private List<Future<Double>> getDistances(int x) {
		List<Future<Double>> distances = Lists.newArrayListWithCapacity(x);
		for (int y = 0; y < x; y++)
			distances.add(distanceBetween(featuresOfSongs.get(x), featuresOfSongs.get(y)));

		return distances;
	}

	private Future<Double> distanceBetween(final Future<FeaturesOfSong> x, final Future<FeaturesOfSong> y) {
		return executor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				try {
					return featureExtractor.distanceBetween(x.get(), y.get());
				} catch (InterruptedException | ExecutionException exception) {
					return Double.NaN;
				}
			}

		});
	}

}
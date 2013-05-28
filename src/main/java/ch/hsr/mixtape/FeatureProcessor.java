package ch.hsr.mixtape;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.collect.Lists;

public class FeatureProcessor<T> {

	private static final int ESTIMATED_NUMBER_OF_WINDOWS = 2048;

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	private final FeatureExtractor<T> feature;
	private final List<List<Future<T>>> tasks;

	private final int numberOfSongs;

	public FeatureProcessor(FeatureExtractor<T> feature, int numberOfSongs) {
		this.feature = feature;
		this.tasks = initTasks(numberOfSongs);

		this.numberOfSongs = numberOfSongs;
	}

	private List<List<Future<T>>> initTasks(int numberOfSongs) {
		List<List<Future<T>>> tasks = Lists.newArrayListWithCapacity(numberOfSongs);
		for (int i = 0; i < numberOfSongs; i++)
			tasks.add(Lists.<Future<T>> newArrayListWithExpectedSize(ESTIMATED_NUMBER_OF_WINDOWS));

		return tasks;
	}

	public void process(Song song, double[] window) {
		tasks.get(song.getId()).add(doExtract(window));
	}

	private Future<T> doExtract(final double[] window) {
		return executor.submit(new Callable<T>() {

			public T call() throws Exception {
				return feature.extract(window);
			}

		});
	}

	public int getWindowSize() {
		return feature.getWindowSize();
	}

	public int getWindowOverlap() {
		return feature.getWindowOverlap();
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
			distances.add(distanceBetween(tasks.get(x), tasks.get(y)));

		return distances;
	}

	private Future<Double> distanceBetween(final List<Future<T>> x, final List<Future<T>> y) {
		return executor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return feature.getMetric().distanceBetween(x, y);
			}

		});
	}

}
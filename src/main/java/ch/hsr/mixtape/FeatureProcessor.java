package ch.hsr.mixtape;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.model.Song;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private Map<Song, EvaluatingIterator<FeaturesOfWindow>> featuresOfWindows = Maps.newHashMap();
	private Map<Song, ListenableFuture<FeaturesOfSong>> featuresOfSongs = Maps.newHashMap();

	private final ListeningExecutorService executor;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor,
			ListeningExecutorService executor) {
		this.featureExtractor = featureExtractor;
		this.executor = executor;
	}

	public void process(Song song, double[] windowOfSamples) {
		EvaluatingIterator<FeaturesOfWindow> iterator = featuresOfWindows.get(song);
		ListenableFuture<FeaturesOfWindow> future = doExtract(windowOfSamples);

		if (iterator != null)
			iterator.put(future);
		else
			initNewSong(song, future);
	}

	private ListenableFuture<FeaturesOfWindow> doExtract(final double[] window) {
		return executor.submit(new Callable<FeaturesOfWindow>() {

			public FeaturesOfWindow call() throws Exception {
				return featureExtractor.extractFrom(window);
			}

		});
	}

	private void initNewSong(Song song, ListenableFuture<FeaturesOfWindow> future) {
		EvaluatingIterator<FeaturesOfWindow> newIterator = new EvaluatingIterator<>(future);

		featuresOfWindows.put(song, newIterator);
		featuresOfSongs.put(song, postprocess(song, newIterator));
	}

	public void postprocess(final Song song) {
		featuresOfWindows.get(song).finish();
	}

	private ListenableFuture<FeaturesOfSong> postprocess(final Song song, final Iterator<FeaturesOfWindow> iterator) {
		return executor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				System.out.println("calling postprocess");
				FeaturesOfSong featuresOfSong = featureExtractor.postprocess(iterator);

				featuresOfWindows.remove(song);
				Runtime.getRuntime().gc();

				return featuresOfSong;
			}

		});
	}

	public ListenableFuture<Double> distanceBetween(final Song x, final Song y) {
		return executor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return featureExtractor.distanceBetween(featuresOfSongs.get(x).get(), featuresOfSongs.get(y).get());
			}

		});
	}

	public int getWindowSize() {
		return featureExtractor.getWindowSize();
	}

	public int getWindowOverlap() {
		return featureExtractor.getWindowOverlap();
	}

}
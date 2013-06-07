package ch.hsr.mixtape;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private Map<Song, EvaluatingIterator<FeaturesOfWindow>> featuresOfWindows = Maps.newHashMap();
	private Map<Song, ListenableFuture<FeaturesOfSong>> featuresOfSongs = Maps.newHashMap();

	private final ListeningExecutorService extractionExecutor;
	private final ListeningExecutorService postprocessingExecutor;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor,
			ListeningExecutorService extractionExecutor, ListeningExecutorService postprocessingExecutor) {
		this.featureExtractor = featureExtractor;
		this.extractionExecutor = extractionExecutor;
		this.postprocessingExecutor = postprocessingExecutor;
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
		return extractionExecutor.submit(new Callable<FeaturesOfWindow>() {

			public FeaturesOfWindow call() throws Exception {
				return featureExtractor.extractFrom(window);
			}

		});
	}

	private void initNewSong(Song song, ListenableFuture<FeaturesOfWindow> future) {
		EvaluatingIterator<FeaturesOfWindow> iterator = new EvaluatingIterator<>(future);

		featuresOfWindows.put(song, iterator);
		featuresOfSongs.put(song, postprocess(song, iterator));
	}

	private ListenableFuture<FeaturesOfSong> postprocess(final Song song, final Iterator<FeaturesOfWindow> iterator) {
		return postprocessingExecutor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				FeaturesOfSong featuresOfSong = featureExtractor.postprocess(iterator);
				featuresOfWindows.remove(song);

				return featuresOfSong;
			}

		});
	}

	public void postprocess(final Song song) {
		featuresOfWindows.get(song).finish();
	}

	public ListenableFuture<Double> distanceBetween(final Song x, final Song y) {
		return extractionExecutor.submit(new Callable<Double>() {

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
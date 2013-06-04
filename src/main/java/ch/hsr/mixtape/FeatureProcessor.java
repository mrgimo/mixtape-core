package ch.hsr.mixtape;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private ArrayListMultimap<Song, ListenableFuture<FeaturesOfWindow>> featuresOfWindows = ArrayListMultimap.create();
	private Map<Song, ListenableFuture<FeaturesOfSong>> featuresOfSongs = Maps.newHashMap();

	private ListeningExecutorService executor;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor,
			ListeningExecutorService executor) {
		this.featureExtractor = featureExtractor;
		this.executor = executor;
	}

	public void process(Song song, double[] windowOfSamples) {
		featuresOfWindows.put(song, doExtract(windowOfSamples));
	}

	private ListenableFuture<FeaturesOfWindow> doExtract(final double[] window) {
		return executor.submit(new Callable<FeaturesOfWindow>() {

			public FeaturesOfWindow call() throws Exception {
				return featureExtractor.extractFrom(window);
			}

		});
	}

	public void postprocess(final Song song) {
		List<ListenableFuture<FeaturesOfWindow>> futures = featuresOfWindows.get(song);
		featuresOfSongs.put(song, postprocess(futures));
	}

	private ListenableFuture<FeaturesOfSong> postprocess(final List<ListenableFuture<FeaturesOfWindow>> futures) {
		ListenableFuture<FeaturesOfSong> task = executor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				System.out.println("calling postprocess");
				FeaturesOfSong featuresOfSong = featureExtractor.postprocess(evaluate(futures));
				futures.clear();
		
				return featuresOfSong;
			}

		});

		return task;
	}

	private List<FeaturesOfWindow> evaluate(List<ListenableFuture<FeaturesOfWindow>> futures) {
		return Lists.transform(futures, new Function<ListenableFuture<FeaturesOfWindow>, FeaturesOfWindow>() {

			public FeaturesOfWindow apply(ListenableFuture<FeaturesOfWindow> future) {
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException exception) {
					throw new RuntimeException(exception);
				}
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
package ch.hsr.mixtape;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private Multimap<Song, Future<FeaturesOfWindow>> featuresOfWindows = ArrayListMultimap.create();
	private Map<Song, Future<FeaturesOfSong>> featuresOfSongs = Maps.newHashMap();

	private ExecutorService executor;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor,
			ExecutorService executor) {
		this.featureExtractor = featureExtractor;
		this.executor = executor;
	}

	public void process(Song song, double[] windowOfSamples) {
		featuresOfWindows.put(song, doExtract(windowOfSamples));
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

	public void postprocess(Song song) {
		Collection<Future<FeaturesOfWindow>> futures = Lists.newArrayList(featuresOfWindows.get(song));
		featuresOfWindows.removeAll(song);
		featuresOfSongs.put(song, postprocess(futures));
	}

	private Future<FeaturesOfSong> postprocess(final Collection<Future<FeaturesOfWindow>> futures) {
		return executor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				List<FeaturesOfWindow> featuresOfWindows = Lists.newArrayListWithCapacity(futures.size());
				for (Future<FeaturesOfWindow> future : futures)
					try {
						featuresOfWindows.add(future.get());
					} catch (InterruptedException | ExecutionException exception) {
						throw new RuntimeException(exception);
					}

				return featureExtractor.postprocess(featuresOfWindows);
			}

		});
	}

	public Table<Song, Song, Double> getDistances(Collection<Song> songs) {
		Table<Song, Song, Double> distances = HashBasedTable.create();
		for (Song songX : songs) {
			for (Song songY : songs) {
				Future<FeaturesOfSong> featuresOfSongX = featuresOfSongs.get(songX);
				Future<FeaturesOfSong> featuresOfSongY = featuresOfSongs.get(songY);

				distances.put(songX, songY, distanceBetween(featuresOfSongX, featuresOfSongY));
			}
		}

		featuresOfSongs = null;

		return distances;
	}

	private double distanceBetween(final Future<FeaturesOfSong> x, final Future<FeaturesOfSong> y) {
		try {
			return featureExtractor.distanceBetween(x.get(), y.get());
		} catch (InterruptedException | ExecutionException exception) {
			throw new RuntimeException(exception);
		}
	}

}
package ch.hsr.mixtape;

import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.Futures.dereference;
import static com.google.common.util.concurrent.Futures.immediateFuture;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.FeaturesOfSong;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.Song;
import ch.hsr.mixtape.processing.SampleWindowPublisher;
import ch.hsr.mixtape.processing.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.processing.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.processing.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.processing.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.processing.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.processing.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.processing.temporal.TemporalFeaturesExtractor;
import ch.hsr.mixtape.processing.temporal.TemporalFeaturesOfSong;

import com.google.common.collect.HashBasedTable;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class Mixtape {

	private static final int NUMBER_OF_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final int NUMBER_OF_FEATURE_EXTRACTORS = 4;

	private final ListeningExecutorService publishingExecutor = exitingFixedExecutorService(1, "publishing");
	private final ListeningExecutorService addingExecutor = exitingFixedExecutorService(1, "adding");

	private final ListeningExecutorService distanceExecutor = exitingFixedExecutorService(
			NUMBER_OF_AVAILABLE_PROCESSORS, "distance");
	private final ListeningExecutorService distanceAwaitingExecutor = exitingFixedExecutorService(NUMBER_OF_AVAILABLE_PROCESSORS,
			"distance-awaiting");

	private final ListeningExecutorService extractionExecutor = exitingFixedExecutorServiceWithBlockingTaskQueue(
			NUMBER_OF_AVAILABLE_PROCESSORS, "extraction");
	private final ListeningExecutorService postprocessingExecutor = exitingFixedExecutorService(
			NUMBER_OF_FEATURE_EXTRACTORS, "postprocessing");
	private final ListeningExecutorService extractionAwaitingExecutor = exitingFixedExecutorService(1,
			"extraction-awating");

	private final HarmonicFeaturesExtractor harmonicFeatureExtractor = new HarmonicFeaturesExtractor();
	private final PerceptualFeaturesExtractor perceptualFeatureExtractor = new PerceptualFeaturesExtractor();
	private final SpectralFeaturesExtractor spectralFeatureExtractor = new SpectralFeaturesExtractor();
	private final TemporalFeaturesExtractor temporalFeatureExtractor = new TemporalFeaturesExtractor();

	private HashBasedTable<Song, Song, Distance> distanceTable = HashBasedTable.create();
	private MixStrategy mixStrategy = new SmoothMix(this);

	public Mixtape(Collection<Distance> distances) {
		addDistances(distances);
	}

	private synchronized void addDistances(Collection<Distance> distances) {
		for (Distance distance : distances)
			addDistance(distance);
	}

	private synchronized void addDistance(Distance distance) {
		Song songX = distance.getSongX();
		Song songY = distance.getSongY();

		if (!songX.equals(songY)) {
			distanceTable.put(songX, songY, distance);
			distanceTable.put(songY, songX, distance);
		} else {
			distanceTable.put(songX, songX, distance);
		}
	}

	public void addSongs(Collection<Song> songs, DistanceCallback callback) throws IOException, InterruptedException,
			ExecutionException {
		List<ListenableFuture<Song>> otherSongs = asFutures(getSongs());
		for (Song song : songs) {
			ListenableFuture<Song> newSong = extractFeaturesOf(song);
			List<ListenableFuture<Distance>> distances = calcDistancesBetween(newSong, otherSongs);

			addSong(newSong, distances, callback);
			otherSongs.add(newSong);
		}
	}

	private void addSong(final ListenableFuture<Song> song, final List<ListenableFuture<Distance>> distances,
			final DistanceCallback callback) {
		addingExecutor.submit(new Runnable() {

			public void run() {
				try {
					Song newSong = song.get();
					List<Distance> newDistances = Futures.allAsList(distances).get();

					addDistances(newDistances);
					newSong.setAnalyzeDate(new Date());

					callback.distanceAdded(newSong, newDistances);
				} catch (InterruptedException | ExecutionException exception) {
					throw new RuntimeException(exception);
				}
			}

		});
	}

	private List<ListenableFuture<Song>> asFutures(List<Song> songs) {
		List<ListenableFuture<Song>> futures = newArrayListWithCapacity(songs.size());
		for (Song song : songs)
			futures.add(immediateFuture(song));

		return futures;
	}

	private ListenableFuture<Song> extractFeaturesOf(final Song song) throws IOException, InterruptedException,
			ExecutionException {
		return dereference(publishingExecutor.submit(new Callable<ListenableFuture<Song>>() {

			public ListenableFuture<Song> call() throws Exception {
				song.setAnalyzeStartDate();

				SampleWindowPublisher publisher = new SampleWindowPublisher(extractionExecutor, postprocessingExecutor);

				final ListenableFuture<HarmonicFeaturesOfSong> harmonic = publisher.register(harmonicFeatureExtractor);
				final ListenableFuture<PerceptualFeaturesOfSong> perceptual = publisher
						.register(perceptualFeatureExtractor);
				final ListenableFuture<SpectralFeaturesOfSong> spectral = publisher.register(spectralFeatureExtractor);
				final ListenableFuture<TemporalFeaturesOfSong> temporal = publisher.register(temporalFeatureExtractor);

				publisher.publish(song);

				return extractionAwaitingExecutor.submit(new Callable<Song>() {

					public Song call() throws Exception {
						song.setFeatures(new FeaturesOfSong(
								harmonic.get(),
								perceptual.get(),
								spectral.get(),
								temporal.get()));

						return song;
					}

				});
			}

		}));
	}

	private List<ListenableFuture<Distance>> calcDistancesBetween(ListenableFuture<Song> song,
			List<ListenableFuture<Song>> otherSongs) {
		List<ListenableFuture<Distance>> distances = newArrayListWithCapacity(otherSongs.size());
		for (ListenableFuture<Song> otherSong : otherSongs)
			distances.add(calcDistanceBetween(song, otherSong));

		return distances;
	}

	private ListenableFuture<Distance> calcDistanceBetween(final ListenableFuture<Song> songX,
			final ListenableFuture<Song> songY) {
		return distanceAwaitingExecutor.submit(new Callable<Distance>() {

			public Distance call() throws Exception {
				return calcDistanceBetween(songX.get(), songY.get());
			}

		});
	}

	private Distance calcDistanceBetween(Song songX, Song songY) throws InterruptedException, ExecutionException {
		if (songX.equals(songY))
			return Distance.toItself(songX);

		FeaturesOfSong x = songX.getFeatures();
		FeaturesOfSong y = songY.getFeatures();

		ListenableFuture<Double> harmonicDistance = harmonicDistance(x, y);
		ListenableFuture<Double> perceptualDistance = perceptualDistance(x, y);
		ListenableFuture<Double> spectralDistance = spectralDistance(x, y);
		ListenableFuture<Double> temporalDistance = temporalDistance(x, y);

		return new Distance(songX, songY, harmonicDistance.get(), perceptualDistance.get(), spectralDistance.get(),
				temporalDistance.get());
	}

	private ListenableFuture<Double> harmonicDistance(final FeaturesOfSong x, final FeaturesOfSong y) {
		return distanceExecutor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return harmonicFeatureExtractor.distanceBetween(x.harmonic, y.harmonic);
			}

		});
	}

	private ListenableFuture<Double> perceptualDistance(final FeaturesOfSong x, final FeaturesOfSong y) {
		return distanceExecutor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return perceptualFeatureExtractor.distanceBetween(x.perceptual, y.perceptual);
			}

		});
	}

	private ListenableFuture<Double> spectralDistance(final FeaturesOfSong x, final FeaturesOfSong y) {
		return distanceExecutor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return spectralFeatureExtractor.distanceBetween(x.spectral, y.spectral);
			}

		});
	}

	private ListenableFuture<Double> temporalDistance(final FeaturesOfSong x, final FeaturesOfSong y) {
		return distanceExecutor.submit(new Callable<Double>() {

			public Double call() throws Exception {
				return temporalFeatureExtractor.distanceBetween(x.temporal, y.temporal);
			}
		});
	}

	public Distance distanceBetween(Song songX, Song songY) {
		return distanceTable.get(songX, songY);
	}

	public Map<Song, Distance> distancesTo(Song song) {
		return newHashMap(distanceTable.row(song));
	}

	public synchronized List<Song> getSongs() {
		return newArrayList(distanceTable.rowKeySet());
	}

	public List<Distance> getDistances() {
		return newArrayList(distanceTable.values());
	}

	public void initialMix(Playlist playList) throws InvalidPlaylistException {
		mixStrategy.initialMix(playList);
	}

	public void mixMultipleSongs(Playlist playlist, List<Song> addedSongs) throws InvalidPlaylistException {
		mixStrategy.mixMultipleSongs(playlist, addedSongs);
	}

	public void mixAnotherSong(Playlist playlist, Song addedSong) throws InvalidPlaylistException {
		mixStrategy.mixAnotherSong(playlist, addedSong);

	}

}
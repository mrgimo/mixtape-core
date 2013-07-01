package ch.hsr.mixtape;

import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.Futures.immediateFuture;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(Mixtape.class);

	private static final int NUMBER_OF_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final int NUMBER_OF_FEATURE_EXTRACTORS = 4;

	private final ListeningExecutorService publishingExecutor = exitingFixedExecutorService(1, "publishing");
	private final ListeningExecutorService addingExecutor = exitingFixedExecutorService(1, "adding");

	private final ListeningExecutorService distanceExecutor = exitingFixedExecutorService(
			NUMBER_OF_AVAILABLE_PROCESSORS, "distance");
	private final ListeningExecutorService distanceAwaitingExecutor = exitingFixedExecutorService(
			NUMBER_OF_AVAILABLE_PROCESSORS,
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

		LOGGER.info("Adding distance between '" + songX.getTitle() + "' and '" + songY.getTitle() + "'.");

		if (!songX.equals(songY)) {
			distanceTable.put(songX, songY, distance);
			distanceTable.put(songY, songX, distance);
		} else {
			distanceTable.put(songX, songX, distance);
		}
	}

	public void addSongs(Collection<Song> songs, DistanceCallback callback) throws IOException, InterruptedException,
			ExecutionException {
		LOGGER.info(songs.size() + " added to Mixtape.");

		List<ListenableFuture<Song>> otherSongs = asFutures(getSongs());
		for (Song song : songs) {
			LOGGER.info("Preparing processing for '" + song.getTitle() + "'.");
			otherSongs.add(extractFeaturesOf(song, newArrayList(otherSongs), callback));
		}
	}

	private void addSong(final Song song, final List<Distance> distances,
			final DistanceCallback callback) {
		//addingExecutor.submit(new Runnable() {

		//public void run() {
				LOGGER.info("Adding new song '" + song.getTitle() + "'.");
				addDistances(distances);
				song.setAnalyzeDate(new Date());

				LOGGER.info("Song '" + song.getTitle() + "' added.");
				callback.distanceAdded(song, distances);
				//}

				//		});
	}

	private List<ListenableFuture<Song>> asFutures(List<Song> songs) {
		List<ListenableFuture<Song>> futures = newArrayListWithCapacity(songs.size());
		for (Song song : songs)
			futures.add(immediateFuture(song));

		return futures;
	}

	private ListenableFuture<Song> extractFeaturesOf(final Song song, final List<ListenableFuture<Song>> otherSongs,
			final DistanceCallback callback) throws IOException, InterruptedException,
			ExecutionException {
		return publishingExecutor.submit(new Callable<Song>() {

			public Song call() throws Exception {
				song.setAnalyzeStartDate();

				LOGGER.info("Started extraction for '" + song.getTitle() + "'.");
				final long extractionStarted = System.currentTimeMillis();

				SampleWindowPublisher publisher = new SampleWindowPublisher(extractionExecutor, postprocessingExecutor);

				final ListenableFuture<HarmonicFeaturesOfSong> harmonic = publisher.register(harmonicFeatureExtractor);
				final ListenableFuture<PerceptualFeaturesOfSong> perceptual = publisher
						.register(perceptualFeatureExtractor);
				final ListenableFuture<SpectralFeaturesOfSong> spectral = publisher.register(spectralFeatureExtractor);
				final ListenableFuture<TemporalFeaturesOfSong> temporal = publisher.register(temporalFeatureExtractor);

				publisher.publish(song);

				song.setFeatures(new FeaturesOfSong(
						harmonic.get(),
						perceptual.get(),
						spectral.get(),
						temporal.get()));

				final long extractionFinished = System.currentTimeMillis();
				LOGGER.info("Finished extraction for '" + song.getTitle() + "'. Extraction took "
						+ (extractionFinished - extractionStarted) / 1000 + " seconds.");

				List<ListenableFuture<Distance>> distances = calcDistancesBetween(song, otherSongs);
				addSong(song, Futures.allAsList(distances).get(), callback);

				return song;
			}

		});
	}

	private List<ListenableFuture<Distance>> calcDistancesBetween(Song song,
			List<ListenableFuture<Song>> otherSongs) {
		List<ListenableFuture<Distance>> distances = newArrayListWithCapacity(otherSongs.size());
		for (ListenableFuture<Song> otherSong : otherSongs)
			distances.add(calcDistanceBetween(song, otherSong));

		return distances;
	}

	private ListenableFuture<Distance> calcDistanceBetween(final Song songX,
			final ListenableFuture<Song> songY) {
		return distanceAwaitingExecutor.submit(new Callable<Distance>() {

			public Distance call() throws Exception {
				Song x = songX;
				Song y = songY.get();
				LOGGER.info("Calculating distance between '" + x.getTitle() + "' and '" + y.getTitle() + "'.");
				Distance distance = calcDistanceBetween(x, y);
				LOGGER.info("Distance between '" + x.getTitle() + "' and '" + y.getTitle() + "' calculated.");
				return distance;
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
		LOGGER.info("Returning distance between '" + songX.getTitle() + "' and '" + songY.getTitle() + "'.");
		return distanceTable.get(songX, songY);
	}

	public Map<Song, Distance> distancesTo(Song song) {
		LOGGER.info("Returning distances to '" + song.getTitle() + "'.");
		return newHashMap(distanceTable.row(song));
	}

	public synchronized List<Song> getSongs() {
		LOGGER.info("Returning all songs.");
		return newArrayList(distanceTable.rowKeySet());
	}

	public List<Distance> getDistances() {
		LOGGER.info("Returning all distances.");
		return newArrayList(distanceTable.values());
	}

	public void initialMix(Playlist playList) throws InvalidPlaylistException {
		LOGGER.info("Returning initial mix.");
		mixStrategy.initialMix(playList);
	}

	public void mixMultipleSongs(Playlist playlist, List<Song> addedSongs) throws InvalidPlaylistException {
		LOGGER.info("Returning mix of multiple songs.");
		mixStrategy.mixMultipleSongs(playlist, addedSongs);
	}

	public void mixAnotherSong(Playlist playlist, Song addedSong) throws InvalidPlaylistException {
		LOGGER.info("Returning mix of another songs.");
		mixStrategy.mixAnotherSong(playlist, addedSong);

	}

}
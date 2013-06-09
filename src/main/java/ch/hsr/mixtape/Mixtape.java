package ch.hsr.mixtape;

import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class Mixtape {
	
	private static final int NUMBER_OF_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final int NUMBER_OF_FEATURE_EXTRACTORS = 4;

	private final ListeningExecutorService extractionExecutor = exitingFixedExecutorServiceWithBlockingTaskQueue(NUMBER_OF_AVAILABLE_PROCESSORS);
	private final ListeningExecutorService postprocessingExecutor = exitingFixedExecutorService(NUMBER_OF_FEATURE_EXTRACTORS);
	private final ListeningExecutorService distanceExecutor = exitingFixedExecutorService(NUMBER_OF_AVAILABLE_PROCESSORS);

	private final HarmonicFeaturesExtractor harmonicFeatureExtractor = new HarmonicFeaturesExtractor();
	private final PerceptualFeaturesExtractor perceptualFeatureExtractor = new PerceptualFeaturesExtractor();
	private final SpectralFeaturesExtractor spectralFeatureExtractor = new SpectralFeaturesExtractor();
	private final TemporalFeaturesExtractor temporalFeatureExtractor = new TemporalFeaturesExtractor();

	private HashBasedTable<Song, Song, Distance> distanceTable = HashBasedTable.create();
	private MixStrategy mixStrategy = new SmoothMix(this);
	
	public Mixtape() {
	}

	public Mixtape(Collection<Distance> distances) {
		addDistances(distances);
	}

	private void addDistances(Collection<Distance> distances) {
		for (Distance distance : distances)
			addDistance(distance);
	}

	private void addDistance(Distance distance) {
		Song songX = distance.getSongX();
		Song songY = distance.getSongY();

		if (!songX.equals(songY)) {
			distanceTable.put(songX, songY, distance);
			distanceTable.put(songY, songX, distance);
		} else {
			distanceTable.put(songX, songX, distance);
		}
	}

	public Collection<Distance> addSongs(Collection<Song> songs)
			throws IOException, InterruptedException, ExecutionException {
		Collection<Distance> addedDistances = Lists.newArrayList();
		for (Song song : songs)
			addedDistances.addAll(addSong(song));

		return addedDistances;
	}

	public Collection<Distance> addSong(Song song) throws IOException, InterruptedException, ExecutionException {
		FeaturesOfSong features = extractFeaturesOf(song);
		song.setFeatures(features);

		Collection<Distance> distances = calcDistancesTo(song);
		addDistances(distances);

		return Lists.newArrayList(distances);
	}

	private FeaturesOfSong extractFeaturesOf(Song song) throws IOException, InterruptedException, ExecutionException {
		SampleWindowPublisher publisher = new SampleWindowPublisher(extractionExecutor, postprocessingExecutor);

		ListenableFuture<HarmonicFeaturesOfSong> harmonic = publisher.register(harmonicFeatureExtractor);
		ListenableFuture<PerceptualFeaturesOfSong> perceptual = publisher.register(perceptualFeatureExtractor);
		ListenableFuture<SpectralFeaturesOfSong> spectral = publisher.register(spectralFeatureExtractor);
		ListenableFuture<TemporalFeaturesOfSong> temporal = publisher.register(temporalFeatureExtractor);

		publisher.publish(song);

		return new FeaturesOfSong(
				harmonic.get(),
				perceptual.get(),
				spectral.get(),
				temporal.get());
	}

	private Collection<Distance> calcDistancesTo(Song song) {
		Set<Song> currentSongs = distanceTable.rowKeySet();

		List<ListenableFuture<Distance>> distances = Lists.newArrayListWithCapacity(currentSongs.size() + 1);
		distances.add(calcDistanceBetween(song, song));

		for (Song currentSong : currentSongs)
			distances.add(calcDistanceBetween(song, currentSong));

		return evaluate(distances);
	}

	private List<Distance> evaluate(List<ListenableFuture<Distance>> distances) {
		return Lists.transform(distances,
				new Function<ListenableFuture<Distance>, Distance>() {

					public Distance apply(ListenableFuture<Distance> distance) {
						try {
							return distance.get();
						} catch (InterruptedException | ExecutionException exception) {
							throw new RuntimeException(exception);
						}
					}

				});
	}

	private ListenableFuture<Distance> calcDistanceBetween(final Song newSong, final Song oldSong) {
		return distanceExecutor.submit(new Callable<Distance>() {

			public Distance call() throws Exception {
				if (newSong.equals(oldSong))
					return distanceToItself(newSong);

				FeaturesOfSong x = newSong.getFeatures();
				FeaturesOfSong y = oldSong.getFeatures();

				return new Distance(newSong, oldSong,
						harmonicFeatureExtractor.distanceBetween(x.harmonic, y.harmonic),
						perceptualFeatureExtractor.distanceBetween(x.perceptual, y.perceptual),
						spectralFeatureExtractor.distanceBetween(x.spectral, y.spectral),
						temporalFeatureExtractor.distanceBetween(x.temporal, y.temporal));
			}

		});
	}

	public Distance distanceBetween(Song songX, Song songY) {
		return distanceTable.get(songX, songY);
	}

	public Map<Song, Distance> distances(Song song) {
		return distanceTable.row(song);
	}

	private Distance distanceToItself(Song song) {
		return new Distance(song, song, 0, 0, 0, 0);
	}

	public List<Song> getSongs() {
		return Lists.newArrayList(distanceTable.rowKeySet());
	}

	public List<Distance> getDistances() {
		return Lists.newArrayList(distanceTable.values());
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
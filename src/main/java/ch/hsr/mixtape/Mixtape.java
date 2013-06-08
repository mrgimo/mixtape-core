package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.vectorLength;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfWindow;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfWindow;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfWindow;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesExtractor;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfSong;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfWindow;
import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.FeaturesOfSong;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.PlaylistItem;
import ch.hsr.mixtape.model.Song;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class Mixtape {

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime()
			.availableProcessors();

	private final ListeningExecutorService extractionExecutor = exitingFixedExecutorServiceWithBlockingTaskQueue(AVAILABLE_PROCESSORS);
	private final ListeningExecutorService postprocessingExecutor = exitingFixedExecutorService(4);

	private final FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> harmonicFeatureExtractor = new HarmonicFeaturesExtractor();
	private final FeatureExtractor<PerceptualFeaturesOfWindow, PerceptualFeaturesOfSong> perceptualFeatureExtractor = new PerceptualFeaturesExtractor();
	private final FeatureExtractor<SpectralFeaturesOfWindow, SpectralFeaturesOfSong> spectralFeatureExtractor = new SpectralFeaturesExtractor();
	private final FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> temporalFeatureExtractor = new TemporalFeaturesExtractor();

	// private List<Distance> distances;
	// private List<Song> songs;

	private HashBasedTable<Song, Song, Distance> distanceTable = HashBasedTable
			.create();

	public Mixtape(List<Song> songs, List<Distance> distances) {
		// this.songs = songs;
		// this.distances = distances;
		updateDistanceTable(distances);
	}

	private void updateDistanceTable(List<Distance> distances) {
		for (Distance distance : distances) {
			distanceTable.put(distance.getSongX(), distance.getSongY(),
					distance);
			// TODO: maybe just put on of em into the table, if so addapt change
			// to all using methods !
			distanceTable.put(distance.getSongY(), distance.getSongX(),
					distance);
		}
	}

	public List<Distance> addSong(Song song) throws IOException,
			InterruptedException, ExecutionException {
		FeaturesOfSong features = extractFeatures(song);
		song.setFeatures(features);

		List<Distance> newDistances = calcDistances(song);
		// distances.addAll(newDistances);
		// songs.add(song);

		updateDistanceTable(newDistances);

		return newDistances;
	}

	private FeaturesOfSong extractFeatures(Song song) throws IOException,
			InterruptedException, ExecutionException {
		SamplePublisher publisher = new SamplePublisher(extractionExecutor,
				postprocessingExecutor);

		ListenableFuture<HarmonicFeaturesOfSong> harmonic = publisher
				.register(harmonicFeatureExtractor);
		ListenableFuture<PerceptualFeaturesOfSong> perceptual = publisher
				.register(perceptualFeatureExtractor);
		ListenableFuture<SpectralFeaturesOfSong> spectral = publisher
				.register(spectralFeatureExtractor);
		ListenableFuture<TemporalFeaturesOfSong> temporal = publisher
				.register(temporalFeatureExtractor);

		publisher.publish(song);

		return new FeaturesOfSong(harmonic.get(), perceptual.get(),
				spectral.get(), temporal.get());
	}

	private List<Distance> calcDistances(Song songX) {
		// TODO: changed to remove song list dependency
		Set<Song> songs = distanceTable.columnKeySet();
		List<Distance> distances = Lists.newArrayListWithCapacity(songs.size());
		for (Song songY : songs)
			distances.add(distanceBetween(songX, songY));

		return distances;
	}

	private Distance distanceBetween(Song songX, Song songY) {
		FeaturesOfSong x = songX.getFeatures();
		FeaturesOfSong y = songY.getFeatures();

		return new Distance(songX, songY,
				harmonicFeatureExtractor
						.distanceBetween(x.harmonic, y.harmonic),
				perceptualFeatureExtractor.distanceBetween(x.perceptual,
						y.perceptual),
				spectralFeatureExtractor
						.distanceBetween(x.spectral, y.spectral),
				temporalFeatureExtractor
						.distanceBetween(x.temporal, y.temporal));
	}

	// public double distanceBetween(long x, long y, double[] weighting) {
	// int temp_x = (int) x; // TODO: no more ints here buddy ;-)
	// int temp_y = (int) y; // TODO: no more ints here buddy ;-)
	// if (temp_x > temp_y)
	// return distance(distances[temp_x][temp_y], weighting);
	// else if (temp_x < temp_y)
	// return distance(distances[temp_y][temp_x], weighting);
	// else
	// return 0;
	// }

	public List<Song> getSongs() {

		// TODO: makes List<Song> songs obsolet?
		ArrayList<Song> songs = new ArrayList<Song>();
		songs.addAll(distanceTable.columnKeySet());

		return songs;
	}

	// MIXING !

	public void initialMix(Playlist playList) throws InvalidPlaylistException {
		List<Song> startSongs = playList.getSettings().getStartSongs();
		if (startSongs.size() > 1) {
			Song initialSong = playList.getSettings().getStartSongs().get(0);

			playList.addItem(new PlaylistItem(initialSong, null,
					Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
					Integer.MAX_VALUE, true));

			startSongs.remove(initialSong);

			mixMultipleSongs(playList, startSongs);

		} else {
			// TODO: how to handle this?
			System.out.println("at least 2 songs needed for initial mix");
		}

	}

	public void mixMultipleSongs(Playlist playlist, List<Song> addedSongs)
			throws InvalidPlaylistException {

		List<Song> availableSongs = new ArrayList<Song>(
				distanceTable.columnKeySet());

		availableSongs.removeAll(playlist.getSongsInPlaylist());
		availableSongs.removeAll(addedSongs);

		sortBySong(playlist.getLastItem().getCurrent(), addedSongs, playlist
				.getSettings().getFeatureWeighting());

		for (Song song : addedSongs)
			mix(playlist, song, availableSongs);

	}

	public void mixAnotherSong(Playlist playlist, Song addedSong)
			throws InvalidPlaylistException {
		ArrayList<Song> availableSongs = new ArrayList<Song>(
				distanceTable.columnKeySet());

		availableSongs.removeAll(playlist.getSongsInPlaylist());
		availableSongs.remove(addedSong);

		mix(playlist, addedSong, availableSongs);

	}

	private void mix(Playlist currentPlaylist, Song addedSong,
			List<Song> availableSongs) throws InvalidPlaylistException {

		stripNonCandidates(currentPlaylist, addedSong, availableSongs);

		Song firstSong = currentPlaylist.getLastItem().getCurrent();
		Song lastSong = firstSong;
		Song mostSuitableSong = firstSong;

		double[] featureWeighting = currentPlaylist.getSettings()
				.getFeatureWeighting();

		double currentDistanceToAddedSong = weightedVectorLength(
				distanceTable.get(firstSong, addedSong), featureWeighting);

		boolean closerSongExists = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = weightedVectorLength(
						distanceTable.get(song, addedSong), featureWeighting);
				double distanceToLastSong = weightedVectorLength(
						distanceTable.get(song, mostSuitableSong),
						featureWeighting);

				if (isMoreSuitable(distanceToAddedSong,
						currentDistanceToAddedSong, distanceToLastSong,
						currentDistanceToLastSong)) {

					mostSuitableSong = song;
					currentDistanceToAddedSong = distanceToAddedSong;
					currentDistanceToLastSong = distanceToLastSong;
				}

			}

			if (closerSongExists(currentDistanceToLastSong)) {
				// TODO: set similarity?

				currentPlaylist.addItem(new PlaylistItem(mostSuitableSong,
						lastSong, 0, 1, 2, 3, false));

				lastSong = mostSuitableSong;
				availableSongs.remove(lastSong);
				closerSongExists = true;

			} else
				closerSongExists = false;

		} while (closerSongExists);

		currentPlaylist.addItem(new PlaylistItem(addedSong, lastSong, 0, 0, 0,
				0, true));

	}

	private double weightedVectorLength(Distance distance,
			double[] featureWeighting) {

		return vectorLength(
				distance.getHarmonicDistance()
						/ FastMath
								.sqrt(HarmonicFeaturesOfSong.NUMBER_OF_HARMONIC_FEATURES),
				distance.getPerceptualDistance()
						/ FastMath
								.sqrt(PerceptualFeaturesOfSong.NUMBER_OF_PERCEPTUAL_FEATURES),
				distance.getSpectralDistance()
						/ FastMath
								.sqrt(SpectralFeaturesOfSong.NUMBER_OF_SPECTRAL_FEATURES),
				distance.getTemporalDistance()
						/ FastMath
								.sqrt(TemporalFeaturesOfSong.NUMBER_OF_TEMPORAL_FEATURES));
	}

	private boolean closerSongExists(double currentDistanceToLastSong) {
		return currentDistanceToLastSong != Double.POSITIVE_INFINITY;
	}

	private boolean isMoreSuitable(double distanceToAddedSong,
			double currentDistanceToAddedSong, double distanceToLastSong,
			double currentDistanceToLastSong) {

		return distanceToAddedSong < currentDistanceToAddedSong
				&& distanceToLastSong < currentDistanceToLastSong;
	}

	private void stripNonCandidates(Playlist playlist, Song addedSong,
			List<Song> availableSongs) {

		double[] featureWeighting = playlist.getSettings()
				.getFeatureWeighting();
		Song lastPlaylistSong = playlist.getLastItem().getCurrent();

		Map<Song, Distance> distancesAddedSong = distanceTable
				.column(addedSong);
		Map<Song, Distance> distancesLastPlaylistSong = distanceTable
				.column(lastPlaylistSong);

		double distanceFirstToAddedSong = weightedVectorLength(
				distancesLastPlaylistSong.get(distancesAddedSong),
				featureWeighting);

		for (Song song : availableSongs)

			if (isNoCandidate(featureWeighting, distancesAddedSong,
					distancesLastPlaylistSong, distanceFirstToAddedSong, song))
				availableSongs.remove(song);

	}

	private boolean isNoCandidate(double[] featureWeighting,
			Map<Song, Distance> distancesAddedSong,
			Map<Song, Distance> distancesLastPlaylistSong,
			double distanceFirstToAddedSong, Song song) {

		return !(weightedVectorLength(distancesLastPlaylistSong.get(song),
				featureWeighting) < distanceFirstToAddedSong && weightedVectorLength(
				distancesAddedSong.get(song), featureWeighting) < distanceFirstToAddedSong);
	}

	private void sortBySong(final Song referenceSong, List<Song> songsToSort,
			final double[] weighting) {

		if (songsToSort.size() > 2) {

			final Map<Song, Distance> distancesReferenceSong = distanceTable
					.column(referenceSong);

			Collections.sort(songsToSort, new Comparator<Song>() {

				@Override
				public int compare(Song x, Song y) {

					double distanceXtoRefSong = weightedVectorLength(
							distancesReferenceSong.get(x), weighting);
					double distanceYtoRefSong = weightedVectorLength(
							distancesReferenceSong.get(y), weighting);

					if (distanceXtoRefSong < distanceYtoRefSong)
						return -1;

					if (distanceXtoRefSong == distanceYtoRefSong)
						if (x.getId() == y.getId())
							return 0;
						else
							return x.getId() < y.getId() ? -1 : 1;

					else
						return 1;
				}
			});
		}
	}

}

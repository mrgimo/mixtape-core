package ch.hsr.mixtape;

import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hsr.mixtape.application.service.ApplicationFactory;
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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class Mixtape {

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime()
			.availableProcessors();

	private final ListeningExecutorService extractionExecutor = exitingFixedExecutorServiceWithBlockingTaskQueue(AVAILABLE_PROCESSORS);
	private final ListeningExecutorService postprocessingExecutor = exitingFixedExecutorService(4);

	private final List<Song> songs;
	private final List<Distance> distances;

	private final FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> harmonicFeatureExtractor = new HarmonicFeaturesExtractor();
	private final FeatureExtractor<PerceptualFeaturesOfWindow, PerceptualFeaturesOfSong> perceptualFeatureExtractor = new PerceptualFeaturesExtractor();
	private final FeatureExtractor<SpectralFeaturesOfWindow, SpectralFeaturesOfSong> spectralFeatureExtractor = new SpectralFeaturesExtractor();
	private final FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> temporalFeatureExtractor = new TemporalFeaturesExtractor();

	public Mixtape(List<Song> songs, List<Distance> distances) {
		this.songs = songs;
		this.distances = distances;
	}

	public List<Distance> addSong(Song song) throws IOException, InterruptedException, ExecutionException {
		FeaturesOfSong features = extractFeatures(song);
		song.setFeatures(features);

		List<Distance> newDistances = calcDistances(song);
		distances.addAll(newDistances);
		songs.add(song);

		return newDistances;
	}

	private FeaturesOfSong extractFeatures(Song song) throws IOException, InterruptedException, ExecutionException {
		SamplePublisher publisher = new SamplePublisher(extractionExecutor, postprocessingExecutor);

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

	private List<Distance> calcDistances(Song songX) {
		List<Distance> distances = Lists.newArrayListWithCapacity(songs.size());
		for (Song songY : songs)
			distances.add(distanceBetween(songX, songY));

		return distances;
	}

	private Distance distanceBetween(Song songX, Song songY) {
		FeaturesOfSong x = songX.getFeatures();
		FeaturesOfSong y = songY.getFeatures();

		return new Distance(songX, songY,
				harmonicFeatureExtractor.distanceBetween(x.harmonic, y.harmonic),
				perceptualFeatureExtractor.distanceBetween(x.perceptual, y.perceptual),
				spectralFeatureExtractor.distanceBetween(x.spectral, y.spectral),
				temporalFeatureExtractor.distanceBetween(x.temporal, y.temporal));
	}

	public double distanceBetween(long x, long y, double[] weighting) {
		int temp_x = (int) x; // TODO: no more ints here buddy ;-)
		int temp_y = (int) y; // TODO: no more ints here buddy ;-)
		if (temp_x > temp_y)
			return distance(distances[temp_x][temp_y], weighting);
		else if (temp_x < temp_y)
			return distance(distances[temp_y][temp_x], weighting);
		else
			return 0;
	}

	public List<Song> getSongs() {
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

		List<Song> availableSongs = getAvailableSongs();

		stripUsedSongs(playlist, addedSongs, availableSongs);

		sortBySong(playlist.getLastItem().getCurrent(), addedSongs, playlist
				.getSettings().getFeatureWeighting());

		for (Song song : addedSongs)
			mix(playlist, song, availableSongs);

	}

	public void mixAnotherSong(Playlist playlist, Song addedSong) throws InvalidPlaylistException {
		List<Song> availableSongs = getAvailableSongs();

		stripUsedSongs(playlist, addedSong, availableSongs);

		mix(playlist, addedSong, availableSongs);

	}

	// TODO: find good strategy -> maybe dont fetch all the time...
	private List<Song> getAvailableSongs() {
		return ApplicationFactory.getDatabaseService()
				.getAllSongs();
	}

	private void mix(Playlist currentPlaylist, Song addedSong,
			List<Song> availableSongs) throws InvalidPlaylistException {

		Song firstSong = currentPlaylist.getLastItem().getCurrent();
		Song lastSong = firstSong;
		Song mostSuitableSong = firstSong;

		double[] featureWeighting = currentPlaylist.getSettings()
				.getFeatureWeighting();

		double distanceFirstToAddedSong = distanceBetween(
				mostSuitableSong.getId(), addedSong.getId(), featureWeighting);

		double currentDistanceToAddedSong = distanceFirstToAddedSong;

		boolean closerSongExists = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = distanceBetween(song.getId(),
						addedSong.getId(), featureWeighting);
				double distanceToLastSong = distanceBetween(song.getId(),
						mostSuitableSong.getId(), featureWeighting);

				double distanceFirstToCurrentSong = distanceBetween(
						firstSong.getId(), song.getId(), featureWeighting);

				if (isMoreSuitable(distanceToAddedSong,
						currentDistanceToAddedSong, distanceToLastSong,
						currentDistanceToLastSong, distanceFirstToAddedSong,
						distanceFirstToCurrentSong)) {

					mostSuitableSong = song;
					currentDistanceToAddedSong = distanceToAddedSong;
					currentDistanceToLastSong = distanceToLastSong;
				}

			}

			if (closerSongExists(currentDistanceToLastSong)) {
				// TODO: why int? what am i supposed to do :<
				int[] d = new int[4];

				currentPlaylist.addItem(new PlaylistItem(mostSuitableSong,
						lastSong, d[0], d[1], d[2], d[3], false));

				lastSong = mostSuitableSong;
				availableSongs.remove(lastSong);
				closerSongExists = true;

			} else
				closerSongExists = false;

		} while (closerSongExists);

		currentPlaylist.addItem(new PlaylistItem(addedSong, lastSong, 0, 0, 0,
				0, true));

	}

	private boolean closerSongExists(double currentDistanceToLastSong) {
		return currentDistanceToLastSong != Double.POSITIVE_INFINITY;
	}

	private boolean isMoreSuitable(double distanceToAddedSong,
			double currentDistanceToAddedSong, double distanceToLastSong,
			double currentDistanceToLastSong, double distanceFirstToAddedSong,
			double distanceFirstToCurrentSong) {

		return distanceToAddedSong < currentDistanceToAddedSong
				&& distanceToLastSong < currentDistanceToLastSong
				&& distanceFirstToCurrentSong < distanceFirstToAddedSong;
	}

	// TODO: merge somehow with other method or remove ?
	private void stripUsedSongs(Playlist playlist, Song addedsong, List<Song> availableSongs) {
		availableSongs.removeAll(playlist.getSongsInPlaylist());
		availableSongs.remove(addedsong);
	}

	private void stripUsedSongs(Playlist currentPlayList,
			List<Song> addedSongs, List<Song> availableSongs) {
		availableSongs.removeAll(currentPlayList.getSongsInPlaylist());
		availableSongs.removeAll(addedSongs);
	}

	private void sortBySong(Song referenceSong, List<Song> songsToSort,
			double[] weighting) {

		if (songsToSort.size() > 2)
			Collections.sort(songsToSort, new SortBySong(songsToSort.get(0),
					weighting));
	}

	private class SortBySong implements Comparator<Song> {

		private Song refSong;
		private double[] weighting;

		public SortBySong(Song song, double[] weighting) {
			this.refSong = song;
			this.weighting = weighting;
		}

		@Override
		public int compare(Song x, Song y) {

			double distanceXtoRefSong = distanceBetween(x.getId(),
					refSong.getId(), weighting);
			double distanceYtoRefSong = distanceBetween(y.getId(),
					refSong.getId(), weighting);

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

	}

}

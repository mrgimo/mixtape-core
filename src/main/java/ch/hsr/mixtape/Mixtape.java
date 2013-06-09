package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.vectorLength;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorService;
import static ch.hsr.mixtape.concurrency.CustomExecutors.exitingFixedExecutorServiceWithBlockingTaskQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesExtractor;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfSong;
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

	private static final int NUMBER_OF_AVAILABLE_PROCESSORS = Runtime
			.getRuntime().availableProcessors();
	private static final int NUMBER_OF_FEATURE_EXTRACTORS = 4;

	private final ListeningExecutorService extractionExecutor = exitingFixedExecutorServiceWithBlockingTaskQueue(NUMBER_OF_AVAILABLE_PROCESSORS);
	private final ListeningExecutorService postprocessingExecutor = exitingFixedExecutorService(NUMBER_OF_FEATURE_EXTRACTORS);

	private final HarmonicFeaturesExtractor harmonicFeatureExtractor = new HarmonicFeaturesExtractor();
	private final PerceptualFeaturesExtractor perceptualFeatureExtractor = new PerceptualFeaturesExtractor();
	private final SpectralFeaturesExtractor spectralFeatureExtractor = new SpectralFeaturesExtractor();
	private final TemporalFeaturesExtractor temporalFeatureExtractor = new TemporalFeaturesExtractor();

	private HashBasedTable<Song, Song, Distance> distanceTable = HashBasedTable
			.create();

	public Mixtape(List<Distance> distances) {
		for (Distance distance : distances)
			distanceTable.put(distance.getSongX(), distance.getSongY(),
					distance);
	}

	public Collection<Distance> addSong(Song song) throws IOException,
			InterruptedException, ExecutionException {
		FeaturesOfSong features = extractFeaturesOf(song);
		song.setFeatures(features);

		Collection<Distance> addedDistances = addDistancesTo(song);
		return Lists.newArrayList(addedDistances);
	}

	private FeaturesOfSong extractFeaturesOf(Song song) throws IOException,
			InterruptedException, ExecutionException {
		SampleWindowPublisher publisher = new SampleWindowPublisher(
				extractionExecutor, postprocessingExecutor);

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

	private Collection<Distance> addDistancesTo(Song song) {
		Map<Song, Distance> distancesTo = distanceTable.row(song);
		for (Song otherSong : distanceTable.rowKeySet())
			if (!song.equals(otherSong))
				distancesTo
						.put(otherSong, calcDistanceBetween(song, otherSong));

		return distancesTo.values();
	}

	private Distance calcDistanceBetween(Song newSong, Song oldSong) {
		FeaturesOfSong x = newSong.getFeatures();
		FeaturesOfSong y = oldSong.getFeatures();

		return new Distance(newSong, oldSong,
				harmonicFeatureExtractor
						.distanceBetween(x.harmonic, y.harmonic),
				perceptualFeatureExtractor.distanceBetween(x.perceptual,
						y.perceptual),
				spectralFeatureExtractor
						.distanceBetween(x.spectral, y.spectral),
				temporalFeatureExtractor
						.distanceBetween(x.temporal, y.temporal));
	}

	public List<Song> getSongs() {
		return Lists.newArrayList(distanceTable.rowKeySet());
	}

	public List<Distance> getDistances() {
		return Lists.newArrayList(distanceTable.values());
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

		sortBySong(playlist.getLastItem().getCurrent(), addedSongs, playlist
				.getSettings().getFeatureWeighting());

		for (Song song : addedSongs) {

			List<Song> availableSongs = Lists.newArrayList(distanceTable
					.columnKeySet());

			availableSongs.removeAll(playlist.getSongsInPlaylist());
			availableSongs.removeAll(addedSongs);

			mix(playlist, song, availableSongs);
		}

	}

	public void mixAnotherSong(Playlist playlist, Song addedSong)
			throws InvalidPlaylistException {
		ArrayList<Song> availableSongs = Lists.newArrayList(distanceTable
				.columnKeySet());

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
		
		double mostSuitableDistanceToAddedSong = Double.POSITIVE_INFINITY;

		boolean closerSongExists = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = weightedVectorLength(
						distanceTable.get(song, addedSong), featureWeighting);
				double distanceToLastSong = weightedVectorLength(
						distanceTable.get(song, mostSuitableSong),
						featureWeighting);

				if (distanceToAddedSong < currentDistanceToAddedSong)
					if (distanceToLastSong < currentDistanceToLastSong) {

						mostSuitableSong = song;
						mostSuitableDistanceToAddedSong = distanceToAddedSong;
						currentDistanceToLastSong = distanceToLastSong;
					} else
						availableSongs.remove(song);

			}

			if (closerSongExists(currentDistanceToLastSong)) {
				// TODO: set similarity?

				currentPlaylist.addItem(new PlaylistItem(mostSuitableSong,
						lastSong, 0, 1, 2, 3, false));

				lastSong = mostSuitableSong;
				currentDistanceToAddedSong = mostSuitableDistanceToAddedSong;
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

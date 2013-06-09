package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.vectorLength;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfSong;
import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.PlaylistItem;
import ch.hsr.mixtape.model.Song;

public class MixNoDuplicates implements MixStrategy{

	private Mixtape mixtape;

	public MixNoDuplicates(Mixtape mixtape) {
		this.mixtape = mixtape;

	}

	@Override
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

	@Override
	public void mixMultipleSongs(Playlist playlist, List<Song> addedSongs)
			throws InvalidPlaylistException {

		sortBySong(playlist.getLastItem().getCurrent(), addedSongs, playlist
				.getSettings().getFeatureWeighting());

		for (Song song : addedSongs) {

			List<Song> availableSongs = mixtape.getSongs();

			availableSongs.removeAll(playlist.getSongsInPlaylist());
			availableSongs.removeAll(addedSongs);

			mix(playlist, song, availableSongs);
		}

	}

	@Override
	public void mixAnotherSong(Playlist playlist, Song addedSong)
			throws InvalidPlaylistException {
		List<Song> availableSongs = mixtape.getSongs();

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
		
		Map<Song, Distance> distancesAddedSong = mixtape.distances(addedSong);

		double currentDistanceToAddedSong = weightedVectorLength(
				distancesAddedSong.get(firstSong), featureWeighting);

		double mostSuitableDistanceToAddedSong = Double.POSITIVE_INFINITY;

		boolean closerSongExists = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = weightedVectorLength(
						distancesAddedSong.get(song), featureWeighting);
				double distanceToLastSong = weightedVectorLength(
						mixtape.distance(song, mostSuitableSong),
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

		Map<Song, Distance> distancesAddedSong = mixtape.distances(addedSong);
		Map<Song, Distance> distancesLastPlaylistSong = mixtape
				.distances(lastPlaylistSong);

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

			final Map<Song, Distance> distancesReferenceSong = mixtape
					.distances(referenceSong);

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

package ch.hsr.mixtape;

import static ch.hsr.mixtape.util.MathUtils.vectorLength;

import java.util.List;
import java.util.Map;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.PlaylistItem;
import ch.hsr.mixtape.model.PlaylistSettings;
import ch.hsr.mixtape.model.Song;

import com.google.common.collect.Lists;

public class SmoothMix implements MixStrategy {

	private Mixtape mixtape;

	public SmoothMix(Mixtape mixtape) {
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

		 List<Song> sortedSongs = sortByDistance(playlist.getLastItem().getCurrent(), addedSongs, playlist
				.getSettings());

		for (Song song : sortedSongs) {

			List<Song> availableSongs = mixtape.getSongs();
			availableSongs.removeAll(sortedSongs);

			if (!playlist.getSettings().allowDuplicates())
				availableSongs.removeAll(playlist.getSongsInPlaylist());

			mix(playlist, song, availableSongs);
		}

	}

	@Override
	public void mixAnotherSong(Playlist playlist, Song addedSong)
			throws InvalidPlaylistException {
		List<Song> availableSongs = mixtape.getSongs();
		availableSongs.remove(addedSong);

		if (!playlist.getSettings().allowDuplicates())
			availableSongs.removeAll(playlist.getSongsInPlaylist());

		mix(playlist, addedSong, availableSongs);
	}

	private void mix(Playlist currentPlaylist, Song addedSong,
			List<Song> availableSongs) throws InvalidPlaylistException {

		stripNonCandidates(currentPlaylist, addedSong, availableSongs);

		Song firstSong = currentPlaylist.getLastItem().getCurrent();
		Song lastSong = firstSong;
		Song mostSuitableSong = firstSong;

		PlaylistSettings playlistSettings = currentPlaylist.getSettings();

		Map<Song, Distance> distancesAddedSong = mixtape.distances(addedSong);

		double currentDistanceToAddedSong = weightedVectorLength(
				distancesAddedSong.get(firstSong), playlistSettings);

		double mostSuitableDistanceToAddedSong = Double.POSITIVE_INFINITY;

		boolean closerSongExists = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = weightedVectorLength(
						distancesAddedSong.get(song), playlistSettings);
				double distanceToLastSong = weightedVectorLength(
						mixtape.distanceBetween(song, mostSuitableSong),
						playlistSettings);

				if (distanceToAddedSong < currentDistanceToAddedSong)
					if (distanceToLastSong < currentDistanceToLastSong) {

						mostSuitableSong = song;
						mostSuitableDistanceToAddedSong = distanceToAddedSong;
						currentDistanceToLastSong = distanceToLastSong;
					} else
						availableSongs.remove(song);

			}

			if (closerSongExists(currentDistanceToLastSong)) {

				currentPlaylist.addItem(createPlaylistItem(lastSong, mostSuitableSong, playlistSettings, false));

				lastSong = mostSuitableSong;
				currentDistanceToAddedSong = mostSuitableDistanceToAddedSong;
				availableSongs.remove(lastSong);
				closerSongExists = true;

			} else
				closerSongExists = false;

		} while (closerSongExists);

		currentPlaylist.addItem(createPlaylistItem(lastSong, mostSuitableSong, playlistSettings, true));

	}

	private PlaylistItem createPlaylistItem(Song lastSong, Song mostSuitableSong, PlaylistSettings playlistSettings,
			boolean isUserWish) {

		Distance distance = mixtape.distanceBetween(lastSong, mostSuitableSong);

		int harmonicSimilarity = (int) (100 - distance.getHarmonicDistance() * playlistSettings.getHarmonicSimilarity());
		int perceptualSimilarity = (int) (100 - distance.getPerceptualDistance()
				* playlistSettings.getPerceptualSimilarity());
		int spectralSimilarity = (int) (100 - distance.getSpectralDistance() * playlistSettings.getSpectralSimilarity());
		int temporalSimilarity = (int) (100 - distance.getTemporalDistance() * playlistSettings.getTemporalSimilarity());

		return new PlaylistItem(
				mostSuitableSong,
				lastSong,
				harmonicSimilarity,
				perceptualSimilarity,
				spectralSimilarity,
				temporalSimilarity,
				isUserWish);
	}

	private double weightedVectorLength(Distance distance,
			PlaylistSettings playlistSettings) {

		return vectorLength(
				distance.getHarmonicDistance() * playlistSettings.getHarmonicSimilarity() * 0.01,
				distance.getPerceptualDistance() * playlistSettings.getPerceptualSimilarity() * 0.01,
				distance.getSpectralDistance() * playlistSettings.getSpectralSimilarity() * 0.01,
				distance.getTemporalDistance() * playlistSettings.getTemporalSimilarity() * 0.01);
	}

	private boolean closerSongExists(double currentDistanceToLastSong) {
		return currentDistanceToLastSong != Double.POSITIVE_INFINITY;
	}

	private void stripNonCandidates(Playlist playlist, Song addedSong,
			List<Song> availableSongs) {

		Song lastPlaylistSong = playlist.getLastItem().getCurrent();

		Map<Song, Distance> distancesAddedSong = mixtape.distances(addedSong);
		Map<Song, Distance> distancesLastPlaylistSong = mixtape
				.distances(lastPlaylistSong);

		double distanceFirstToAddedSong = weightedVectorLength(
				distancesLastPlaylistSong.get(distancesAddedSong),
				playlist.getSettings());

		for (Song song : availableSongs)

			if (isNoCandidate(playlist.getSettings(), distancesAddedSong,
					distancesLastPlaylistSong, distanceFirstToAddedSong, song))
				availableSongs.remove(song);

	}

	private boolean isNoCandidate(PlaylistSettings playlistSettings,
			Map<Song, Distance> distancesAddedSong,
			Map<Song, Distance> distancesLastPlaylistSong,
			double distanceFirstToAddedSong, Song song) {

		return !(weightedVectorLength(distancesLastPlaylistSong.get(song),
				playlistSettings) < distanceFirstToAddedSong && weightedVectorLength(
				distancesAddedSong.get(song), playlistSettings) < distanceFirstToAddedSong);
	}

	/*
	 * @ commented code, maybe test both ways
	 */
	private List<Song> sortByDistance(final Song referenceSong, List<Song> songsToSort,
			final PlaylistSettings playlistSettings) {

		if (songsToSort.size() > 2) {

//			final Map<Song, Distance> distancesReferenceSong = mixtape
//					.distances(referenceSong);

			List<Song> sortedSongs = Lists.newArrayListWithCapacity(songsToSort.size());
			Song lastSongInList = referenceSong;

			for (int i = 0; i < songsToSort.size(); i++) {
				Song mostSuitableCandidate = referenceSong;
				Map<Song, Distance> distancesLast = mixtape.distances(lastSongInList);
				double distanceMostSuitabletoLast = Double.POSITIVE_INFINITY;
				for (Song song : songsToSort) {
					double distanceToLast = weightedVectorLength(distancesLast.get(song),
							playlistSettings);

					if (distanceToLast < distanceMostSuitabletoLast) {
						mostSuitableCandidate = song;
						distanceMostSuitabletoLast = distanceToLast;
					}

					sortedSongs.add(mostSuitableCandidate);
					lastSongInList = mostSuitableCandidate;
					songsToSort.remove(mostSuitableCandidate);
				}
			}
			
			return sortedSongs;

//			Collections.sort(songsToSort, new Comparator<Song>() {
//
//				@Override
//				public int compare(Song x, Song y) {
//
//					double distanceXtoRefSong = weightedVectorLength(
//							distancesReferenceSong.get(x), playlistSettings);
//					double distanceYtoRefSong = weightedVectorLength(
//							distancesReferenceSong.get(y), playlistSettings);
//
//					if (distanceXtoRefSong < distanceYtoRefSong)
//						return -1;
//
//					if (distanceXtoRefSong == distanceYtoRefSong)
//						if (x.getId() == y.getId())
//							return 0;
//						else
//							return x.getId() < y.getId() ? -1 : 1;
//
//					else
//						return 1;
//				}
//			});
		}
		else
			return songsToSort;
	}

}

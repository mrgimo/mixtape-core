package ch.hsr.mixtape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.hsr.mixtape.domain.Song;

public class Pathfinder {

	public List<Song> createPlaylist(Mixtape mixtape, List<Song> chosenSongs,
			List<Song> availableSongs, double[] weighting) {
		sortByFirstSong(chosenSongs, mixtape, weighting);
		List<Song> playList = new ArrayList<Song>();

		if (chosenSongs.size() > 1)
			playList.add(chosenSongs.get(0));
		else
			System.out
					.println("ATM u need atleast 2 chosen songz, gimme more songs!");

		for (int i = 1; i < chosenSongs.size(); i++)
			extendPlaylist(mixtape, playList, chosenSongs.get(i),
					availableSongs, weighting);

		return playList;
	}

	private void sortByFirstSong(List<Song> chosenSongs, Mixtape mixtape,
			double[] weighting) {

		if (chosenSongs.size() > 2)
			Collections
					.sort(chosenSongs, new SortByFirstSong(chosenSongs.get(0),
							mixtape, weighting));
	}

	// TODO: possible to remove songs in playlist from available songs ? -> get
	// rid of playlist.contains(songToadd)

	public void extendPlaylist(Mixtape mixtape, List<Song> playList,
			Song addedSong, List<Song> availableSongs, double[] weighting) {

		Song firstSongInPlaylist = playList.get(playList.size() - 1);
		Song lastSongInPlaylist = firstSongInPlaylist;

		double distanceFirstToAddedSong = mixtape.distanceBetween(
				lastSongInPlaylist, addedSong, weighting);

		double currentDistanceToAddedSong = distanceFirstToAddedSong;

		boolean closerSongFound = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = mixtape.distanceBetween(song,
						addedSong, weighting);
				double distanceToLastSong = mixtape.distanceBetween(song,
						lastSongInPlaylist, weighting);

				double distanceFirstToCurrentSong = mixtape.distanceBetween(
						firstSongInPlaylist, song, weighting);

				if (!playList.contains(song)
						&& isMoreSuitable(distanceToAddedSong,
								currentDistanceToAddedSong, distanceToLastSong,
								currentDistanceToLastSong,
								distanceFirstToAddedSong,
								distanceFirstToCurrentSong)) {

					lastSongInPlaylist = song;
					currentDistanceToAddedSong = distanceToAddedSong;
					currentDistanceToLastSong = distanceToLastSong;
				}

			}

			if (closerSongFound(currentDistanceToLastSong)) {
				playList.add(lastSongInPlaylist);
				closerSongFound = true;
			} else
				closerSongFound = false;

		} while (closerSongFound);

		playList.add(addedSong);

	}

	private boolean closerSongFound(double currentDistanceToLastSong) {
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

	private class SortByFirstSong implements Comparator<Song> {

		private Song firstSong;
		private Mixtape mixtape;
		private double[] weighting;

		public SortByFirstSong(Song firstSong, Mixtape mixtape,
				double[] weighting) {
			this.firstSong = firstSong;
			this.mixtape = mixtape;
			this.weighting = weighting;
		}

		@Override
		public int compare(Song x, Song y) {

			double distanceXtoFirst = mixtape.distanceBetween(x, firstSong,
					weighting);
			double distanceYtoFirst = mixtape.distanceBetween(y, firstSong,
					weighting);

			if (distanceXtoFirst < distanceYtoFirst)
				return -1;

			if (distanceXtoFirst == distanceYtoFirst)
				if (x.getId() == y.getId())
					return 0;
				else
					return x.getId() < y.getId() ? -1 : 1;

			else
				return 1;
		}

	}

}

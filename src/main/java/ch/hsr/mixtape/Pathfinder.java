package ch.hsr.mixtape;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.mixtape.domain.Song;

public class Pathfinder {

	public List<Song> createPlaylist(Mixtape mixtape, List<Song> chosenSongs,
			List<Song> availableSongs, double[] weighting) {
		sortByFirstSong(chosenSongs);
		List<Song> playList = new ArrayList<Song>();

		if (chosenSongs.size() > 1)
			playList.add(chosenSongs.get(0));
		else
			System.out.println("ATM u need atleast 2 chosen songz, gimme more songs!");

		for (int i = 1; i < chosenSongs.size(); i++) {
			extendPlaylist(mixtape, playList, chosenSongs.get(i), availableSongs, weighting);
		}
		
		return playList;
	}

	private void sortByFirstSong(List<Song> chosenSongs) {
		
		//sort relative to first Song by distances

	}

	public void extendPlaylist(Mixtape mixtape, List<Song> playList,
			Song addedSong, List<Song> availableSongs, double[] weighting) {

		Song lastSongInPlaylist = playList
				.get(playList.size() - 1);

		double currentDistanceToAddedSong = mixtape.distanceBetween(
				lastSongInPlaylist, addedSong, weighting);

		boolean closerSongFound = false;

		do {

			double currentDistanceToLastSong = Double.POSITIVE_INFINITY;

			for (Song song : availableSongs) {
				double distanceToAddedSong = mixtape.distanceBetween(song,
						addedSong, weighting);
				double distanceToLastSong = mixtape.distanceBetween(song,
						lastSongInPlaylist, weighting);

				if (!playList.contains(song)
						&& isCloserSong(distanceToAddedSong,
								currentDistanceToAddedSong, distanceToLastSong,
								currentDistanceToLastSong)) {

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

	private boolean isCloserSong(double distanceToAddedSong,
			double currentDistanceToAddedSong, double distanceToLastSong,
			double currentDistanceToLastSong) {
		
		return distanceToAddedSong < currentDistanceToAddedSong
				&& distanceToLastSong < currentDistanceToLastSong;
	}

}

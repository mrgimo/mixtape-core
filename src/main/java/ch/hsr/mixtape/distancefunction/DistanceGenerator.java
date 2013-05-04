package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Song;

public class DistanceGenerator implements Runnable {
	
	private Song song;
	private ArrayList<Song> songsToCompare;
	private int startIndex;

	public DistanceGenerator(Song song, ArrayList<Song> songsToCompare, int startIndex ) {
		this.startIndex = startIndex;
		this.songsToCompare = songsToCompare;
		this.song = song;
	}
	
	@Override
	public void run() {
		computeDistances();
	}

	public void computeDistances() {
		NormalizedInformationDistanceSpeedUp_v2 distanceFunction = new NormalizedInformationDistanceSpeedUp_v2();
		
		for (int i = startIndex; i < songsToCompare.size(); i++) {
			Song songToCompare = songsToCompare.get(i);
			double distance = distanceFunction.distance(song, songToCompare);
			song.setDistance(songToCompare, distance);
			songToCompare.setDistance(song, distance);
		}
	}


}

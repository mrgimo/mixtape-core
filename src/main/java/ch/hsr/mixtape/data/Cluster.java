package ch.hsr.mixtape.data;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

	private ArrayList<Song> clusteredSongs = new ArrayList<Song>();
	private String name = "Default";

	public Cluster(ArrayList<Song> clusteredSongs) {
		this.clusteredSongs = clusteredSongs;
	}

	public Cluster(String name, ArrayList<Song> clusteredSongs) {
		this.name = name;
		this.clusteredSongs = clusteredSongs;
	}

	public Cluster() {
	}

	public ArrayList<Song> getSongs() {
		return clusteredSongs;
	}

	public void setSongs(ArrayList<Song> songsInCluster) {
		this.clusteredSongs = songsInCluster;
	}

	public String getName() {
		return name;
	}

	public void add(Song song) {
		clusteredSongs.add(song);
	}

	public boolean remove(Song song) {
		return clusteredSongs.remove(song);
	}

	public void add(List<Song> songs) {
		clusteredSongs.addAll(songs);
	}
	
	public int clusterSize() {
		return clusteredSongs.size();
	}

	// TODO: just for testing purposes
	public double distance(Cluster clusterToCompare) {
		
		if(clusterToCompare.clusterSize() == 0)
			return Double.POSITIVE_INFINITY;

		double maxDistance = 0.0;

		for (Song song : clusteredSongs) {
			double songDistance = 0.0;

			for (Song songToCompare : clusterToCompare.getSongs()) {
				double distance = song.distanceTo(songToCompare) ;
				
				songDistance = distance > songDistance ? distance : songDistance;
			}
			
			maxDistance = songDistance > maxDistance ? songDistance : maxDistance;
		}

		return maxDistance;
	}
}

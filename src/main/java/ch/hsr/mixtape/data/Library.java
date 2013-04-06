package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class Library {


	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	public int getClusterCount() {
		return clusters.size();
	}

	public ArrayList<Song> getAllSongs() {
		ArrayList<Song> songs = new ArrayList<Song>();
		for (Cluster cluster : clusters) {
			songs.addAll(cluster.getSongs());
		}
		return songs;
	}

	public void setClusters(ArrayList<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	public ArrayList<Cluster> getClusters() {
		return clusters;
	}
}

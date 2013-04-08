package ch.hsr.mixtape.data;

import java.util.ArrayList;
import java.util.List;


public class Cluster {
	
	private ArrayList<Song> clusteredSongs = new ArrayList<Song>();
	private String name = "Default";
	
	public Cluster(ArrayList<Song> clusteredSongs){
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
	
	

}

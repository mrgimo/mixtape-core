package ch.hsr.mixtape.clustering.algorithm;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Song;

public class Hierarchical implements ClusterAlgorithm {
	
	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	@Override
	public ArrayList<Cluster> cluster(ArrayList<Song> songs) {
		createInitialClusters(songs);
		return clusters;
	}

	private void createInitialClusters(ArrayList<Song> allSongs) {
		for (Song song : allSongs) {
			Cluster cluster = new Cluster();
			cluster.add(song);
			clusters.add(cluster);
		}
	}

}

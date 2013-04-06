package ch.hsr.mixtape.clustering.algorithm;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Song;

public interface ClusterAlgorithm {
	
	ArrayList<Cluster> cluster(ArrayList<Song> songs);
		

}

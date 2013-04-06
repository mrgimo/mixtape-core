package ch.hsr.mixtape.library;

//move to other package ?

import java.util.ArrayList;

import ch.hsr.mixtape.clustering.algorithm.ClusterAlgorithm;
import ch.hsr.mixtape.clustering.algorithm.Slink;
import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Library;

public class LibraryController {
	
	private ClusterAlgorithm clusterAlgorithm = new Slink();
	private Library library = new Library();
	
	public void organizeLibrary() {
		ArrayList<Cluster> clusters  = clusterAlgorithm.cluster(library.getAllSongs());
		library.setClusters(clusters);
	}
}

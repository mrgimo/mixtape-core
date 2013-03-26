package ch.hsr.mixtape.library;

//move to other package ?

import java.util.ArrayList;

import ch.hsr.mixtape.clustering.algorithm.ClusterAlgorithm;
import ch.hsr.mixtape.clustering.algorithm.Hierarchical;
import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Library;
import ch.hsr.mixtape.data.Song;

public class LibraryController {
	
	private ClusterAlgorithm clusterAlgorithm = new Hierarchical();
	private Library library = new Library();
	
	public void organizeLibrary() {
		library.mergeAllClustersToDefault();
		ArrayList<Song> songsInLibrary = library.getDefaultCluster().getSongs();
		ArrayList<Cluster> groupedSongs  = clusterAlgorithm.cluster(songsInLibrary);
		
		for (Cluster cluster : groupedSongs) {
			library.add(cluster);
		}
	}
	
}

package ch.hsr.mixtape.library;

//move to other package ?

import java.util.ArrayList;

import ch.hsr.mixtape.clustering.algorithm.ClusterAlgorithm;
import ch.hsr.mixtape.clustering.algorithm.Slink;
import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Library;
import ch.hsr.mixtape.data.Song;

public class LibraryController {
	
	private ClusterAlgorithm clusterAlgorithm = new Slink();
	private Library library = new Library();
	
	public void organizeLibrary() {
		ArrayList<Cluster> clusters  = clusterAlgorithm.cluster(library.getAllSongs());
		library.setClusters(clusters);
	}
	
	public void addSongsToLibrary(ArrayList<Song> songs) {
		library.add(songs);
	}

	public void printClusters() {
		System.out.println();
		for (Cluster cluster : library.getClusters()) {
			System.out.println("Cluster: " + cluster.getName());
			for (Song song : cluster.getSongs()) {
				System.out.println(song.getName() + "\nRMS: " + song.getFeatureVector().getFeatureValues()[0] + "\nZC: " + song.getFeatureVector().getFeatureValues()[1]);
				System.out.println();
			}
			System.out.println();
			System.out.println();
		}
		
	}
}

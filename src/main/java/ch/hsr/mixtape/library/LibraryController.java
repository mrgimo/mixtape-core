package ch.hsr.mixtape.library;

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
		ArrayList<Cluster> clusters = clusterAlgorithm.cluster(library
				.getAllSongs());
		library.setClusters(clusters);
	}

	public void addSongsToLibrary(ArrayList<Song> songs) {
		library.add(songs);
	}

	public void printClusters() {
		int clusterCount = 1;
		System.out.println("\n\nPrinting cluster: ");
		for (Cluster cluster : library.getClusters()) {
			System.out
					.println("\n---------------------------------------------------------");
			System.out.println("\nCluster: " + clusterCount++ + "\n");

			for (Song song : cluster.getSongs())
				System.out.println(song.getName() + "\n");
		}

	}
}

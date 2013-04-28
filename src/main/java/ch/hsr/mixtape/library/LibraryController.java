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
		System.out.println();
		for (Cluster cluster : library.getClusters()) {
			System.out.println("---------------------------------------------------------");
			System.out.println("Cluster: " + cluster.getName() + "\n");
			for (Song song : cluster.getSongs()) {
				System.out.println(song.getName() + "\n"
//						+ "RMS: "
//						+ song.getFeatureVector().getFeatureMeanValues()[0] + "\n"
//						+ "ZC: "
//						+ song.getFeatureVector().getFeatureMeanValues()[1] + "\n"
//						+ song.getFeatureVector().getFeatureMeanValues()[2] + "\n"
//						+ song.getFeatureVector().getFeatureMeanValues()[3] + "\n"
//						+ song.getFeatureVector().getFeatureMeanValues()[4] + "\n"
//						+ song.getFeatureVector().getFeatureValues()[5] + "\n"
//						+ song.getFeatureVector().getFeatureValues()[6] + "\n"
				);
				System.out.println();
			}
			System.out.println();
			System.out.println();
		}

	}
}

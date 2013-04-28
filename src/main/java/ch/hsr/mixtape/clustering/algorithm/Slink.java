package ch.hsr.mixtape.clustering.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.hsr.mixtape.Mixer;
import ch.hsr.mixtape.Mixer.LoggingFormatter;
import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Song;

/**
 * Hierarchical single link cluster algorithm based on 'SLINK : An optimally
 * efficient algorithm for the single-link cluster method' by R. Sibson.
 * 
 * Slink runs in O(n^2)
 */

// TODO: remove loggin shissle

public class Slink implements ClusterAlgorithm {

	private Logger logger;

	private static final double DISTANCE_THRESHOLD = 1.67;
	private static final double INFINITY = Double.POSITIVE_INFINITY;
	private static final int CLUSTER_COUNT = 2;

	private double[][] distanceMatrix;
	private int[] nearestCluster;

	public Slink() {
		initLogger();
		
	}

	private void initLogger() {
		logger = Logger.getLogger("Clustering Report\n");
		try {
			FileHandler fileHandler = new FileHandler("logs/clustering.log",
					false);
			logger.addHandler(fileHandler);
			logger.setLevel(Level.ALL);
			fileHandler.setFormatter(new Mixer.LoggingFormatter());
			logger.log(Level.INFO, "\n\nClustering Report");
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<Cluster> cluster(ArrayList<Song> songs) {

		nearestCluster = new int[songs.size()];
		generateInitialDistances(songs);

		return findClusters(songs);
	}

	private ArrayList<Cluster> findClusters(ArrayList<Song> songs) {
		ArrayList<Cluster> initialClusters = new ArrayList<Cluster>();
		// TODO : BLUBB ^^
		for (Song song : songs) {
			Cluster cluster = new Cluster();
			cluster.add(song);
			initialClusters.add(cluster);
		}

		int clusterCount = initialClusters.size();
		System.out.print("\n\nBuilding clusters: \n\n");

		double minDistance = 0.0;

		while (minDistance < DISTANCE_THRESHOLD) {
			logger.log(Level.INFO, "\n\n\n-----------------------------------------\n\nNew clustering round\n\nMinimum distance: "
							+ minDistance);
			int clusterPair1 = 0;
			for (int i = 0; i < songs.size(); i++)
				if (distanceMatrix[i][nearestCluster[i]] < distanceMatrix[clusterPair1][nearestCluster[clusterPair1]])
					clusterPair1 = i;
			int clusterPair2 = nearestCluster[clusterPair1];

			minDistance = distanceMatrix[clusterPair1][clusterPair2];

			for (int j = 0; j < songs.size(); j++)
				if (distanceMatrix[clusterPair2][j] < distanceMatrix[clusterPair1][j])
					distanceMatrix[clusterPair1][j] = distanceMatrix[j][clusterPair1] = distanceMatrix[clusterPair2][j];
			distanceMatrix[clusterPair1][clusterPair1] = INFINITY;

			for (int i = 0; i < songs.size(); i++)
				distanceMatrix[clusterPair2][i] = distanceMatrix[i][clusterPair2] = INFINITY;

			for (int j = 0; j < songs.size(); j++) {
				if (nearestCluster[j] == clusterPair2)
					nearestCluster[j] = clusterPair1;
				if (distanceMatrix[clusterPair1][j] < distanceMatrix[clusterPair1][nearestCluster[clusterPair1]])
					nearestCluster[clusterPair1] = j;
			}

			Cluster masterCluster = initialClusters.get(clusterPair1);
			Cluster mergedCluster = initialClusters.get(clusterPair2);
			masterCluster.add(mergedCluster.getSongs());
			mergedCluster.getSongs().clear();

			printClusters(initialClusters);

			clusterCount--;
		}

		return getRemainingClusters(initialClusters);

	}

	private void printClusters(ArrayList<Cluster> initialClusters) {
		for (int i = 0; i < initialClusters.size(); i++) {
			if (!initialClusters.get(i).getSongs().isEmpty()) {
				logger.log(Level.INFO, "\n\nCluster " + i);
				ArrayList<Song> songs = initialClusters.get(i).getSongs();
				for (Song song : songs) {
					logger.log(Level.INFO, song.getName());
				}
			}
		}
	}

	private ArrayList<Cluster> getRemainingClusters(ArrayList<Cluster> clusters) {
		ArrayList<Cluster> remainingClusters = new ArrayList<Cluster>();

		for (int i = 0; i < nearestCluster.length; i++) {
			if (distanceMatrix[i][nearestCluster[i]] != INFINITY) {
				remainingClusters.add(clusters.get(i));
			}
		}
		return remainingClusters;
	}

	private void generateInitialDistances(ArrayList<Song> songs) {
		distanceMatrix = new double[songs.size()][songs.size()];

		System.out.print("\ngenerating distance matrix: \t");

		for (int i = 0; i < songs.size(); i++) {
			System.out.print("#");

			for (int j = 0; j < songs.size(); j++) {
				if (i == j)
					distanceMatrix[i][j] = INFINITY;
				else
					distanceMatrix[i][j] = songs.get(i)
							.distanceTo(songs.get(j));
				if (distanceMatrix[i][j] < distanceMatrix[i][nearestCluster[i]])
					nearestCluster[i] = j;
			}
		}

	}
}
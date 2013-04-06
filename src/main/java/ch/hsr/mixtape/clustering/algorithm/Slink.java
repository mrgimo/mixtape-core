package ch.hsr.mixtape.clustering.algorithm;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Cluster;
import ch.hsr.mixtape.data.Song;

public class Slink implements ClusterAlgorithm {

	private static final double INFINITY = Double.POSITIVE_INFINITY;
	private static final int CLUSTER_COUNT = 5;

	private double[][] distanceMatrix;
	private int[] nearestCluster;

	@Override
	public ArrayList<Cluster> cluster(ArrayList<Song> songs) {

		nearestCluster = new int[songs.size()];
		generateInitialDistances(songs);

		return findClusters(songs);
	}

	private ArrayList<Cluster> findClusters(ArrayList<Song> songs) {
		ArrayList<Cluster> initialClusters = new ArrayList<Cluster>();
		//TODO : BLUBB ^^
		for (Song song : songs) {
			Cluster cluster = new Cluster();
			cluster.add(song);
			initialClusters.add(cluster);
		}
		
		int clusterCount = initialClusters.size();
		while (clusterCount > CLUSTER_COUNT) {
				int clusterPair1 = 0;
				for (int i = 0; i < songs.size(); i++)
					if (distanceMatrix[i][nearestCluster[i]] < distanceMatrix[clusterPair1][nearestCluster[clusterPair1]])
						clusterPair1 = i;
				int clusterPair2 = nearestCluster[clusterPair1];
				
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
				
				clusterCount--;
		}
		
		return getRemainingClusters(initialClusters);
		
	}

	private ArrayList<Cluster> getRemainingClusters(ArrayList<Cluster> clusters) {
		ArrayList<Cluster> remainingClusters = new ArrayList<Cluster>();
		
		for (int i = 0; i < nearestCluster.length; i++) {
			if(distanceMatrix[i][nearestCluster[i]] != INFINITY){
				remainingClusters.add(clusters.get(i));
			}
		}
		return remainingClusters;
	}

	private void generateInitialDistances(ArrayList<Song> songs) {
		distanceMatrix = new double[songs.size()][songs.size()];
		for (int i = 0; i < songs.size(); i++) {
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
package ch.hsr.mixtape.data;

import java.util.ArrayList;



public class Library {
	
	private static final String DEFAULT_CLUSTER = "Default";
	
	private Cluster defaultCluster;
	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	
	public Library() {
		initLibrary();
	}
	
	private void initLibrary() {
		defaultCluster = new Cluster(DEFAULT_CLUSTER, new ArrayList<Song>()); 
	}


	public ArrayList<Cluster> getClusters() {
		return clusters;
	}
	
	public Cluster getDefaultCluster() {
		return defaultCluster;
	}
	
	public void createCluster(ArrayList<Song> clusteredSongs) {
		clusters.add(new Cluster(DEFAULT_CLUSTER, clusteredSongs));
	}
	
	public void createCluster(String name, ArrayList<Song> clusteredSongs) {
		clusters.add(new Cluster(name, clusteredSongs));
	}
	
	public boolean removeCluster(String name) {
		for (Cluster cluster : clusters) {
			if(cluster.getName().equals(name)){
				mergeToDefaultCluster(cluster);
				return clusters.remove(cluster);
			}
		}
		return false;
	}

	private void mergeToDefaultCluster(Cluster cluster) {
		defaultCluster.add(cluster.getSongs());
	}
	
	public void mergeAllClustersToDefault() {
		for (Cluster cluster : clusters) {
			mergeToDefaultCluster(cluster);
			clusters.remove(cluster);
		}
	}

	public void add(Cluster cluster) {
		clusters.add(cluster);
		
	}
}

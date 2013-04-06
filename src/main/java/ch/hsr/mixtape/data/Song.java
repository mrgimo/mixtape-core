package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class Song {
	
	ArrayList<Feature> features = new ArrayList<Feature>();
	
	public ArrayList<Feature> getFeatures(){
		return features;
	}

	public double distanceTo(Song song) {
		return 0;
	}

}

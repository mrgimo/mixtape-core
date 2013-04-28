package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class FeatureVector {
	
	ArrayList<Feature> features = new ArrayList<Feature>();
	
	public void addFeature(Feature feature) {
		features.add(feature);
	}
	
	public void setFeatures(ArrayList<Feature> features) {
		this.features = features;
	}
	
	public ArrayList<Feature> getFeatures() {
		return features;
	}

	public int getDimension() {
		return features.size();
	}
}

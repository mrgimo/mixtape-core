package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class FeatureVector {
	
	ArrayList<Feature<?>> features = new ArrayList<Feature<?>>();
	
	public void addFeature(Feature<?> feature) {
		features.add(feature);
	}
	
	public double[] getFeatureValues() {
		double[] featureVector = new double[features.size()];
		
		for (int i = 0; i < features.size(); i++) {
			featureVector[i] = features.get(i).doubleValue();
		}
		return featureVector;
	}

	public void setFeatures(ArrayList<Feature<?>> features) {
		this.features = features;
	}
}

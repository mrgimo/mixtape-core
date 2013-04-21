package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class FeatureVector {
	
	ArrayList<SpectralFeature> features = new ArrayList<SpectralFeature>();
	
	public void addFeature(SpectralFeature feature) {
		features.add(feature);
	}
	
	public double[] getFeatureMeanValues() {
		double[] featureVector = new double[features.size()];
		
		for (int i = 0; i < features.size(); i++) {
			featureVector[i] = features.get(i).meanValue();
		}
		return featureVector;
	}

	public void setFeatures(ArrayList<SpectralFeature> features) {
		this.features = features;
	}
	
	public ArrayList<SpectralFeature> getFeatures() {
		return features;
	}

	public int getDimension() {
		return features.size();
	}
}

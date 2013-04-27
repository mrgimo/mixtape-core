package ch.hsr.mixtape.data;

import java.util.ArrayList;

public class FeatureVector {
	
	ArrayList<SpectralCentroidFeature> features = new ArrayList<SpectralCentroidFeature>();
	
	public void addFeature(SpectralCentroidFeature feature) {
		features.add(feature);
	}
	
	public double[] getFeatureMeanValues() {
		double[] featureVector = new double[features.size()];
		
		for (int i = 0; i < features.size(); i++) {
			featureVector[i] = features.get(i).meanValue();
		}
		return featureVector;
	}

	public void setFeatures(ArrayList<SpectralCentroidFeature> features) {
		this.features = features;
	}
	
	public ArrayList<SpectralCentroidFeature> getFeatures() {
		return features;
	}

	public int getDimension() {
		return features.size();
	}
}

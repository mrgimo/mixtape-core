package ch.hsr.mixtape.distancefunction;

import ch.hsr.mixtape.data.FeatureVector;

public interface DistanceFunction {
	
	public double computeDistance(FeatureVector featureVector1, FeatureVector featureVector2);
}

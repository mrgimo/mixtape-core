package ch.hsr.mixtape.distancefunction;

import ch.hsr.mixtape.data.FeatureVector;

public class Euclidean implements DistanceFunction {
	
	@Override
	public double computeDistance(FeatureVector featureVector1, FeatureVector featureVector2) {
		
		double[] valuesV1 = featureVector1.getFeatureMeanValues();
		double[] valuesV2 = featureVector2.getFeatureMeanValues();
		double distance = 0.0;
		
		if(equalDimension(valuesV1,valuesV2)){
			for (int i = 0; i < valuesV1.length; i++) 
				distance += (valuesV1[i] - valuesV2[i]) * (valuesV1[i] - valuesV2[i]);
		}
		return Math.sqrt(distance);
	}

	private boolean equalDimension(double[] featureVector1,
			double[] featureVector2) {
		return featureVector1.length == featureVector2.length;
	}

}

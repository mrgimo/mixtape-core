package ch.hsr.mixtape.distancefunction;

public class Euclidean implements DistanceFunction {
	
	@Override
	public double computeDistance(double[] featureVector1, double[] featureVector2) {
		double distance = 0.0;
		
		if(equalDimension(featureVector1, featureVector2)){
			for (int i = 0; i < featureVector1.length; i++) 
				distance += (featureVector1[i] - featureVector2[i]) * (featureVector1[i] - featureVector2[i]);
		}
		return Math.sqrt(distance);
	}

	private boolean equalDimension(double[] featureVector1,
			double[] featureVector2) {
		return featureVector1.length == featureVector2.length;
	}

}

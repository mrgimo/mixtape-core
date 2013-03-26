package ch.hsr.mixtape.features;

public class RelativeDifferenceFunction {

	private static final double THRESHOLD = 1E-50;

	public double extractFeature(double previousRootMeanSquare, double currentRootMeanSquare) {
		double difference = Math.abs(currentRootMeanSquare - previousRootMeanSquare);
		return Math.log(difference < THRESHOLD ? THRESHOLD : difference);
	}

}

package ch.hsr.mixtape.features;

public class StandardDeviation {

	public double extractFeature(double[] values) throws Exception {
		return getStandardDeviation(values);
	}

	private double getStandardDeviation(double[] values) {
		double average = getAverage(values);
		double sum = 0.0;
		for (int i = 0; i < values.length; i++) {
			double diff = values[i] - average;
			sum = sum + diff * diff;
		}

		return Math.sqrt(sum / ((double) (values.length - 1)));
	}

	private double getAverage(double[] values) {
		double sum = 0.0;
		for (int i = 0; i < values.length; i++)
			sum = sum + values[i];

		return (sum / ((double) values.length));
	}

}

package ch.hsr.mixtape.features;

public class StrongestFrequencyVariability {

	public double extractFeature(double[] strongestFrequenciesViaFftMaximum) {
		return getStandardDeviation(strongestFrequenciesViaFftMaximum);
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
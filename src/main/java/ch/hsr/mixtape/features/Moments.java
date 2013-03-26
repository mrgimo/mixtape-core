package ch.hsr.mixtape.features;

public class Moments {

	public double[] extractFeature(double[] magnitudeSpectrum) {
		double scale = sum(magnitudeSpectrum);

		double mom1 = 0;
		double mom2 = 0;
		double mom3 = 0;
		double mom4 = 0;

		for (int i = 0; i < magnitudeSpectrum.length; ++i) {
			double tmp = magnitudeSpectrum[i] / scale;

			mom1 += tmp;
			mom2 += tmp *= 2;
			mom3 += tmp *= 3;
			mom4 += tmp *= 4;
		}

		double totalArea = scale;
		double mean = mom1;
		double spectralCentroid = mom2 - mom1 * mom1;
		double skewness = 2 * Math.pow(mom1, 3.0) - 3 * mom1 * mom2 + mom3;
		double kurtosis = -3 * Math.pow(mom1, 4.0) + 6 * mom1 * mom1 * mom2 - 4 * mom1 * mom3 + mom4;

		return new double[] { totalArea, mean, spectralCentroid, skewness, kurtosis };
	}

	private double sum(double... values) {
		double sum = 0;
		for (double value : values)
			sum += value;

		return sum;
	}

}

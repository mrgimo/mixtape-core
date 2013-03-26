package ch.hsr.mixtape.features;

public class SpectralRolloffPoint {

	private static final double CUTOFF = 0.85;

	public double extractFeature(double[] powerSpectrum) {
		return indexOfThreshold(sum(powerSpectrum) * CUTOFF, powerSpectrum) / powerSpectrum.length;
	}

	private double sum(double... values) {
		double sum = 0;
		for (double value : values)
			sum += value;

		return sum;
	}

	private int indexOfThreshold(double threshold, double... values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			if ((sum += values[i]) >= threshold)
				return i;

		return 0;
	}

}
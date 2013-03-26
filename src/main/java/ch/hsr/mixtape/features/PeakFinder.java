package ch.hsr.mixtape.features;

import java.util.Arrays;

public class PeakFinder {

	private static final int PEAK_THRESHOLD = 10;

	public double[] extractFeature(double[] magnitudeSpectrum) {
		double threshold = max(magnitudeSpectrum) / PEAK_THRESHOLD;

		int index = 0;
		double[] values = new double[magnitudeSpectrum.length / 2];
		for (int i = 1; i < magnitudeSpectrum.length - 1; ++i) {
			double previous = magnitudeSpectrum[i - 1];
			double current = magnitudeSpectrum[i];
			double next = magnitudeSpectrum[i + 1];

			if ((previous < current) && (next < current) && (current > threshold))
				values[index++] = current;
		}

		return Arrays.copyOf(values, index);
	}

	private double max(double... values) {
		double max = Double.NEGATIVE_INFINITY;
		for (double value : values)
			if (max < value)
				max = value;

		return max;
	}

}

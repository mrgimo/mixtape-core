package ch.hsr.mixtape.features;

import java.util.Arrays;

public class FrequencyEstimator {

	public double estimateByZeroCrossings(double[] window, double sampleRate) {
		int[] indicesOfZeroCrossings = indicesOfZeroCrossings(window);

		double[] crossings = new double[indicesOfZeroCrossings.length];
		for (int i = 0; i < indicesOfZeroCrossings.length; i++) {
			int index = indicesOfZeroCrossings[i];
			crossings[i] = index - window[index] / (window[index + 1] - window[index]);
		}

		return sampleRate / mean(difference(crossings));
	}

	private double[] difference(double... values) {
		if (values.length < 2)
			return new double[] {};

		double[] differences = new double[values.length - 1];
		for (int i = 0; i < differences.length; i++)
			differences[i] = values[i + 1] - values[i];

		return differences;
	}

	private double mean(double... values) {
		double sum = 0;
		for (double value : values)
			sum += value;

		return sum / values.length;
	}

	private int[] indicesOfZeroCrossings(double[] window) {
		if (window.length < 2)
			return new int[] {};

		int[] indicesOfZeroCrossings = new int[window.length / 2];
		int numberOfZeroCrossings = 0;
		for (int index = 1; index < window.length; index++)
			if (window[index - 1] < 0 != window[index] > 0)
				indicesOfZeroCrossings[numberOfZeroCrossings++] = index;

		return Arrays.copyOf(indicesOfZeroCrossings, numberOfZeroCrossings);
	}

}
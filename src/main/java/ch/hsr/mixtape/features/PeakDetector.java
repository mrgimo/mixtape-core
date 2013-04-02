package ch.hsr.mixtape.features;

import java.util.Arrays;

public class PeakDetector {

	public int[] getIndicesOfPeaks(double[] values) {
		if (values.length < 2)
			return new int[] {};

		int[] indicesOfPeaks = new int[values.length / 2];
		int numberOfPeaks = 0;

		for (int index = 1; index < values.length; index++)
			if (values[index - 1] < values[index]  && values[index] > values[index + 1])
				indicesOfPeaks[numberOfPeaks++] = index;

		return Arrays.copyOf(indicesOfPeaks, numberOfPeaks);
	}

}
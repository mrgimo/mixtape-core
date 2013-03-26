package ch.hsr.mixtape.features;

public class StrongestBeat {

	public double extractFeature(double[] beatHistogram, double[] beatHistogramBinLabels) {
		int maxIndex = 0;
		for (int index = 1; index < beatHistogram.length; index++)
			if (beatHistogram[maxIndex] < beatHistogram[index])
				maxIndex = index;

		return beatHistogramBinLabels[maxIndex];
	}

}
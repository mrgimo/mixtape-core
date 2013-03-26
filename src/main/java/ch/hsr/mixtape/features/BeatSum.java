package ch.hsr.mixtape.features;

public class BeatSum {

	public double extractFeature(double[] beatHistogram) {
		double sum = 0.0;
		for (int i = 0; i < beatHistogram.length; i++)
			sum += beatHistogram[i];

		return sum;
	}

}
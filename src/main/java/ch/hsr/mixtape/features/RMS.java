package ch.hsr.mixtape.features;

public class RMS {

	public double extractFeature(double[] window) {
		double sum = 0.0;
		for (int samp = 0; samp < window.length; samp++)
			sum += window[samp] * window[samp];

		return Math.sqrt(sum / window.length);
	}

}
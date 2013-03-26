package ch.hsr.mixtape.features;

public class RMS {

	public double extractFeature(double[] samples) {
		double sum = 0.0;
		for (int samp = 0; samp < samples.length; samp++)
			sum += samples[samp] * samples[samp];

		return Math.sqrt(sum / samples.length);
	}

}
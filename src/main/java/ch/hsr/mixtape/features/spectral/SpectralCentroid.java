package ch.hsr.mixtape.features.spectral;

import org.apache.commons.math3.util.FastMath;

public class SpectralCentroid {

	public double extractFeature(double[] samples, double[] powerSpectrum) {
		double total = 0.0;
		double weightedTotal = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			weightedTotal += i * Math.sqrt(powerSpectrum[i] * powerSpectrum[i]);
			// take sqrt for better scaling with increased power
			total += FastMath.sqrt(powerSpectrum[i] * powerSpectrum[i]);
		}

		if (total != 0.0)
			return weightedTotal / total;
		else
			return 0.0;
	}

}
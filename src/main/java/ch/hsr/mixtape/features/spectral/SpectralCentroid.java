package ch.hsr.mixtape.features.spectral;

import ch.hsr.mixtape.MathUtils;

public class SpectralCentroid {

	public double extractFeature(double[] samples, double[] powerSpectrum) {
		double total = 0.0;
		double weightedTotal = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			weightedTotal += MathUtils.binToFrequency(i, 44100, powerSpectrum.length)
					* powerSpectrum[i];
			// maybe take sqrt for better scaling with increased power
			total += powerSpectrum[i];
		}

		return total != 0.0 ? weightedTotal / total : 0.0;
	}

}
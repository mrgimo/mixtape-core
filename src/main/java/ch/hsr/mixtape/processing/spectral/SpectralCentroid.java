package ch.hsr.mixtape.processing.spectral;

import static ch.hsr.mixtape.util.MathUtils.binToFrequency;

public class SpectralCentroid {

	public double extractFeature(double[] samples, double[] powerSpectrum, double totalPower) {
		double weightedTotal = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			weightedTotal += binToFrequency(i, 44100, powerSpectrum.length)
					* powerSpectrum[i];
			// maybe take sqrt for better scaling with increased power
			totalPower += powerSpectrum[i];
		}

		return totalPower != 0.0 ? weightedTotal / totalPower : 0.0;
	}

}
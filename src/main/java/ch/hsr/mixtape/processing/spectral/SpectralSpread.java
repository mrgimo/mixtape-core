package ch.hsr.mixtape.processing.spectral;

import static ch.hsr.mixtape.util.MathUtils.binToFrequency;

import org.apache.commons.math3.util.FastMath;

public class SpectralSpread {

	public double extractFeature(double[] powerSpectrum, double spectralCentroid, double totalPower) {
		double totalSpread = 0.0;

		if (totalPower != 0.0) {
			for (int i = 0; i < powerSpectrum.length; i++) {
				double spectralDeviation = binToFrequency(i, 44100, powerSpectrum.length) - spectralCentroid;
				totalSpread += spectralDeviation * spectralDeviation * powerSpectrum[i];
			}
		}

		return totalPower != 0.0 ? FastMath.sqrt(totalSpread / totalPower) : 0;
	}

}
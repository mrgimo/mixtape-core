package ch.hsr.mixtape.features.spectral;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.processing.MathUtils;

public class SpectralSpread {

	public double extractFeature(double[] powerSpectrum, double spectralCentroid) {
		double totalSpread = 0.0;
		double totalPower = MathUtils.sum(powerSpectrum);

		if (totalPower != 0.0) {
			for (int i = 0; i < powerSpectrum.length; i++) {
				double spectralDeviation = MathUtils.frequency(i, powerSpectrum.length) - spectralCentroid;
				totalSpread += spectralDeviation * spectralDeviation * powerSpectrum[i];
			}
		}

		return FastMath.sqrt(totalSpread / totalPower);
	}

}
package ch.hsr.mixtape.features.spectral;

import org.apache.commons.math3.util.FastMath;

public class SpectralSpread {

	public double extractFeature(double[] powerSpectrum, double spectralCentroid) {
		double totalSpread = 0.0;
		double totalPower = summatePower(powerSpectrum);

		if (totalPower != 0.0) {
			for (int i = 0; i < powerSpectrum.length; i++) {
				double spectralDeviation = frequency(i, powerSpectrum.length) - spectralCentroid;
				totalSpread += spectralDeviation * spectralDeviation * powerSpectrum[i];
			}
		}

		return FastMath.sqrt(totalSpread / totalPower);
	}

	private double frequency(int i, int length) {
		return (double)(i * 44100) / length ;
	}

	private double summatePower(double[] powerSpectrum) {
		double sum = 0.0;
		for (int i = 0; i < powerSpectrum.length; i++)
			sum += powerSpectrum[i];

		return sum;
	}

}
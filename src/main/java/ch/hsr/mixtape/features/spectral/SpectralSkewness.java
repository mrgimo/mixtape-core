package ch.hsr.mixtape.features.spectral;

import static ch.hsr.mixtape.MathUtils.binToFrequency;

/*
 * skewness = 0 -> symmetric
 * skewness < 0 -> more energy on the right
 * skewness > 0 -> more energy on the left
 */

public class SpectralSkewness {

	public double extractFeature(double[] powerSpectrum,
			double spectralCentroid, double spectralSpread, double totalPower) {

		double avgThirdOrderDeviation = totalPower != 0.0 ? summateThirdOrderMoments(powerSpectrum,
				spectralCentroid) / totalPower : 0;

		return calculateSkewness(avgThirdOrderDeviation, spectralSpread);
	}

	private double calculateSkewness(double avgThirdOrderMoment,
			double spectralSpread) {
		double thirdOrderSpectralSpread = spectralSpread * spectralSpread
				* spectralSpread;

		return thirdOrderSpectralSpread != 0.0 ? avgThirdOrderMoment
				/ thirdOrderSpectralSpread : 0.0;
	}

	private double summateThirdOrderMoments(double[] powerSpectrum,
			double spectralCentroid) {
		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			double centroidDeviation = binToFrequency(i, 44100, powerSpectrum.length)
					- spectralCentroid;
			sum += (centroidDeviation * centroidDeviation * centroidDeviation)
					* powerSpectrum[i];
		}

		return sum;
	}

}
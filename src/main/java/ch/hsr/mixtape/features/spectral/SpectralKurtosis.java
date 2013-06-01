package ch.hsr.mixtape.features.spectral;

import ch.hsr.mixtape.processing.MathUtils;

/*
 * measure for the flatness of a distribution around its mean value
 * k = 3 -> normal distribution
 * k < 3 -> flatter distribution
 * k > 3 -> peaker distribution
 */

public class SpectralKurtosis {

	public double extracFeature(double[] powerSpectrum,
			double spectralCentroid, double spectralSpread) {

		double avgFourthOrderDeviation = summateFourthOrderMoments(
				powerSpectrum, spectralCentroid) / MathUtils.sum(powerSpectrum);

		return calculateKurtosis(avgFourthOrderDeviation, spectralSpread);
	}

	private double calculateKurtosis(double avgFourthOrderMoment,
			double spectralSpread) {
		double fourthOrderspectralSpread = (spectralSpread * spectralSpread
				* spectralSpread * spectralSpread);

		return fourthOrderspectralSpread != 0.0 ? avgFourthOrderMoment
				/ fourthOrderspectralSpread : 0.0;
	}

	private double summateFourthOrderMoments(double[] powerSpectrum,
			double spectralCentroid) {
		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			double centroidDeviation = MathUtils.frequency(i, powerSpectrum.length)
					- spectralCentroid;
			double thirdOrderMoment = (centroidDeviation * centroidDeviation
					* centroidDeviation * centroidDeviation)
					* powerSpectrum[i];

			sum += thirdOrderMoment;
		}

		return sum;
	}

}
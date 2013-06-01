package ch.hsr.mixtape.features.spectral;

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
				powerSpectrum, spectralCentroid) / summatePower(powerSpectrum);

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
			double centroidDeviation = frequency(i, powerSpectrum.length)
					- spectralCentroid;
			double thirdOrderMoment = (centroidDeviation * centroidDeviation
					* centroidDeviation * centroidDeviation)
					* powerSpectrum[i];

			sum += thirdOrderMoment;
		}

		return sum;
	}

	private double summatePower(double[] powerSpectrum) {
		double sum = 0.0;
		for (int i = 0; i < powerSpectrum.length; i++)
			sum += powerSpectrum[i];

		return sum;
	}

	private double frequency(int i, int length) {
		return (double) (i * 44100) / length;
	}
}
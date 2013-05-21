package ch.hsr.mixtape.features.spectral;

/*
 * measure for the flatness of a distribution around its mean value
 * k = 3 -> normal distribution
 * k < 3 -> flatter distribution
 * k > 3 -> peaker distribution
 */

public class SpectralKurtosis {

	private double[] powerSpectrum;
	private double spectralCentroid;
	private double spectralSpread;

	public double extracFeature(double[] powerSpectrum,
			double spectralCentroid, double spectralSpread) {

		this.powerSpectrum = powerSpectrum;
		this.spectralCentroid = spectralCentroid;
		this.spectralSpread = spectralSpread;

		double avgFourthOrderMoments = summateFourthOrderMoments()
				/ powerSpectrum.length;

		return avgKurtosis(avgFourthOrderMoments);
	}

	private double avgKurtosis(double avgFourthOrderMoment) {
		double fourthOrderspectralSpread = (spectralSpread * spectralSpread
				* spectralSpread * spectralSpread);
		if (fourthOrderspectralSpread != 0.0)
			return avgFourthOrderMoment / fourthOrderspectralSpread;
		return 0.0;
	}

	private double summateFourthOrderMoments() {

		double totalPower = summatePower(powerSpectrum);
		double sum = 0.0;

		if (totalPower != 0.0) {
			for (int i = 0; i < powerSpectrum.length; i++) {
				double centroidDeviation = i - spectralCentroid;
				double thirdOrderMoment = (centroidDeviation
						* centroidDeviation * centroidDeviation * centroidDeviation)
						* powerSpectrum[i] / totalPower;
				sum += thirdOrderMoment;
			}
		}
		return sum;
	}

	private double summatePower(double[] powerSpectrum) {
		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			sum += powerSpectrum[i];
		}
		return sum;
	}
}

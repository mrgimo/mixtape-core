package ch.hsr.mixtape.features.spectral;

/*
 * skewness = 0 -> symmetric
 * skewness < 0 -> more energy on the right
 * skewness > 0 -> more energy on the left
 */

public class SpectralSkewness {

	private double[] powerSpectrum;
	private double spectralCentroid;
	private double spectralSpread;

	public double extractFeature(double[] powerSpectrum,
			double spectralCentroid, double spectralSpread) {
		this.powerSpectrum = powerSpectrum;
		this.spectralCentroid = spectralCentroid;
		this.spectralSpread = spectralSpread;

		double avgThirdOrderMoment = summateThirdOrderMoments()
				/ powerSpectrum.length;
		double avgSkewness = getAvgSkewness(avgThirdOrderMoment);

		return avgSkewness;
	}

	private double getAvgSkewness(double avgThirdOrderMoment) {
		
			double thirdOrderSpectralSpread = spectralSpread * spectralSpread * spectralSpread;
			if(thirdOrderSpectralSpread != 0.0)
			return avgThirdOrderMoment
					/ thirdOrderSpectralSpread;
			return 0.0;
	}

	private double summateThirdOrderMoments() {

		double totalPower = summatePower(powerSpectrum);
		double sum = 0.0;
		
		if(totalPower != 0.0 ) {
			for (int i = 0; i < powerSpectrum.length; i++) {
				double centroidDeviation = i - spectralCentroid;
				double thirdOrderMoment = (centroidDeviation * centroidDeviation * centroidDeviation)
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

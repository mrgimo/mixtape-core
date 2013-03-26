package ch.hsr.mixtape.features;

public class SpectralCentroid {

	public double extractFeature(double[] samples, double[] powerSpectrum) {
		double total = 0.0;
		double weightedTotal = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			weightedTotal += i * powerSpectrum[i];
			total += powerSpectrum[i];
		}

		if (total != 0.0) {
			return weightedTotal / total;
		} else {
			return 0.0;
		}
	}

}
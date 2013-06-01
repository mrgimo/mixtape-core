package ch.hsr.mixtape.features.spectral;


public class SpectralCentroid {

	public double extractFeature(double[] samples, double[] powerSpectrum) {
		double total = 0.0;
		double weightedTotal = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			weightedTotal += frequency(i, powerSpectrum.length) * powerSpectrum[i];
			// take sqrt for better scaling with increased power
			total += powerSpectrum[i];
		}

		if (total != 0.0)
			return weightedTotal / total;
		else
			return 0.0;
	}

	private double frequency(int i, int length) {
		return (double)(i* 44100) / length;
	}

}
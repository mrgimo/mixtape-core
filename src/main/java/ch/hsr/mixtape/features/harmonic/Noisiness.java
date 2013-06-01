package ch.hsr.mixtape.features.harmonic;

public class Noisiness {

	public double extract(int[] harmonics, double[] powerSpectrum) {

		double totalEnergy = summatePower(powerSpectrum);
		double noiseEngergy = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++)
			if (!isHarmonic(harmonics, i))
				noiseEngergy += powerSpectrum[i];

		return noiseEngergy / totalEnergy;
	}

	private boolean isHarmonic(int[] harmonics, int i) {

		for (int j = 0; j < harmonics.length; j++)
			if (harmonics[i] == i)
				return true;
		return false;
	}

	private double summatePower(double[] powerSpectrum) {
		double sum = 0.0;
		for (int i = 0; i < powerSpectrum.length; i++)
			sum += powerSpectrum[i];

		return sum;
	}

}

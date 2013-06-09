package ch.hsr.mixtape.processing.harmonic;

import ch.hsr.mixtape.util.MathUtils;

public class Noisiness {

	public double extract(int[] harmonics, double[] powerSpectrum) {

		double totalEnergy = MathUtils.sum(powerSpectrum);
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

}

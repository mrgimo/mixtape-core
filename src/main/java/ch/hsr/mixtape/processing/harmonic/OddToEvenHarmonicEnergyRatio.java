package ch.hsr.mixtape.processing.harmonic;

import static ch.hsr.mixtape.util.MathUtils.square;

public class OddToEvenHarmonicEnergyRatio {

	public double extract(double[] powerSpectrum, int[] harmonics) {
		double sumOfEvenHarmonics = 0.0;
		double sumOfOddHarmonics = 0.0;

		for (int i = 0; i < harmonics.length - 1; i += 2) {
			sumOfEvenHarmonics += square(powerSpectrum[harmonics[i]]);
			sumOfEvenHarmonics += square(powerSpectrum[harmonics[i + 1]]);
		}

		return sumOfEvenHarmonics != 0.0 ? sumOfOddHarmonics / sumOfEvenHarmonics : 0;
	}

}
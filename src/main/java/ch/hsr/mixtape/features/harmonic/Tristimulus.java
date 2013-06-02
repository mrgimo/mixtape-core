package ch.hsr.mixtape.features.harmonic;

public class Tristimulus {

	private static final int TRISTIMULUS_COUNT = 3;

	public double[] extract(double[] powerSpectrum, int[] binsOfHarmonics) {
		double[] tristimulus = new double[TRISTIMULUS_COUNT];

		double sumOfHarmonics = sum(powerSpectrum, binsOfHarmonics, 0, binsOfHarmonics.length);
		if (binsOfHarmonics.length > 0)
			tristimulus[0] = sum(powerSpectrum, binsOfHarmonics, 0, 1) / sumOfHarmonics;

		if (binsOfHarmonics.length > 3)
			tristimulus[1] = sum(powerSpectrum, binsOfHarmonics, 1, 4) / sumOfHarmonics;

		if (binsOfHarmonics.length > 4)
			tristimulus[2] = sum(powerSpectrum, binsOfHarmonics, 4, binsOfHarmonics.length) / sumOfHarmonics;

		return tristimulus;
	}

	private double sum(double[] powerSpectrum, int[] binsOfHarmonics, int from, int to) {
		double sum = 0;
		for (int i = from; i < to; i++)
			sum += powerSpectrum[binsOfHarmonics[i]];

		return sum;
	}

}
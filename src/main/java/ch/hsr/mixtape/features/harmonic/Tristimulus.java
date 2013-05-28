package ch.hsr.mixtape.features.harmonic;

public class Tristimulus {

	private static final int TRISTIMULUS_COUNT = 3;

	public double[] extractFeature(double[] harmonics) {
		double[] tristimulus = new double[TRISTIMULUS_COUNT];

		double sumHarmonics = summateHarmonics(harmonics, 0, harmonics.length);
		if (harmonics.length > 1)
			tristimulus[0] = summateHarmonics(harmonics, 0, 1) / sumHarmonics;

		if (harmonics.length > 4) {
			tristimulus[1] = summateHarmonics(harmonics, 1, 4) / sumHarmonics;
			tristimulus[2] = summateHarmonics(harmonics, 4, harmonics.length) / sumHarmonics;
		}

		return tristimulus;
	}

	private double summateHarmonics(double[] harmonics, int from, int to) {
		double sum = 0.0;
		for (int i = from; i < to; i++)
			sum += harmonics[i];

		return sum;
	}

}
package ch.hsr.mixtape.features.harmonic;

import static ch.hsr.mixtape.MathUtils.argMax;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

public class Harmonics {

	private static final int MAX_DEVIATION_IN_BINS = 10;

	public int[] extract(double[] frequencySpectrum, int binOfFundamental) {
		int numberOfHarmonics = frequencySpectrum.length / binOfFundamental;
		int[] binsOfHarmonics = new int[numberOfHarmonics];

		for (int i = 0; i < numberOfHarmonics; i++) {
			int binOfHarmonic = (i + 2) * binOfFundamental;

			int from = max(binOfHarmonic - MAX_DEVIATION_IN_BINS, 0);
			int to = min(binOfHarmonic + MAX_DEVIATION_IN_BINS, frequencySpectrum.length);

			int argMax = argMax(frequencySpectrum, from, to);

			binsOfHarmonics[i] = (frequencySpectrum[argMax] == frequencySpectrum[from]
					&& frequencySpectrum[argMax] == frequencySpectrum[to])
					? binOfHarmonic
					: argMax;
		}

		return binsOfHarmonics;
	}

}
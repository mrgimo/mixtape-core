package ch.hsr.mixtape.processing.harmonic;

import static ch.hsr.mixtape.util.MathUtils.*;

public class Inharmonicity {

	public double extract(double[] powerSpectrum, int binOfFundamental, int[] binsOfHarmonics) {
		double totalEnergy = sumOfSquares(powerSpectrum);

		double weightedInharmony = 0.0;
		for (int i = 0; i < binsOfHarmonics.length; i++) {
			int binOfHarmonic = binsOfHarmonics[i];
			int deviationInBins = deviationInBins(binOfFundamental, binOfHarmonic);

			weightedInharmony += deviationInBins * square(powerSpectrum[binOfHarmonic]);
		}

		return totalEnergy != 0.0 ? (2 * weightedInharmony) / (binOfFundamental * totalEnergy) : 0;
	}

	private int deviationInBins(int binOfFundamental, int binOfHarmonic) {
		int mod = binOfHarmonic % binOfFundamental;
		if (mod > binOfFundamental / 2)
			return mod - binOfFundamental / 2;
		else
			return mod;
	}

}
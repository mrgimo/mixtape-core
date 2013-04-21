package ch.hsr.mixtape.features;

public class Inharmonicity {

	public double extractFeature(double[] powerSpectrum,
			int fundamentalFrequency, int[] harmonics) {

		double totalEnergy = summateEnergy(powerSpectrum);

		double weightedInharmonie = 0.0;
		for (int i = 0; i < harmonics.length; i++) {
			weightedInharmonie = frequencyDeviation(fundamentalFrequency,
					harmonics, i)
					* powerSpectrum[harmonics[i]]
					* powerSpectrum[harmonics[i]];
		}
		
		//TODO: can fundamental frequency be 0 && formel sais 2 * weightedInharmonie, why? :)?
		return totalEnergy != 0.0  && fundamentalFrequency != 0 ? 2 * weightedInharmonie / fundamentalFrequency * totalEnergy : 0;
	}

	private int frequencyDeviation(int fundamentalFrequency, int[] harmonics,
			int i) {
		int deviation = harmonics[i] - harmonics[i] * fundamentalFrequency;
		return (int) Math.sqrt(deviation * deviation);
	}

	private double summateEnergy(double[] powerSpectrum) {

		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			sum += powerSpectrum[i] * powerSpectrum[i];
		}
		return sum;
	}

}

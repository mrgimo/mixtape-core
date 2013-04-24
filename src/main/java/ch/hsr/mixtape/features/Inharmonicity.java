package ch.hsr.mixtape.features;

public class Inharmonicity {

	private static final int SAMPLING_RATE = 44100;

	public double extractFeature(double[] powerSpectrum,
			double fundamentalFrequency, int[] harmonics) {

		double totalEnergy = summateEnergy(powerSpectrum);

		double weightedInharmonie = 0.0;
		for (int i = 0; i < harmonics.length; i++) {
			weightedInharmonie += frequencyDeviation(fundamentalFrequency,
					harmonics[i], powerSpectrum.length)
					* powerSpectrum[harmonics[i]]
					* powerSpectrum[harmonics[i]];
		}
		
		//TODO: formel sais 2 * weightedInharmonie, why? :)?
		return totalEnergy != 0.0  ? (2 * weightedInharmonie) / (fundamentalFrequency * totalEnergy) : 0;
	}

	private double frequencyDeviation(double fundamentalFrequency, int indexOfHarmonic, int lengthOfPowerSpectrum) {
		double frequencyMulitplicator = SAMPLING_RATE / lengthOfPowerSpectrum;
		
		double deviation = indexOfHarmonic * frequencyMulitplicator - indexOfHarmonic * fundamentalFrequency;
		return Math.sqrt(deviation * deviation);
	}

	private double summateEnergy(double[] powerSpectrum) {

		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			sum += powerSpectrum[i] * powerSpectrum[i];
		}
		return sum;
	}

}

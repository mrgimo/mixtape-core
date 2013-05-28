package ch.hsr.mixtape.features.harmonic;

public class Inharmonicity {

	private double sampleRate;

	public Inharmonicity(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double extract(double[] frequencySpectrum, int[] fundamentals, int[] harmonics) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double extract(double[] powerSpectrum, double fundamentalFrequency, int[] harmonics) {
		double totalEnergy = summateEnergy(powerSpectrum);
		double weightedInharmony = 0.0;
		for (int i = 0; i < harmonics.length; i++) {
			weightedInharmony += frequencyDeviation(fundamentalFrequency, harmonics[i], powerSpectrum.length)
					* powerSpectrum[harmonics[i]]
					* powerSpectrum[harmonics[i]];
		}

		// TODO: formula says 2 * weightedInharmony, why? :)?
		return totalEnergy != 0.0 ? (2 * weightedInharmony) / (fundamentalFrequency * totalEnergy) : 0;
	}

	private double frequencyDeviation(double fundamentalFrequency, int indexOfHarmonic, int lengthOfPowerSpectrum) {
		double frequencyMulitplicator = sampleRate / lengthOfPowerSpectrum;
		double deviation = indexOfHarmonic * frequencyMulitplicator - indexOfHarmonic * fundamentalFrequency;

		return Math.sqrt(deviation * deviation);
	}

	private double summateEnergy(double[] powerSpectrum) {
		double sum = 0.0;
		for (int i = 0; i < powerSpectrum.length; i++)
			sum += powerSpectrum[i] * powerSpectrum[i];

		return sum;
	}

}
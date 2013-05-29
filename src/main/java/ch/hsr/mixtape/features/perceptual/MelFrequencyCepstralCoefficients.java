package ch.hsr.mixtape.features.perceptual;

public class MelFrequencyCepstralCoefficients {

	private final static int numberOfFilters = 23;
	private final static int numberOfCoefficientsPerFrame = 13;

	private final static double LOWER_LIMIT_OF_FILTER = 133.3334;
	private final static double FLOOR = -50;

	private double sampleRate;

	public MelFrequencyCepstralCoefficients(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double[] extractFeature(double[] samples, double[] magnitudeSpectrum) {
		int[] fftBinIndices = calculateFftBinIndices(sampleRate, magnitudeSpectrum.length);

		double[] melFilter = melFilter(magnitudeSpectrum, fftBinIndices);
		double[] nonLinearTransformation = nonLinearTransformation(melFilter);
		double[] cepstralCoefficientsCoefficients = calculateCepstralCoefficients(nonLinearTransformation);

		return cepstralCoefficientsCoefficients;
	}

	private int[] calculateFftBinIndices(double samplingRate, int frameSize) {
		int[] indices = new int[numberOfFilters + 2];

		indices[0] = (int) Math.round(LOWER_LIMIT_OF_FILTER / samplingRate * frameSize);
		indices[indices.length - 1] = (int) (frameSize / 2);

		for (int i = 1; i <= numberOfFilters; i++)
			indices[i] = (int) Math.round(calculateCenterFreuency(i, samplingRate) / samplingRate * frameSize);

		return indices;
	}

	private double calculateCenterFreuency(int indexOfMelFilters, double samplingRate) {
		double mel0 = convertFrequencyToMelFrequency(LOWER_LIMIT_OF_FILTER);
		double mel1 = convertFrequencyToMelFrequency(samplingRate / 2);

		return calculateInverseMelFrequency(mel0 + ((mel1 - mel0) / (numberOfFilters + 1)) * indexOfMelFilters);
	}

	private double convertFrequencyToMelFrequency(double frequency) {
		return 2595 * Math.log10(1 + frequency / 700);
	}

	private double calculateInverseMelFrequency(double x) {
		return 700 * (Math.pow(10, x / 2595) - 1);
	}

	private double[] melFilter(double bin[], int fftBinIndices[]) {
		double[] temp = new double[numberOfFilters + 2];
		for (int k = 1; k <= numberOfFilters; k++) {
			double num1 = 0;
			double num2 = 0;

			for (int i = fftBinIndices[k - 1]; i <= fftBinIndices[k]; i++)
				num1 += ((i - fftBinIndices[k - 1] + 1) / (fftBinIndices[k] - fftBinIndices[k - 1] + 1)) * bin[i];

			for (int i = fftBinIndices[k] + 1; i <= fftBinIndices[k + 1]; i++)
				num2 += (1 - ((i - fftBinIndices[k]) / (fftBinIndices[k + 1] - fftBinIndices[k] + 1))) * bin[i];

			temp[k] = num1 + num2;
		}

		double filterBank[] = new double[numberOfFilters];
		for (int i = 0; i < numberOfFilters; i++)
			filterBank[i] = temp[i + 1];

		return filterBank;
	}

	private double[] nonLinearTransformation(double[] filterBank) {
		double[] f = new double[filterBank.length];
		for (int i = 0; i < filterBank.length; i++) {
			f[i] = Math.log(filterBank[i]);
			if (f[i] < FLOOR)
				f[i] = FLOOR;
		}

		return f;
	}

	private double[] calculateCepstralCoefficients(double f[]) {
		double[] coefficients = new double[numberOfCoefficientsPerFrame];
		for (int i = 0; i < coefficients.length; i++)
			for (int j = 1; j <= numberOfFilters; j++)
				coefficients[i] += f[j - 1] * Math.cos(Math.PI * i / numberOfFilters * (j - 0.5));

		return coefficients;
	}

}
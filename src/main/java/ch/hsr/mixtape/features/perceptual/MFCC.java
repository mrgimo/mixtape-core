package ch.hsr.mixtape.features.perceptual;

public class MFCC {

	private final static int numberOfMelFilters = 23;
	private final static int numberOfMFCCsPerFrame = 13;

	private final static double LOWER_LIMIT_OF_FILTER = 133.3334;
	private final static double FLOOR = -50;

	public double[] extractFeature(double[] samples, double sampling_rate, double[] magnitudeSpectrum) throws Exception {
		int[] fftBinIndices = calculateFftBinIndices(sampling_rate, magnitudeSpectrum.length);

		double[] melFilter = melFilter(magnitudeSpectrum, fftBinIndices);
		double[] nonLinearTransformation = nonLinearTransformation(melFilter);
		double[] cepstralCoefficientsCoefficients = calculateCepstralCoefficients(nonLinearTransformation);

		return cepstralCoefficientsCoefficients;
	}

	private int[] calculateFftBinIndices(double samplingRate, int frameSize) {
		int[] indices = new int[numberOfMelFilters + 2];

		indices[0] = (int) Math.round(LOWER_LIMIT_OF_FILTER / samplingRate * frameSize);
		indices[indices.length - 1] = (int) (frameSize / 2);

		for (int i = 1; i <= numberOfMelFilters; i++)
			indices[i] = (int) Math.round(calculateCenterFreuency(i, samplingRate) / samplingRate * frameSize);

		return indices;
	}

	private double calculateCenterFreuency(int indexOfMelFilters, double samplingRate) {
		double mel0 = convertFrequencyToMelFrequency(LOWER_LIMIT_OF_FILTER);
		double mel1 = convertFrequencyToMelFrequency(samplingRate / 2);

		// take inverse mel of:
		double temp = mel0 + ((mel1 - mel0) / (numberOfMelFilters + 1)) * indexOfMelFilters;
		return calculateInverseMelFrequency(temp);
	}

	private double convertFrequencyToMelFrequency(double frequency) {
		return 2595 * Math.log10(1 + frequency / 700);
	}

	private double calculateInverseMelFrequency(double x) {
		double temp = Math.pow(10, x / 2595) - 1;
		return 700 * (temp);
	}

	private double[] melFilter(double bin[], int fftBinIndices[]) {
		double temp[] = new double[numberOfMelFilters + 2];

		for (int k = 1; k <= numberOfMelFilters; k++) {
			double num1 = 0, num2 = 0;

			for (int i = fftBinIndices[k - 1]; i <= fftBinIndices[k]; i++) {
				num1 += ((i - fftBinIndices[k - 1] + 1) / (fftBinIndices[k] - fftBinIndices[k - 1] + 1)) * bin[i];
			}

			for (int i = fftBinIndices[k] + 1; i <= fftBinIndices[k + 1]; i++) {
				num2 += (1 - ((i - fftBinIndices[k]) / (fftBinIndices[k + 1] - fftBinIndices[k] + 1))) * bin[i];
			}

			temp[k] = num1 + num2;
		}

		double fbank[] = new double[numberOfMelFilters];
		for (int i = 0; i < numberOfMelFilters; i++) {
			fbank[i] = temp[i + 1];
		}

		return fbank;
	}

	private double[] nonLinearTransformation(double fbank[]) {
		double f[] = new double[fbank.length];

		for (int i = 0; i < fbank.length; i++) {
			f[i] = Math.log(fbank[i]);
			if (f[i] < FLOOR)
				f[i] = FLOOR;
		}

		return f;
	}

	private double[] calculateCepstralCoefficients(double f[]) {
		double cepc[] = new double[numberOfMFCCsPerFrame];

		for (int i = 0; i < cepc.length; i++) {
			for (int j = 1; j <= numberOfMelFilters; j++) {
				cepc[i] += f[j - 1] * Math.cos(Math.PI * i / numberOfMelFilters * (j - 0.5));
			}
		}

		return cepc;
	}

}

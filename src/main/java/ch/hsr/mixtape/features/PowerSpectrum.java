package ch.hsr.mixtape.features;

public class PowerSpectrum {

	public double[] extractFeature(double[] realNumbers, double[] imaginaryNumbers) {
		int numberOfUnfoldedBins = imaginaryNumbers.length / 2;
		double[] powerSpectrum = new double[numberOfUnfoldedBins];
		for (int i = 0; i < powerSpectrum.length; i++)
			powerSpectrum[i] = (realNumbers[i] * realNumbers[i] + imaginaryNumbers[i] * imaginaryNumbers[i])
					/ realNumbers.length;

		return powerSpectrum;
	}

}

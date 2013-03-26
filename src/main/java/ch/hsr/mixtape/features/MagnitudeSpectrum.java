package ch.hsr.mixtape.features;

public class MagnitudeSpectrum {

	public double[] extractFeature(double[] realNumbers, double[] imaginaryNumbers) {
		int number_unfolded_bins = imaginaryNumbers.length / 2;
		double[] output_magnitude = new double[number_unfolded_bins];
		for (int i = 0; i < output_magnitude.length; i++)
			output_magnitude[i] = (Math.sqrt(realNumbers[i] * realNumbers[i] + imaginaryNumbers[i]
					* imaginaryNumbers[i]))
					/ realNumbers.length;

		return output_magnitude;
	}

}

package ch.hsr.mixtape.features;

public class FFTBinFrequencies {
	public double[] extractFeature(double[] samples, double samplingRate) {
		int fftSize = ensureIsPowerOfN(samples.length, 2);

		int number_bins = fftSize;
		double bin_width = samplingRate / (double) number_bins;
		double offset = bin_width / 2.0;

		int numberUnfoldedBins = fftSize / 2;
		double[] labels = new double[numberUnfoldedBins];
		for (int bin = 0; bin < labels.length; bin++)
			labels[bin] = (bin * bin_width) + offset;

		return labels;
	}

	private int ensureIsPowerOfN(int x, int n) {
		double log_value = logBaseN((double) x, (double) n);
		int log_int = (int) log_value;
		int valid_size = pow(n, log_int);
		if (valid_size != x)
			valid_size = pow(n, log_int + 1);
		return valid_size;
	}

	private double logBaseN(double x, double n) {
		return (Math.log10(x) / Math.log10(n));
	}

	private int pow(int a, int b) {
		int result = a;
		for (int i = 1; i < b; i++)
			result *= a;
		return result;
	}

}

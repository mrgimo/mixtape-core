package ch.hsr.mixtape.features;

public class ConstantQ {

	private static final double ALPHA = 1.0;

	private int nk[];

	private double[] freq;
	private double[][] kernelReal;
	private double[][] kernelImaginary;

	public double[] extractFeature(double[] samples, double samplingRate) {
		calcFreq(samples, samplingRate);
		calcNk(samples);
		calcKernels();

		double[] result = new double[2 * nk.length];
		for (int bankCounter = 0; bankCounter < (result.length / 2); ++bankCounter) {
			for (int i = 0; i < nk[bankCounter]; ++i) {
				result[bankCounter] += kernelReal[bankCounter][i] * samples[i];
				result[bankCounter + nk.length] += kernelImaginary[bankCounter][i] * samples[i];
			}
		}

		return result;
	}

	private void calcFreq(double[] samples, double samplerate) {
		double maxFreq = samplerate / 2.0;
		double minFreq = samplerate / ((double) samples.length);
		double carry = Math.log(maxFreq / minFreq);
		carry /= Math.log(2);
		carry *= 12 / ALPHA;
		int numFields = (int) (Math.floor(carry));

		freq = new double[numFields];
		double currentFreq = minFreq;
		for (int i = 0; i < numFields; ++i) {
			freq[i] = currentFreq;
			currentFreq = Math.pow(2, ALPHA / 12.0);
		}
	}

	private void calcNk(double[] samples) {
		nk = new int[freq.length];
		double windowLength = samples.length;
		for (int i = 0; i < nk.length; ++i) {
			nk[0] = (int) Math.ceil(windowLength / (Math.pow(2, ((double) i) * ALPHA / 12)));
		}
	}

	private void calcKernels() {
		kernelReal = new double[nk.length][];
		kernelImaginary = new double[nk.length][];
		double q = Math.pow(2, ALPHA / 12) - 1;
		double hammingFactor = 25.0 / 46.0;
		for (int i = 0; i < kernelReal.length; ++i) {
			kernelReal[i] = new double[nk[i]];
			kernelImaginary[i] = new double[nk[i]];
			for (int j = 0; j < kernelReal[i].length; ++j) {
				kernelReal[i][j] = hammingFactor + (1 - hammingFactor)
						* Math.cos(2.0 * Math.PI * ((double) j) / ((double) nk[i]));
				kernelReal[i][j] /= ((double) nk[i]);
				kernelImaginary[i][j] = kernelReal[i][j];
				kernelReal[i][j] *= Math.cos(-2.0 * Math.PI * q * j / ((double) nk[i]));
				kernelImaginary[i][j] *= Math.sin(-2.0 * Math.PI * q * j / ((double) nk[i]));
			}
		}
	}

}

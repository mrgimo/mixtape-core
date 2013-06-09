package ch.hsr.mixtape.processing.harmonic;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.util.MathUtils;

public class FundamentalFrequencies {

	private static final int PEAK_BANDWIDTH_IN_BINS = 16;

	private int windowSize;
	private int spectrumSize;

	private double[][] candidates;

	public FundamentalFrequencies(int windowSize) {
		this.windowSize = windowSize;
		spectrumSize = MathUtils.spectrumSize(windowSize);

		candidates = initCandidates();
	}

	private double[][] initCandidates() {
		double[] gaussianFunction = gaussianFunction();

		double[][] candidates = new double[88][spectrumSize];
		for (int key = 0; key < candidates.length; key++)
			candidates[key] = fundamental(pianoKeyToBin(key), gaussianFunction);

		return candidates;
	}

	private int pianoKeyToBin(double key) {
		return MathUtils.frequencyToBin(pianoKeyToFrequency(key), 44100, windowSize);
	}

	private double pianoKeyToFrequency(double key) {
		if (key < 0)
			return 0;

		return Math.pow(2, (key - 48.0) / 12.0) * 440;
	}

	private double[] fundamental(int bin, double[] gaussianFunction) {
		double[] fundamental = new double[spectrumSize];

		int length;
		int sourceBegin;
		int destinationBegin;

		if (PEAK_BANDWIDTH_IN_BINS > bin) {
			length = bin;
			sourceBegin = (PEAK_BANDWIDTH_IN_BINS - bin) / 2;
			destinationBegin = bin / 2;
		} else {
			length = PEAK_BANDWIDTH_IN_BINS;
			sourceBegin = 0;
			destinationBegin = bin - (PEAK_BANDWIDTH_IN_BINS / 2);
		}

		while (destinationBegin < spectrumSize) {
			int rest = spectrumSize - destinationBegin;
			System.arraycopy(gaussianFunction, sourceBegin, fundamental, destinationBegin, rest < length ? rest
					: length);
			destinationBegin += bin;
		}

		return fundamental;
	}

	private double[] gaussianFunction() {
		double[] gaussian = new double[PEAK_BANDWIDTH_IN_BINS];

		double sigma = PEAK_BANDWIDTH_IN_BINS * 0.125;
		double halfBandwith = PEAK_BANDWIDTH_IN_BINS * 0.5;
		double factor = -1.0 / (2.0 * sigma * sigma);

		int middle = (int) halfBandwith;
		for (int x = 0; x < halfBandwith; x++)
			gaussian[middle - x] = gaussian[middle + x] = FastMath.exp(x * x * factor);

		return gaussian;
	}

	public int[] extract(double[] frequencySpectrum) {
		int[] fundamentals = new int[88];
		int numberOfFundamentals = 0;

		double[] fundamental = new double[spectrumSize];

		double fitness = 0;
		double fitnessGain = 1;
		while (fitnessGain > 0.05 * fitness) {
			double[] fitnesses = new double[88];
			for (int i = 0; i < fitnesses.length; i++)
				fitnesses[i] = fitness(eachMax(fundamental, candidates[i]),
						frequencySpectrum);

			int argMax = argMax(fitnesses);
			fundamentals[numberOfFundamentals++] = pianoKeyToBin(argMax);

			fitness += fitnesses[argMax];
			fitnessGain = fitnesses[argMax];

			fundamental = eachMax(fundamental, candidates[argMax]);
		}

		return Arrays.copyOf(fundamentals, numberOfFundamentals);
	}

	private int argMax(double[] f) {
		int argMax = 0;
		for (int i = 1; i < f.length; i++)
			if (f[argMax] < f[i])
				argMax = i;

		return argMax;
	}

	private double[] eachMax(double[] a, double[] b) {
		double[] max = new double[a.length];
		for (int i = 0; i < max.length; i++)
			max[i] = a[i] > b[i] ? a[i] : b[i];

		return max;
	}

	private double fitness(double[] candidate, double[] spectrum) {
		double a = 0;
		double b = 0;

		for (int i = 0; i < spectrum.length; i++) {
			a += candidate[i] * spectrum[i];
			b += candidate[i];
		}

		return a / b;
	}

}
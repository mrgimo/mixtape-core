package ch.hsr.mixtape.features.harmonic;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import java.math.RoundingMode;
import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import com.google.common.math.DoubleMath;

public class FundamentalFrequencies {

	private static final int PEAK_BANDWIDTH_IN_BINS = 16;

	private double sampleRate;

	private int windowSize;
	private int spectrumSize;

	private double[][] candidates;

	private double[] gaussianFunction;
	private double[] hannWindow;

	public FundamentalFrequencies(double sampleRate, int windowSize) {
		this.sampleRate = sampleRate;
		this.windowSize = windowSize;
		spectrumSize = windowSize / 2;

		gaussianFunction = initGaussianFunction();
		candidates = initCandidates();
		hannWindow = initHannWindow();
	}

	private double[][] initCandidates() {
		double[][] candidates = new double[88][spectrumSize];
		for (int key = 0; key < candidates.length; key++)
			candidates[key] = fundamental(frequencyToBin(pianoKeyToFrequency(key)));

		return candidates;
	}

	private int frequencyToBin(double frequency) {
		return DoubleMath.roundToInt(frequency * windowSize / sampleRate,
				RoundingMode.HALF_UP);
	}

	private double pianoKeyToFrequency(double key) {
		if (key < 0)
			return 0;

		return Math.pow(2, (key - 48.0) / 12.0) * 440;
	}

	private double[] fundamental(int bin) {
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
			System.arraycopy(gaussianFunction, sourceBegin, fundamental, destinationBegin,
					rest < length ? rest : length);
			destinationBegin += bin;
		}

		return fundamental;
	}

	private double[] initGaussianFunction() {
		double[] gaussian = new double[PEAK_BANDWIDTH_IN_BINS];

		double sigma = PEAK_BANDWIDTH_IN_BINS * 0.125;
		double halfBandwith = PEAK_BANDWIDTH_IN_BINS * 0.5;
		double factor = -1.0 / (2.0 * sigma * sigma);

		int middle = (int) halfBandwith;
		for (int x = 0; x < halfBandwith; x++)
			gaussian[middle - x] = gaussian[middle + x] = FastMath.exp(x * x
					* factor);

		return gaussian;
	}

	private double[] initHannWindow() {
		double[] hannWindow = new double[windowSize];
		for (int i = 0; i < windowSize; i++)
			hannWindow[i] = 0.5 * (1 - cos((2 * PI * i) / (windowSize - 1)));

		return hannWindow;
	}

	public int[] extract(double[] window) {
		double[] frequencySpectrum = frequencySpectrum(window);

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
			fundamentals[numberOfFundamentals++] = argMax;

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

	private double[] frequencySpectrum(double[] samples) {
		return fftToFrequencySpectrum(fft(hannWindow(samples)));
	}

	public static Complex[] fft(double... f) {
		return new FastFourierTransformer(DftNormalization.STANDARD).transform(
				f, TransformType.FORWARD);
	}

	private double[] fftToFrequencySpectrum(Complex[] fft) {
		double[] frequencySpectrum = new double[spectrumSize];
		for (int i = 0; i < spectrumSize; i++)
			frequencySpectrum[i] = fft[i].abs();

		return frequencySpectrum;
	}

	private double[] hannWindow(double[] samples) {
		double[] window = new double[windowSize];
		for (int i = 0; i < windowSize; i++)
			window[i] = samples[i] * hannWindow[i];

		return window;
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
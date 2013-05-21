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

public class PitchDetection {

	private static final double SAMPLE_RATE = 44100;

	private static final int WINDOW_SIZE = 4096;
	private static final int SPECTRUM_SIZE = WINDOW_SIZE / 2;

	private static final int PEAK_BANDWIDTH_IN_BINS = 16;

	private double[][] candidates;
	private double[] gaussianFunction;
	private double[] hannWindow;

	public PitchDetection() {
		gaussianFunction = initGaussianFunction();
		candidates = initCandidates();
		hannWindow = initHannWindow(WINDOW_SIZE);
	}

	private double[][] initCandidates() {
		double[][] candidates = new double[88][SPECTRUM_SIZE];
		for (int key = 0; key < candidates.length; key++)
			candidates[key] = fundamental(frequencyToBin(pianoKeyToFrequency(key)));

		return candidates;
	}

	private int frequencyToBin(double frequency) {
		return DoubleMath.roundToInt(frequency * WINDOW_SIZE / SAMPLE_RATE,
				RoundingMode.HALF_UP);
	}

	private double pianoKeyToFrequency(double key) {
		if (key < 0)
			return 0;

		return Math.pow(2, (key - 48.0) / 12.0) * 440;
	}

	private double[] fundamental(int bin) {
		double[] fundamental = new double[SPECTRUM_SIZE];

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

		while (destinationBegin < SPECTRUM_SIZE) {
			int rest = SPECTRUM_SIZE - destinationBegin;
			System.arraycopy(gaussianFunction, sourceBegin, fundamental,
					destinationBegin, rest < length ? rest : length);
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

	private double[] initHannWindow(int n) {
		double[] hannWindow = new double[n];
		for (int i = 0; i < n; i++)
			hannWindow[i] = 0.5 * (1 - cos((2 * PI * i) / (n - 1)));

		return hannWindow;
	}

	public int[] fundamentals(double[] samples) {
		double[] frequencySpectrum = frequencySpectrum(samples);

		int[] fundamentals = new int[88];
		int numberOfFundamentals = 0;

		double[] fundamental = new double[SPECTRUM_SIZE];

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
		double[] frequencySpectrum = new double[SPECTRUM_SIZE];
		for (int i = 0; i < SPECTRUM_SIZE; i++)
			frequencySpectrum[i] = fft[i].abs();

		return frequencySpectrum;
	}

	private double[] hannWindow(double[] samples) {
		double[] window = new double[WINDOW_SIZE];
		for (int i = 0; i < WINDOW_SIZE; i++)
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

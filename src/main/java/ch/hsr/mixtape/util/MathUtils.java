package ch.hsr.mixtape.util;

import static com.google.common.math.DoubleMath.roundToInt;
import static org.apache.commons.math3.transform.DftNormalization.STANDARD;
import static org.apache.commons.math3.transform.TransformType.FORWARD;
import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.math.RoundingMode;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;

public class MathUtils {

	private static final FastFourierTransformer fft = new FastFourierTransformer(STANDARD);

	public static double[] powerSpectrum(double[] samples) {
		return square(frequencySpectrum(samples));
	}
	
	public static double[] frequencySpectrum(double[] samples) {
		return toFrequencySpectrum(fft(hannWindow(samples)));
	}
	
	public static double binToFrequency(int bin, double sampleRate, int windowSize) {
		return bin * sampleRate / windowSize;
	}

	public static int frequencyToBin(double frequency, double sampleRate, int windowSize) {
		return roundToInt(frequency * windowSize / sampleRate, RoundingMode.HALF_UP);
	}

	public static Complex[] fft(double... samples) {
		return fft.transform(samples, FORWARD);
	}

	private static double[] toFrequencySpectrum(Complex[] fft) {
		double[] frequencySpectrum = new double[fft.length >> 1];
		for (int i = 0; i < frequencySpectrum.length; i++)
			frequencySpectrum[i] = fft[i].abs();

		return frequencySpectrum;
	}

	private static double[] hannWindow(double[] samples) {
		double[] window = new double[samples.length];
		for (int i = 0; i < samples.length; i++)
			window[i] = samples[i] * 0.5 * (1 - cos((2 * PI * i) / (samples.length - 1)));

		return window;
	}

	public static double[] multiply(double[] a, double[] b) {
		if (a.length != b.length)
			throw new IllegalArgumentException();

		double[] c = new double[a.length];
		for (int i = 0; i < c.length; i++)
			c[i] = a[i] * b[i];

		return c;
	}

	public static double vectorLength(double... values) {
		return sqrt(sumOfSquares(values));
	}

	public static double sumOfSquares(double... values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += square(values[i]);

		return sum;
	}

	public static double square(double value) {
		return value * value;
	}

	public static double[] square(double[] f) {
		return multiply(f, f);
	}

	public static double sum(double... values) {
		return sum(values, 0, values.length);
	}

	public static double sum(double[] values, int from, int to) {
		double sum = 0;
		for (int i = from; i < to; i++)
			sum += values[i];

		return sum;
	}

	public static int spectrumSize(int windowSize) {
		return windowSize / 2;
	}

	public static int argMax(double... values) {
		return argMax(values, 0, values.length);
	}

	public static int argMax(double[] values, int from, int to) {
		int argMax = from;
		for (int i = from + 1; i < to; i++)
			if (values[argMax] < values[i])
				argMax = i;

		return argMax;
	}

	public static double limit(double value, double min, double max) {
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}

}
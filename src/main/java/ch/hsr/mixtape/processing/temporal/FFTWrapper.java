package ch.hsr.mixtape.processing.temporal;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.ArithmeticUtils;

/**
 * Wrapper class for accessing Apache Commons FastFourierTransformer for usage
 * in an Aubio Library context.
 * 
 * @author Stefan Derungs
 */
public class FFTWrapper {

	private FastFourierTransformer fft;

	public FFTWrapper() {
		fft = new FastFourierTransformer(DftNormalization.STANDARD);
	}

	/**
	 * Compute forward FFT.
	 * 
	 * @param input
	 *            input signal
	 * @return output spectrum
	 */
	public double[][] forward(double[] input) {
		if (!ArithmeticUtils.isPowerOfTwo(input.length)) {
			double[] newData = new double[MathUtils
					.ensureIsPowerOfTwo(input.length)];
			System.arraycopy(input, 0, newData, 0, input.length);
			input = newData;
		}
		Complex[] transformed = fft.transform(input, TransformType.FORWARD);
		return getSpectrum(extractImaginaryPart(transformed));
	}

	private double[] extractImaginaryPart(Complex[] c) {
		double[] imaginaries = new double[c.length];
		for (int i = 0; i < c.length; i++)
			imaginaries[i] = c[i].getImaginary();
		return imaginaries;
	}

	/**
	 * Convert real/imag spectrum to norm/phas spectrum.
	 * 
	 * @param complexSpectrum
	 *            real/imag input fft array
	 * @return spectrum norm/phas output array
	 */
	private double[][] getSpectrum(double[] complexSpectrum) {
		double[][] spectrum = new double[2][];
		spectrum[0] = getPhase(complexSpectrum);
		spectrum[1] = getNorm(complexSpectrum);
		return spectrum;
	}

	/**
	 * Compute phas spectrum from real/imag parts.
	 * 
	 * @param complexSpectrum
	 *            real/imag input fft array
	 * @return spectrum phas output array
	 */
	private double[] getPhase(double[] complexSpectrum) {
		double[] spectrum = new double[complexSpectrum.length];
		if (complexSpectrum[0] < 0)
			spectrum[0] = Math.PI;
		else
			spectrum[0] = 0.;

		for (int i = 1; i < spectrum.length - 1; i++) {
			spectrum[i] = Math.atan2(
					complexSpectrum[complexSpectrum.length - i],
					complexSpectrum[i]);
		}

		if (complexSpectrum[complexSpectrum.length / 2] < 0)
			spectrum[spectrum.length - 1] = Math.PI;
		else
			spectrum[spectrum.length - 1] = 0.;

		return spectrum;
	}

	/**
	 * Compute norm component from real/imag parts.
	 * 
	 * @param complexSpectrum
	 *            real/imag input fft array
	 * @return Spectrum norm output array.
	 */
	private double[] getNorm(double[] complexSpectrum) {
		double[] spectrum = new double[complexSpectrum.length];
		spectrum[0] = Math.abs(complexSpectrum[0]);

		for (int i = 1; i < spectrum.length - 1; i++)
			spectrum[i] = Math.sqrt(complexSpectrum[i] * complexSpectrum[i]
					+ complexSpectrum[complexSpectrum.length - i]
					* complexSpectrum[complexSpectrum.length - i]);

		spectrum[spectrum.length - 1] = Math
				.abs(complexSpectrum[complexSpectrum.length / 2]);

		return spectrum;
	}
}

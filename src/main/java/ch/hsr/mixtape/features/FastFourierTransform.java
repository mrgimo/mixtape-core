package ch.hsr.mixtape.features;

public class FastFourierTransform {

	private double[] realNumbers;
	private double[] imaginaryNumbers;

	public FastFourierTransform(double[] window) {
		int size = ensureIsPowerOfN(window.length, 2);
		realNumbers = new double[size];
		System.arraycopy(window, 0, realNumbers, 0, size);

		imaginaryNumbers = new double[size];

		for (int i = 0; i < realNumbers.length; i++) {
			double hanning = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / size);
			realNumbers[i] *= hanning;
		}

		int j = 0;
		for (int i = 0; i < size; ++i) {
			if (j >= i) {
				double tempr = realNumbers[j] * 1.0;
				double tempi = imaginaryNumbers[j] * 1.0;
				realNumbers[j] = realNumbers[i] * 1.0;
				imaginaryNumbers[j] = imaginaryNumbers[i] * 1.0;
				realNumbers[i] = tempr;
				imaginaryNumbers[i] = tempi;
			}
			int m = size / 2;
			while (m >= 1 && j >= m) {
				j -= m;
				m /= 2;
			}
			j += m;
		}

		// Perform the spectral recombination stage by stage
		int max_spectra_for_stage;
		int step_size;
		for (max_spectra_for_stage = 1, step_size = 2 * max_spectra_for_stage; max_spectra_for_stage < size; max_spectra_for_stage = step_size, step_size = 2 * max_spectra_for_stage) {
			double delta_angle = 1 * Math.PI / max_spectra_for_stage;

			// Loop once for each individual spectra
			for (int spectra_count = 0; spectra_count < max_spectra_for_stage; ++spectra_count) {
				double angle = spectra_count * delta_angle;
				double real_correction = Math.cos(angle);
				double imag_correction = Math.sin(angle);

				int right = 0;
				for (int left = spectra_count; left < size; left += step_size) {
					right = left + max_spectra_for_stage;
					double temp_real = real_correction * realNumbers[right] - imag_correction * imaginaryNumbers[right];
					double temp_imag = real_correction * imaginaryNumbers[right] + imag_correction * realNumbers[right];
					realNumbers[right] = realNumbers[left] - temp_real;
					imaginaryNumbers[right] = imaginaryNumbers[left] - temp_imag;
					realNumbers[left] += temp_real;
					imaginaryNumbers[left] += temp_imag;
				}
			}
			max_spectra_for_stage = step_size;
		}
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

	public double[] getRealValues() {
		return realNumbers;
	}

	public double[] getImaginaryValues() {
		return imaginaryNumbers;
	}

}
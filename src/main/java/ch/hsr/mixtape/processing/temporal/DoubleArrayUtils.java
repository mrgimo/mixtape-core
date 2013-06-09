package ch.hsr.mixtape.processing.temporal;

/**
 * This class contains some helper methods for double array manipulation that
 * are spread all over the Aubio Library.
 * 
 * For further details see (list not complete): fvec.c, mathutils.c,
 * peackpicker.c
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class DoubleArrayUtils {

	/**
	 * Set all elements to ones.
	 */
	public static void resetWithOnes(double[] data) {
		for (int i = 0; i < data.length; i++)
			data[i] = 1;
	}

	/**
	 * Revert order of vector elements.
	 */
	public static void revert(double[] data) {
		double t;
		for (int i = 0; i < Math.floor(data.length / 2.); i++) {
			t = data[i];
			data[i] = data[data.length - 1 - i];
			data[data.length - 1 - i] = t;
		}
	}

	/**
	 * Compute the mean of a vector.
	 */
	public static double getMean(double[] data) {
		double sum = 0.0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum / (double) data.length;
	}

	public static double getMinElement(double[] data) {
		double tmp = data[0];
		for (int i = 0; i < data.length; i++)
			tmp = tmp < data[i] ? tmp : data[i];
		return tmp;
	}

	public static double getMaxElement(double[] data) {
		double tmp = 0.0;
		for (int i = 0; i < data.length; i++)
			tmp = tmp > data[i] ? tmp : data[i];
		return tmp;
	}

	public static int getMaxElementPosition(double[] data) {
		int pos = 0;
		double tmp = 0.0;
		for (int i = 0; i < data.length; i++) {
			pos = tmp > data[i] ? pos : i;
			tmp = tmp > data[i] ? tmp : data[i];
		}
		return pos;
	}

	/**
	 * Apply weight to vector.
	 * 
	 * If the weight vector is longer than s, only the first elements are used.
	 * If the weight vector is shorter than s, the last elements of s are not
	 * weighted.
	 * 
	 * @param data
	 *            Vector to apply the weight.
	 * @param weight
	 *            Weighting coefficients.
	 */
	public static void weight(double[] data, double[] weight) {
		for (int j = 0; j < Math.min(data.length, weight.length); j++)
			data[j] *= weight[j];
	}

	/**
	 * Swap the left and right halves of the vector.
	 * 
	 * This function swaps the left part of the signal with the right part of
	 * the signal.
	 * 
	 * This operation, known as 'fftshift' in the Matlab Signal Processing
	 * Toolbox, can be used before computing the FFT to simplify the phase
	 * relationship of the resulting spectrum.
	 */
	public static void shift(double[] data) {
		for (int j = 0; j < data.length / 2; j++) {
			double t = data[j];
			data[j] = data[j + data.length / 2];
			data[j + data.length / 2] = t;
		}
	}

	/**
	 * Returns the median of a vector.
	 */
	public static double getMedianThreshold(double[] input) {
		int low = 0;
		int high = input.length - 1;
		int median = (low + high) / 2;
		int middle, ll, hh;

		while (true) {
			if (high <= low) /* One element only */
				return input[median];

			if (high == low + 1) { /* Two elements only */
				if (input[low] > input[high]) {
					double t = input[low];
					input[low] = input[high];
					input[high] = t;
				}
				return input[median];
			}

			/* Find median of low, middle and high items; swap into position low */
			middle = (low + high) / 2;
			if (input[middle] > input[high]) {
				double t = input[middle];
				input[middle] = input[high];
				input[high] = t;
			}
			if (input[low] > input[high]) {
				double t = input[low];
				input[low] = input[high];
				input[high] = t;
			}
			if (input[middle] > input[low]) {
				double t = input[middle];
				input[middle] = input[low];
				input[low] = t;
			}

			/* Swap low item (now in position middle) into position (low+1) */
			double t = input[middle];
			input[middle] = input[low + 1];
			input[low + 1] = t;

			/* Nibble from each end towards middle, swapping items when stuck */
			ll = low + 1;
			hh = high;
			while (true) {
				do
					ll++;
				while (input[low] > input[ll]);
				do
					hh--;
				while (input[hh] > input[low]);

				if (hh < ll)
					break;

				// swap
				t = input[ll];
				input[ll] = input[hh];
				input[hh] = t;
			}

			/* Swap middle item (in position low) back into correct position */
			t = input[low];
			input[low] = input[hh];
			input[hh] = t;

			/* Re-set active partition */
			if (hh <= median)
				low = ll;
			if (hh >= median)
				high = hh - 1;
		}
	}
}
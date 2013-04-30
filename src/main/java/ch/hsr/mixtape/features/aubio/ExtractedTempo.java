package ch.hsr.mixtape.features.aubio;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.features.aubio.SpectralDescription.SpectralDescriptionType;

/**
 * This class is a wrapper object for an extracted tempo.
 * 
 * @author Stefan Derungs
 */
public class ExtractedTempo {

	private SpectralDescriptionType type;

	private double[] bpms;

	private double[] confidences;

	public ExtractedTempo(SpectralDescriptionType type, double[] bpms,
			double[] confidences) {
		this.type = type;
		this.bpms = bpms;
		this.confidences = confidences;

		// Pre-sort arrays for later median calculation.
		Arrays.sort(this.bpms);
		Arrays.sort(this.confidences);
	}

	public SpectralDescriptionType getType() {
		return type;
	}

	public void printBeat(int j) {
		System.out.println(bpms[j]);
	}

	/**
	 * @return The ArrayList contains beats rounded to the next integer value.
	 */
	public ArrayList<Double> getRoundedBeatCollection(boolean discardZeroValues) {
		ArrayList<Double> beats = new ArrayList<Double>();
		for (int i = 0; i < bpms.length; i++) {
			if (!discardZeroValues || bpms[i] > 0)
				beats.add((double) Math.round(bpms[i]));
		}
		return beats;
	}

	/**
	 * @return The number of elements in the BPM array.
	 */
	public int getLength() {
		return bpms.length;
	}

	public double getMedianBpm(boolean discardZeroValues) {
		return getMedian(bpms, discardZeroValues, 0, bpms.length);
	}

	/**
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	public double getMedianOfBpmInterval(boolean discardZeroValues,
			int intStart, int intEnd) {
		return getMedian(bpms, discardZeroValues, intStart, intEnd);
	}

	public double getMeanBpm(boolean discardZeroValues) {
		return getMean(bpms, discardZeroValues, 0, bpms.length);
	}

	/**
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	public double getMeanOfBpmInterval(boolean discardZeroValues, int intStart,
			int intEnd) {
		return getMean(bpms, discardZeroValues, intStart, intEnd);
	}

	/**
	 * The unit of the standard deviation is bpm.
	 */
	public double getStandardDeviationBpm(boolean discardZeroValues) {
		return getStandardDeviation(bpms, discardZeroValues, 0, bpms.length);
	}

	/**
	 * The unit of the standard deviation is bpm.
	 * 
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	public double getStandardDeviationOfBpmInterval(boolean discardZeroValues,
			int intStart, int intEnd) {
		return getStandardDeviation(bpms, discardZeroValues, intStart, intEnd);
	}

	public double getMedianConfidence(boolean discardZeroValues) {
		return getMedian(confidences, discardZeroValues, 0, confidences.length);
	}

	/**
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	public double getMedianOfConfidenceInterval(boolean discardZeroValues,
			int intStart, int intEnd) {
		return getMedian(confidences, discardZeroValues, intStart, intEnd);
	}

	public double getMeanConfidence(boolean discardZeroValues) {
		return getMean(confidences, discardZeroValues, 0, confidences.length);
	}

	/**
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	public double getMeanOfConfidenceInterval(boolean discardZeroValues,
			int intStart, int intEnd) {
		return getMean(confidences, discardZeroValues, intStart, intEnd);
	}

	private double[] removeZeroValues(double[] array) {
		ArrayList<Double> tmp = new ArrayList<Double>();

		for (int i = 0; i < array.length; i++)
			if (array[i] > 1 && array[i] != Double.NaN)
				tmp.add(array[i]);

		double[] out = new double[tmp.size()];
		for (int i = 0; i < tmp.size(); i++)
			out[i] = tmp.get(i);
		return out;
	}

	/**
	 * Calculates the median value in a defined interval.
	 * 
	 * @param array
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 * @return
	 */
	private double getMedian(double[] array, boolean discardZeroValues,
			int intStart, int intEnd) {
		if (discardZeroValues) {
			array = removeZeroValues(array);
			if (array.length == 0)
				return 0.0;
			if (intStart > array.length)
				return 0.0;
			else if (intEnd > array.length)
				intEnd = array.length;
		}

		int length = intEnd - intStart;
		return length % 2 == 0 ? (array[intStart + length / 2] + array[intStart
				+ length / 2 - 1]) / 2. : array[intStart + (length - 1) / 2];
	}

	/**
	 * @param array
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	private double getMean(double[] array, boolean discardZeroValues,
			int intStart, int intEnd) {
		double sum = 0;
		int k = 0;
		for (int i = intStart; i < intEnd; i++) {
			if (!discardZeroValues && array[i] != Double.NaN) {
				sum += array[i];
			} else if (array[i] > 0 && array[i] != Double.NaN) {
				sum += array[i];
				k++;
			}
		}
		return sum == 0 ? 0.0 : sum
				/ (double) (discardZeroValues ? k : intEnd - intStart);
	}

	/**
	 * For the expected value (estimator) the mean is taken.
	 * 
	 * @param array
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	private double getStandardDeviation(double[] array,
			boolean discardZeroValues, int intStart, int intEnd) {
		double estimator = getMean(array, discardZeroValues, intStart, intEnd);
		double variance = getVariance(array, discardZeroValues, estimator,
				intStart, intEnd);
		return Math.sqrt(variance);
	}

	/**
	 * 
	 * @param array
	 * @param discardZeroValues
	 *            If true, only non-zero values are taken into account.
	 * @param intStart
	 *            The interval start position. This position is included in the
	 *            calculation.
	 * @param intEnd
	 *            The interval end position. This is the first element that will
	 *            be excluded in the calculation.
	 */
	private double getVariance(double[] array, boolean discardZeroValues,
			double estimator, int intStart, int intEnd) {
		double sum = 0;
		int k = 0;
		for (int i = intStart; i < intEnd; i++) {
			if (!discardZeroValues && array[i] != Double.NaN)
				sum += (array[i] - estimator) * (array[i] - estimator);
			else if (array[i] > 0 && array[i] != Double.NaN) {
				sum += (array[i] - estimator) * (array[i] - estimator);
				k++;
			}
		}

		return sum
				/ ((double) (discardZeroValues ? k : intEnd - intStart) - 1.);
	}

}

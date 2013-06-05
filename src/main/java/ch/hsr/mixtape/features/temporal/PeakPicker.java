package ch.hsr.mixtape.features.temporal;

/**
 * This class provides peak picking utilities.
 * 
 * <p>
 * <b>This class was ported from aubio's `peakpicker.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class PeakPicker {

	/**
	 * Offset threshold [0.1; 0.033; 0.01; 0.0668; 0.33; 0.082;].
	 * <br>
	 * Value range: 0.0 to 1.0
	 * <br>
	 * Aubio's default: 0.3
	 */
	double threshold = 0.3;

	/**
	 * Median filter window length (causal part) [8].
	 */
	int windowPost = 5;

	/**
	 * Median filter window (anti-causal part) [post-1].
	 */
	int windowPre = 1;

	/**
	 * Biquad lowpass filter.
	 */
	Filter biquad;

	/**
	 * Original onsets.
	 */
	double[] onsetKeep;

	/**
	 * Modified onsets.
	 */
	double[] onsetProc;

	/**
	 * Peak picked window [3].
	 */
	double[] onsetPeek;

	/**
	 * Thresholded function.
	 */
	double[] thresholded;

	/**
	 * Scratch pad for biquad and median.
	 */
	double[] scratch;

	public PeakPicker() {
		scratch = new double[windowPost + windowPre + 1];
		onsetKeep = new double[windowPost + windowPre + 1];
		onsetProc = new double[windowPost + windowPre + 1];
		onsetPeek = new double[3];
		thresholded = new double[1];

		/*
		 * cutoff: low-pass filter with cutoff reduced frequency at 0.34
		 * generated with octave butter function: [b,a] = butter(2, 0.34);
		 */
		try {
			biquad = BiquadFilter.aubio_filter_set_biquad(0.15998789,
					0.31997577, 0.15998789, -0.59488894, 0.23484048);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Real time peak picking function.
	 * 
	 * Modified version for real time, moving mean adaptive threshold this
	 * method is slightly more permissive than the offline one, and yields to an
	 * increase of false positives. best
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_peakpicker_do
	 * </p>
	 */
	public double[] pickPeak(double[] onset) {
		double[] out = new double[1];
		double mean = 0., median = 0.;
		/* store onset in onset_keep */
		/* shift all elements but last, then write last */
		for (int j = 0; j < windowPost + windowPre + 1 - 1; j++) {
			onsetKeep[j] = onsetKeep[j + 1];
			onsetProc[j] = onsetKeep[j];
		}
		onsetKeep[windowPost + windowPre] = onset[0];
		onsetProc[windowPost + windowPre] = onset[0];

		/* filter onsetProc */
		/** \bug filtfilt calculated post+pre times, should be only once !? */
		onsetProc = biquad.doubleFilter(onsetProc, scratch);

		/* calculate mean and median for onsetProc */
		mean = DoubleArrayUtils.getMean(onsetProc);
		/* copy to scratch */
		for (int j = 0; j < windowPost + windowPre + 1; j++)
			scratch[j] = onsetProc[j];
		median = DoubleArrayUtils.getMedianThreshold(scratch);

		/* shift peek array */
		for (int j = 0; j < 2; j++)
			onsetPeek[j] = onsetPeek[j + 1];
		
		/* calculate new thresholded value */
		thresholded[0] = onsetProc[windowPost] - median - mean * threshold;
		onsetPeek[2] = thresholded[0];
		out[0] = peakPicker(onsetPeek);
		if (out[0] > 0)
			out[0] = MathUtils.quadraticInterpolation(onsetPeek, 1);
		return out;
	}

	/**
	 * This method returns the current value in the peak picking buffer after
	 * smoothing.
	 */
	public double[] getThresholdedInput() {
		return thresholded;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Peak picker function.
	 * 
	 * @param onset_peek
	 *            input vector
	 * @return 1 if onset_peek[1] is a peak and positive, 0 otherwise.
	 */
	private int peakPicker(double[] onset_peek) {
		return (onset_peek[1] > onset_peek[0] && onset_peek[1] > onset_peek[2] && onset_peek[1] > 0.) ? 1
				: 0;
	}

}

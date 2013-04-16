package ch.hsr.mixtape.features.aubio;

/**
 * Computes a histogram.
 * 
 * <p>
 * <b>This class was ported from aubio's `hist.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class Histogram {

	double[] histogram;

	int nelems;

	double[] cent;

	Scaler scaler;

	/**
	 * @param flow
	 *            minimum input
	 * @param fhig
	 *            maximum input
	 * @param nelems
	 *            number of histogram columns
	 */
	public Histogram(double ilow, double ihig, int nelems) {
		double step = (ihig - ilow) / (double) (nelems);
		double accum = step;
		int i;
		this.nelems = nelems;
		histogram = new double[nelems];
		cent = new double[nelems];

		/* use scale to map ilow/ihig . 0/nelems */
		scaler = new Scaler(ilow, ihig, 0, nelems);
		/* calculate centers now once */
		cent[0] = ilow + 0.5 * step;
		for (i = 1; i < nelems; i++, accum += step)
			cent[i] = cent[0] + accum;
	}

	/**
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_hist_do
	 * </p>
	 */
	public void computeHistogram(double[] input) {
		int tmp = 0;
		scaler.scale(input);
		histogram = new double[histogram.length];
		/* run accum */
		for (int i = 0; i < input.length; i++) {
			tmp = (int) Math.floor(input[i]);
			if ((tmp >= 0) && (tmp < (int) nelems))
				histogram[tmp] += 1;
		}
	}

	/**
	 * Compute dynamic histogram for non-null elements.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_hist_dyn_notnull
	 * </p>
	 */
	public void computeDynamicHistogram(double[] input) {
		int tmp = 0;
		double ilow = DoubleArrayUtils.getMinElement(input);
		double ihig = DoubleArrayUtils.getMaxElement(input);
		double step = (ihig - ilow) / (double) (nelems);

		/* readapt */
		scaler.setLimits(ilow, ihig, 0, nelems);

		/* recalculate centers */
		cent[0] = ilow + 0.5 * step;
		for (int i = 1; i < nelems; i++)
			cent[i] = cent[0] + i * step;

		/* scale */
		scaler.scale(input);

		/* reset data */
		histogram = new double[histogram.length];
		/* run accum */
		for (int i = 0; i < input.length; i++) {
			if (input[i] != 0) {
				tmp = (int) Math.floor(input[i]);
				if ((tmp >= 0) && (tmp < (int) nelems))
					histogram[tmp] += 1;
			}
		}
	}

	/**
	 * Weight the histogram.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_hist_weight
	 * </p>
	 */
	public void applyWeight() {
		for (int i = 0; i < nelems; i++)
			histogram[i] *= cent[i];
	}

	/**
	 * Compute the mean of the histogram.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_hist_mean
	 * </p>
	 */
	public double mean() {
		double tmp = 0.0;
		for (int i = 0; i < nelems; i++)
			tmp += histogram[i];
		return tmp / (double) (nelems);
	}

}

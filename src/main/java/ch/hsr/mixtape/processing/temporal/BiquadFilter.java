package ch.hsr.mixtape.processing.temporal;

/**
 * This file implements a normalised biquad filter (Second order Infinite
 * Impulse Response filter).
 * 
 * <p>
 * <b>This class was ported from aubio's `biquad.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class BiquadFilter {

	/**
	 * Set coefficients of a biquad filter.
	 * 
	 * @param b0
	 *            forward filter coefficient
	 * @param b1
	 *            forward filter coefficient
	 * @param b2
	 *            forward filter coefficient
	 * @param a1
	 *            feedback filter coefficient
	 * @param a2
	 *            feedback filter coefficient
	 * @throws Exception
	 */
	public static Filter aubio_filter_set_biquad(double b0, double b1,
			double b2, double a1, double a2) throws Exception {
		Filter filter = new Filter(3);
		int order = filter.getOrder();
		double[] bs = filter.getFeedforward();
		double[] as = filter.getFeedback();

		if (order != 3)
			throw new Exception("Order of biquad filter must be 3, not "
					+ order + "!");

		bs[0] = b0;
		bs[1] = b1;
		bs[2] = b2;
		as[0] = 1.;
		as[1] = a1;
		as[1] = a2;

		return filter;
	}

}

package ch.hsr.mixtape.features.aubio;

/**
 * Digital filter. This object stores a digital filter of order \f$n\f$.
 * 
 * It contains the following data:
 * <ul>
 * <li>n*1 b_i feedforward coefficients</li>
 * <li>n*1 a_i feedback coefficients</li>
 * <li>n*c x_i input signal</li>
 * <li>n*c y_i output signal</li>
 * </ul>
 * 
 * Feedforward and feedback parameters can be modified using
 * aubio_filter_get_feedback() and aubio_filter_get_feedforward().
 * 
 * The function aubio_filter_do_outplace() computes the following output signal
 * y[n] from the input signal x[n] \f$:
 * 
 * \f{eqnarray*}{ y[n] = b_0 x[n] & + & b_1 x[n-1] + b_2 x[n-2] + ... + b_P
 * x[n-P] \\ & - & a_1 y[n-1] - a_2 y[n-2] - ... - a_P y[n-P] \\ \f}
 * 
 * The function aubio_filter_do() executes the same computation but modifies
 * directly the input signal (in-place).
 * 
 * The function aubio_filter_do_filtfilt() version runs the filter twice, first
 * forward then backward, to compensate with the phase shifting of the forward
 * operation.
 * 
 * Some convenience functions are provided:
 * <ul>
 * <li>new_aubio_filter_a_weighting() and aubio_filter_set_a_weighting()</li>
 * <li>new_aubio_filter_c_weighting() and aubio_filter_set_c_weighting()</li>
 * <li>new_aubio_filter_biquad() and aubio_filter_set_biquad()</li>
 * </ul>
 * 
 * <p>
 * <b>This class was ported from aubio's `filter.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class Filter {

	int order;

	double[] a;

	double[] b;

	double[] y;

	double[] x;

	public Filter(int order) {
		x = new double[order];
		y = new double[order];
		a = new double[order];
		b = new double[order];
		this.order = order;
		/* set default to identity */
		a[1] = 1.;
	}

	/**
	 * Filter input vector (in-place).
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_filter_do
	 * </p>
	 * 
	 * @param in
	 *            input vector to filter
	 */
	public void filter(double[] in) {
		for (int j = 0; j < in.length; j++) {
			/* new input */
			x[0] = Math.abs(in[j]) < 2.e-42 ? 0. : in[j];
			y[0] = b[0] * x[0];
			for (int l = 1; l < order; l++) {
				y[0] += b[l] * x[l];
				y[0] -= a[l] * y[l];
			}
			/* new output */
			in[j] = y[0];
			/* store for next sample */
			for (int l = order - 1; l > 0; l--) {
				x[l] = x[l - 1];
				y[l] = y[l - 1];
			}
		}
	}

	/**
	 * Filter input vector forward and backward.
	 * 
	 * The rough way: reset memory of filter between each run to avoid end
	 * effects.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_filter_do_filtfilt
	 * </p>
	 * 
	 * @param in
	 *            input vector to filter
	 * @param tmp
	 *            memory space to use for computation
	 */
	public double[] doubleFilter(double[] in, double[] tmp) {
		/* apply filtering */
		filter(in);
		reset();
		/* mirror */
		for (int j = 0; j < in.length; j++)
			tmp[in.length - j - 1] = in[j];
		/* apply filtering on mirrored */
		filter(tmp);
		reset();
		/* invert back */
		for (int j = 0; j < in.length; j++)
			in[j] = tmp[in.length - j - 1];
		return in;
	}

	public double[] getFeedback() {
		return a;
	}

	public double[] getFeedforward() {
		return b;
	}

	public int getOrder() {
		return order;
	}

	public void reset() {
		x = new double[x.length];
		y = new double[y.length];
	}

}

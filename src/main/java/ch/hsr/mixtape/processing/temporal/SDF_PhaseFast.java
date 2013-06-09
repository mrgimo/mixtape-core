package ch.hsr.mixtape.processing.temporal;

/**
 * Phase Based Method onset detection function.
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_PhaseFast implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		sd.dev1[0] = 0.;
		for (int i = 0; i < fftgrain[0].length; i++) {
			sd.dev1[i] = aubio_unwrap2pi(fftgrain[0][i] - 2.0 * sd.theta1[i]
					+ sd.theta2[i]);
			if (sd.threshold < fftgrain[1][i])
				sd.dev1[i] = Math.abs(sd.dev1[i]);
			else
				sd.dev1[i] = 0.0;
			/* keep a track of the past frames */
			sd.theta2[i] = sd.theta1[i];
			sd.theta1[i] = fftgrain[0][i];
		}
		/* apply o.histogram */
		sd.histogram.computeDynamicHistogram(sd.dev1);
		/* weight it */
		sd.histogram.applyWeight();
		/* its mean is the result */
		onset[0] = sd.histogram.mean();
		// onset.data[0] = fvec_mean(o.dev1);
		return onset;
	}

	/**
	 * Compute the principal argument
	 * 
	 * This function maps the input phase to its corresponding value wrapped in
	 * the range \f$ [-\pi, \pi] \f$.
	 * 
	 * @param phase
	 *            unwrapped phase to map to the unit circle
	 * @return equivalent phase wrapped to the unit circle
	 */
	public static double aubio_unwrap2pi(double phase) {
		return phase + 2 * Math.PI
				* (1. + Math.floor(-(phase + Math.PI) / (2 * Math.PI)));
	}

}

package ch.hsr.mixtape.features.aubio;

/**
 * Complex Domain Method onset detection function.
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_ComplexDomain implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++) {
			// compute the predicted phase
			sd.dev1[i] = 2. * sd.theta1[i] - sd.theta2[i];
			// compute the euclidean distance in the complex domain
			// sqrt ( r_1^2 + r_2^2 - 2 * r_1 * r_2 * \cos ( \phi_1 - \phi_2 ) )
			onset[0] += Math.sqrt(Math.abs(sd.oldMagnitude[i] * sd.oldMagnitude[i]
					+ fftgrain[1][i] * fftgrain[1][i] - 2. * sd.oldMagnitude[i]
					* fftgrain[1][i] * Math.cos(sd.dev1[i] - fftgrain[0][i])));
			/* swap old phase data (need to remember 2 frames behind) */
			sd.theta2[i] = sd.theta1[i];
			sd.theta1[i] = fftgrain[0][i];
			/* swap old magnitude data (1 frame is enough) */
			sd.oldMagnitude[i] = fftgrain[1][i];
		}
		return onset;
	}

}

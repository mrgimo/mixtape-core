package ch.hsr.mixtape.features.aubio;

/**
 * Modified Kullback Liebler onset detection function.
 * 
 * <p>
 * NOTE: we use ln(1+Xn/(Xn-1+0.0001)) to avoid negative (1.+) and infinite
 * values (+1.e-10)
 * </p>
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_ModifiedKullbackLiebler implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++) {
			onset[0] += Math.log(1. + fftgrain[1][i] / (sd.oldMagnitude[i] + 1.e-1));
			sd.oldMagnitude[i] = fftgrain[1][i];
		}
		if (Double.isNaN(onset[0]))
			onset[0] = 0.;
		return onset;
	}

}

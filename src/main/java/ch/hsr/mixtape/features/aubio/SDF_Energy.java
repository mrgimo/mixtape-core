package ch.hsr.mixtape.features.aubio;

/**
 * Energy based onset detection function.
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_Energy implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++)
			onset[0] += fftgrain[1][i] * fftgrain[1][i];
		return onset;
	}

}

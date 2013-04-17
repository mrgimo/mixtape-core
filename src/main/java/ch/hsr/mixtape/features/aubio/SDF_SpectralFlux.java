package ch.hsr.mixtape.features.aubio;

/**
 * Spectral flux based onset detection function.
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_SpectralFlux implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++) {
			if (fftgrain[1][i] > sd.oldMagnitude[i])
				onset[0] += fftgrain[1][i] - sd.oldMagnitude[i];
			sd.oldMagnitude[i] = fftgrain[1][i];
		}
		return onset;
	}

}

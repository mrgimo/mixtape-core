package ch.hsr.mixtape.processing.temporal;

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
			if (fftgrain[1][i] > sd.oldmag[i])
				onset[0] += fftgrain[1][i] - sd.oldmag[i];
			sd.oldmag[i] = fftgrain[1][i];
		}
		return onset;
	}

}

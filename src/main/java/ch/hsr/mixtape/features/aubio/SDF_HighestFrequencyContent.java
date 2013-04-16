package ch.hsr.mixtape.features.aubio;

/**
 * High Frequency Content onset detection function.
 * 
 * Finds the sum of frequency weighted short term spectral frames. According to
 * Davies and Plumbley [1] this function is most suited for detecting wide-band
 * percussive events.
 * 
 * <p>
 * [1] Comparing mid-level representations for audio based beat tracking, Davies
 * & Plumbley, 2005
 * </p>
 * 
 * <p>
 * <b>The method in this class was extracted from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SDF_HighestFrequencyContent implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++)
			onset[0] += (i + 1) * fftgrain[1][i];
		return onset;
	}

}

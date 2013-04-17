package ch.hsr.mixtape.features.aubio;

/**
 * Spectral difference method onset detection function. This function is a
 * measure of the Euclidean distance between the magnitude spectra of two
 * adjacent frames [1].
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
public class SDF_SpectralDifference implements SpectralDescriptionFunction {

	public double[] call(SpectralDescription sd, double[][] fftgrain) {
		double[] onset = new double[1];
		for (int i = 0; i < fftgrain[1].length; i++) {
			sd.dev1[i] = Math.sqrt(Math.abs(fftgrain[1][i] * fftgrain[1][i]
					- sd.oldMagnitude[i] * sd.oldMagnitude[i]));
			if (sd.threshold < fftgrain[1][i])
				sd.dev1[i] = Math.abs(sd.dev1[i]);
			else
				sd.dev1[i] = 0.0;
			sd.oldMagnitude[i] = fftgrain[1][i];
		}

		/*
		 * apply o.histogram (act somewhat as a low pass on the overall
		 * function)
		 */
		sd.histogram.computeDynamicHistogram(sd.dev1);
		/* weight it */
		sd.histogram.applyWeight();
		/* its mean is the result */
		onset[0] = sd.histogram.mean();
		return onset;
	}

}

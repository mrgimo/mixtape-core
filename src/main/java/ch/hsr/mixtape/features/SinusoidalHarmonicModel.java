package ch.hsr.mixtape.features;

import java.util.Arrays;

//TODO: maybe needs to be changed to handle multiple fundamental frequencies per window

public class SinusoidalHarmonicModel {

	// TODO: find good threshold
	private static final int FREQUENCY_DEVIATION_THRESHOLD = 50;

	/**
	 * 
	 * @param powerSpectrum
	 *            Power Spectrum of the processing window
	 * @param fundamentalFrequency
	 *            Fundamental Frequency of the processing window
	 * @return indices of the harmonic overtones corresponding to the
	 *         powerspectrum
	 */

	public int[] generateSinusoidalHarmonicModel(double[] powerSpectrum,
			int fundamentalFrequency) {
		return findHarmonics(powerSpectrum, fundamentalFrequency);
	}

	private int[] findHarmonics(double[] powerSpectrum, int fundamentalFrequency) {
		PeakDetector peakDetector = new PeakDetector();

		int[] harmonicPeaks = peakDetector.getIndicesOfPeaks(powerSpectrum);
		int[] overtones = new int[harmonicPeaks.length];

		int overtoneCount = 0;

		for (int i = 0; i < harmonicPeaks.length; i++) {

			if (!(harmonicPeaks[i] == fundamentalFrequency)) {
				if (harmonicPeaks[i] % fundamentalFrequency < FREQUENCY_DEVIATION_THRESHOLD) {
					overtones[overtoneCount] = harmonicPeaks[i];
					overtoneCount++;
				} else {
					if (fundamentalFrequency - harmonicPeaks[i]
							% fundamentalFrequency < FREQUENCY_DEVIATION_THRESHOLD) {
						overtones[overtoneCount] = harmonicPeaks[i];
						overtoneCount++;
					}
				}
			}

		}

		return Arrays.copyOf(overtones, overtoneCount);
	}

}

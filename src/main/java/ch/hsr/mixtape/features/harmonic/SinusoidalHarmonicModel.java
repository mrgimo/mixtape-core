package ch.hsr.mixtape.features.harmonic;

import java.util.Arrays;

//TODO: maybe needs to be changed to handle multiple fundamental frequencies per window
public class SinusoidalHarmonicModel {

	// TODO: find good threshold
	private static final int FREQUENCY_DEVIATION_THRESHOLD = 50;

	private double sampleRate;

	public SinusoidalHarmonicModel(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * 
	 * @param powerSpectrum
	 *            Power Spectrum of the processing window
	 * @param fundamentalFrequency
	 *            Fundamental Frequency of the processing window
	 * @return indices of the harmonic overtones corresponding to the
	 *         powerspectrum
	 */
	public int[] generateSinusoidalHarmonicModel(double[] powerSpectrum, double fundamentalFrequency) {
		return findHarmonics(powerSpectrum, fundamentalFrequency);
	}

	private int[] findHarmonics(double[] powerSpectrum, double fundamentalFrequency) {
		int[] harmonicPeaks = getPeaks(powerSpectrum);
		int[] overtones = new int[harmonicPeaks.length];

		double frequencyMulitplicator = sampleRate / powerSpectrum.length;

		int overtoneCount = 0;
		for (int i = 0; i < harmonicPeaks.length; i++) {
			if (!(harmonicPeaks[i] * frequencyMulitplicator == fundamentalFrequency)) {
				if (harmonicPeaks[i] * frequencyMulitplicator % fundamentalFrequency < FREQUENCY_DEVIATION_THRESHOLD) {
					overtones[overtoneCount] = harmonicPeaks[i];
					overtoneCount++;
				} else if (fundamentalFrequency - harmonicPeaks[i] * frequencyMulitplicator
						% fundamentalFrequency < FREQUENCY_DEVIATION_THRESHOLD) {
					overtones[overtoneCount] = harmonicPeaks[i];
					overtoneCount++;
				}
			}
		}

		return Arrays.copyOf(overtones, overtoneCount);
	}

	private int[] getPeaks(double[] powerSpectrum) {
		return new int[0];
	}

}
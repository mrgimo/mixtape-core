package ch.hsr.mixtape.processing.temporal;

import ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType;

/**
 * Tempo detection.
 * 
 * This object stores all the memory required for tempo detection algorithm and
 * returns the estimated beat locations.
 * 
 * <p>
 * <b>This class is a port of aubio's `tempo.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class Tempo {

	/**
	 * Onset detection.
	 */
	SpectralDescription onsetDetection;

	/**
	 * Phase vocoder.
	 */
	PhaseVocoder phaseVocoder;

	/**
	 * Peak picker.
	 */
	PeakPicker peakPicker;

	/**
	 * Beat tracking.
	 */
	BeatTracking beatTracking;

	/**
	 * Onset detection function value.
	 */
	double[] onsetFunctionValue;

	/**
	 * Peak picked detection function buffer.
	 */
	double[] dfframe;

	/**
	 * Beat tactus candidates.
	 */
	double[] out;

	/**
	 * Onset results.
	 */
	double[] onset;

	/**
	 * Silence parameter <br>
	 * Value Range: -120.0 to 0.0 <br>
	 * Aubio's default: -90.0
	 */
	double silence = -90.0;

	/**
	 * Peak picking threshold.
	 * 
	 * @see PeakPicker.threshold
	 */
	double threshold = 0.3;

	/**
	 * Current position in dfframe.
	 */
	int blockPos = 0;

	/**
	 * dfframe bufsize.
	 */
	int windowLength;

	/**
	 * dfframe hopsize.
	 */
	int step;

	/**
	 * Sampling rate of the signal.
	 */
	int sampleRate;

	/**
	 * Get hop_size.
	 */
	int hopSize;

	/**
	 * Total frames since beginning.
	 */
	int totalFrames = 0;

	/**
	 * Time of latest detected beat, in samples.
	 */
	int lastBeat = 0;

	/**
	 * Delay to remove to last beat, in samples.
	 */
	int delay = 0;

	public Tempo(SpectralDescriptionType onsetType, int bufferSize,
			int hopSize, int samplerate) {
		this.sampleRate = samplerate;
		windowLength = (int) (262144 / hopSize); // 512 * 512 / hopSize
		step = windowLength / 4;
		this.hopSize = hopSize;
		dfframe = new double[windowLength];
		out = new double[step];
		phaseVocoder = new PhaseVocoder(bufferSize, hopSize);
		peakPicker = new PeakPicker();
		peakPicker.setThreshold(threshold);
		onsetDetection = new SpectralDescription(onsetType, bufferSize);
		onsetFunctionValue = new double[1];
		beatTracking = new BeatTracking(windowLength);
		onset = new double[1];
	}

	/**
	 * Execute tempo detection function on input buffer.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_tempo_do
	 * </p>
	 */
	public double[] extractTempo(boolean silent, double[][] fftgrain) {
		double[] output = new double[2];
		double[] thresholded;
		onsetFunctionValue = onsetDetection.call(fftgrain);
		/* execute every overlap_size*step */
		if (blockPos == step - 1) {
			/* check dfframe */
			out = beatTracking.trackBeat(dfframe);
			/* rotate dfframe */
			for (int i = 0; i < windowLength - step; i++)
				dfframe[i] = dfframe[i + step];
			for (int i = windowLength - step; i < windowLength; i++)
				dfframe[i] = 0.;
			blockPos = -1;
		}
		blockPos++;
		onset = peakPicker.pickPeak(onsetFunctionValue);
		output[1] = onset[0];
		thresholded = peakPicker.getThresholdedInput();
		dfframe[windowLength - step + blockPos] = thresholded[0];
		/* end of second level loop */
		output[0] = 0; /* reset tactus */
		for (int i = 1; i < out[0]; i++) {
			/* if current frame is a predicted tactus */
			if (blockPos == (int) Math.floor(out[i])) {
				/* set tactus */
				output[0] = out[i] - Math.floor(out[i]);
				/* test for silence */
				if (!silent)
					lastBeat = totalFrames + (int) Math.floor(output[0] * hopSize + 0.5);
			}
		}
		totalFrames += hopSize;
		return output;
	}

	/**
	 * Get the time of the latest beat detected, in samples.
	 */
	public int getLastInSamples() {
		return lastBeat - delay;
	}

	/**
	 * Get the time of the latest beat detected, in seconds.
	 */
	public double getLastInSeconds() {
		return getLastInSamples() / (double) sampleRate;
	}

	/**
	 * Get the time of the latest beat detected, in milliseconds.
	 */
	public double getLastMilliseconds() {
		return getLastInSeconds() / 1000.;
	}

	/**
	 * Set delay, in samples.
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * Get delay, in samples.
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Get current tempo.
	 * 
	 * @return the currently observed tempo, or `0` if no consistent value is
	 *         found
	 */
	public double getBPM() {
		return beatTracking.getBPM();
	}

	/**
	 * Get current tempo confidence.
	 * 
	 * @return confidence with which the tempo has been observed, `0` if no
	 *         consistent value is found.
	 */
	public double getConfidence() {
		return beatTracking.getConfidence();
	}

	/**
	 * Check if buffer level in dB SPL is under a given threshold.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_silence_detection
	 * </p>
	 * 
	 * @param v
	 *            vector to get level from
	 * @param threshold
	 *            threshold in dB SPL
	 * @return False if level is under the given threshold, true otherwise.
	 */
	private boolean detectSilence(double[] v, double threshold) {
		double energy = 0.;
		for (int j = 0; j < v.length; j++)
			energy += v[j] * v[j];
		return 10. * Math.log10(energy / (double) v.length) >= threshold;
	}

}

package ch.hsr.mixtape.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioProperties {

	public static final Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
	public static final boolean BIG_ENDIAN = true;

	private double sampleRateInHz;

	private int sampleSizeInBits;
	private int numberOfChannels;

	public AudioProperties(double sampleRateInHz, int sampleSizeInBits, int numberOfChannels) {
		this.sampleRateInHz = sampleRateInHz;

		this.sampleSizeInBits = sampleSizeInBits;
		this.numberOfChannels = numberOfChannels;
	}

	public double getSampleRateInHz() {
		return sampleRateInHz;
	}

	public int getSampleSizeInBits() {
		return sampleSizeInBits;
	}

	public int getSampleSizeInBytes() {
		return sampleSizeInBits / 8;
	}

	public double getFrameRateInHz() {
		return sampleRateInHz;
	}

	public int getFrameSizeInBits() {
		return sampleSizeInBits * numberOfChannels;
	}

	public int getFrameSizeInBytes() {
		return getFrameSizeInBits() / 8;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

}

package ch.hsr.mixtape.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioChannel implements ReadableByteChannel {

	private ReadableByteChannel channel;
	private AudioProperties properties;

	private AudioChannel(ReadableByteChannel channel, AudioProperties properties) {
		this.channel = channel;
		this.properties = properties;
	}

	public static AudioChannel load(File file) {
		AudioInputStream stream = open(file);
		AudioFormat format = stream.getFormat();

		ReadableByteChannel channel = Channels.newChannel(stream);
		AudioProperties properties = getAudioProperties(format);

		return new AudioChannel(channel, properties);
	}

	private static AudioProperties getAudioProperties(AudioFormat format) {
		return new AudioProperties(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels());
	}

	private static AudioInputStream open(File file) {
		try {
			return tryOpen(file);
		} catch (UnsupportedAudioFileException | IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static AudioInputStream tryOpen(File file) throws UnsupportedAudioFileException, IOException {
		AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file);

		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = buildTargetFormat(sourceFormat);

		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	private static AudioFormat buildTargetFormat(AudioFormat sourceFormat) {
		float sampleRate = sourceFormat.getSampleRate();
		float frameRate = sampleRate;

		int numberOfChannels = sourceFormat.getChannels();

		int sampleSizeInBits = getSampleSizeInBits(sourceFormat.getSampleSizeInBits());
		int frameSizeInBytes = (sampleSizeInBits / 8) * numberOfChannels;

		return new AudioFormat(AudioProperties.ENCODING, sampleRate, sampleSizeInBits, numberOfChannels,
				frameSizeInBytes, frameRate, AudioProperties.BIG_ENDIAN);
	}

	private static int getSampleSizeInBits(int sampleSizeInBits) {
		switch (sampleSizeInBits) {
		case 8:
		case 32:
			return sampleSizeInBits;
		default:
			return 16;
		}
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public void close() throws IOException {
		channel.close();
	}

	public int read(ByteBuffer buffer) throws IOException {
		return channel.read(buffer);
	}

	public AudioProperties getProperties() {
		return properties;
	}

}

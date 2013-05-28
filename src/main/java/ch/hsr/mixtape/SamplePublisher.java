package ch.hsr.mixtape;

import static java.util.Arrays.copyOfRange;
import static org.apache.commons.math3.util.FastMath.max;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import ch.hsr.mixtape.audio.AudioChannel;
import ch.hsr.mixtape.domain.Song;

import com.google.common.primitives.Ints;

public class SamplePublisher {

	private final static int MIN_SAMPLE_BUFFER_SIZE = 8192;

	private final Song song;
	private final List<FeatureProcessor<?>> subscribers;

	private double[] sampleBuffer;

	private int writePosition;
	private int[] readPositions;

	private final AudioChannel channel;
	private final ByteBuffer byteBuffer;

	public SamplePublisher(Song song, List<FeatureProcessor<?>> subscribers) {
		this.song = song;
		this.subscribers = subscribers;

		sampleBuffer = new double[max(MIN_SAMPLE_BUFFER_SIZE, getMaxWindowSize(subscribers))];

		writePosition = 0;
		readPositions = new int[subscribers.size()];

		channel = AudioChannel.load(new File(song.getFilePath()));
		byteBuffer = ByteBuffer.allocate(getBufferSize(channel, sampleBuffer.length));
	}

	private int getBufferSize(AudioChannel channel, int maxWindowSize) {
		return channel.getProperties().getFrameSizeInBytes() * maxWindowSize;
	}

	private int getMaxWindowSize(Collection<FeatureProcessor<?>> subscribers) {
		int maxWindowSize = 0;
		for (FeatureProcessor<?> subscriber : subscribers)
			if (maxWindowSize < subscriber.getWindowSize())
				maxWindowSize = subscriber.getWindowSize();

		return maxWindowSize;
	}

	public void publish() throws IOException {
		while (readSamples()) {
			publishSamples();
			resetSampleBuffer();
		}

		publishRemainingSamples();
	}

	private boolean readSamples() throws IOException {
		if (channel.read(byteBuffer) != -1)
			return false;

		byteBuffer.flip();
		while (!isByteBufferEmpty() && !isSampleBufferFull())
			sampleBuffer[writePosition++] = readSample();

		byteBuffer.compact();

		return true;
	}

	private boolean isByteBufferEmpty() {
		return byteBuffer.remaining() < channel.getProperties().getFrameSizeInBytes();
	}

	private boolean isSampleBufferFull() {
		return writePosition == sampleBuffer.length;
	}

	private double readSample() {
		int numberOfChannels = channel.getProperties().getNumberOfChannels();

		double sample = 0;
		for (int channel1 = 0; channel1 < numberOfChannels; channel1++)
			sample += getSample();

		return sample / numberOfChannels;
	}

	private double getSample() {
		switch (channel.getProperties().getSampleSizeInBits()) {
		case 8:
			return (double) byteBuffer.get() / Byte.MAX_VALUE;
		case 32:
			return (double) byteBuffer.getInt() / Integer.MAX_VALUE;
		case 64:
			return (double) byteBuffer.getLong() / Long.MAX_VALUE;
		default:
			return (double) byteBuffer.getShort() / Short.MAX_VALUE;
		}
	}

	private void publishSamples() {
		for (int i = 0; i < readPositions.length; i++) {
			FeatureProcessor<?> subscriber = subscribers.get(i);

			int size = subscriber.getWindowSize();
			int hop = size - subscriber.getWindowOverlap();

			int position = readPositions[i];
			while (position + size <= writePosition) {
				subscriber.process(song, copyOfRange(sampleBuffer, position, position + size));
				position += hop;
			}

			readPositions[i] = position;
		}
	}

	private void resetSampleBuffer() {
		int minReadPosition = Ints.min(readPositions);
		for (int i = 0; i < readPositions.length; i++)
			readPositions[i] -= minReadPosition;

		writePosition -= minReadPosition;
		sampleBuffer = copyOfRange(sampleBuffer, minReadPosition, minReadPosition + sampleBuffer.length);
	}

	private void publishRemainingSamples() {
		for (int i = 0; i < readPositions.length; i++) {
			FeatureProcessor<?> subscriber = subscribers.get(i);

			int position = readPositions[i];
			int size = subscriber.getWindowSize();

			subscriber.process(song, copyOfRange(sampleBuffer, position, position + size));
		}
	}

}
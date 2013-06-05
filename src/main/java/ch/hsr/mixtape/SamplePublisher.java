package ch.hsr.mixtape;

import static java.util.Arrays.copyOfRange;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import ch.hsr.mixtape.audio.AudioChannel;
import ch.hsr.mixtape.domain.Song;

import com.google.common.primitives.Ints;

public class SamplePublisher {

	private final List<FeatureProcessor<?, ?>> subscribers;

	public SamplePublisher(List<FeatureProcessor<?, ?>> subscribers) {
		this.subscribers = subscribers;
	}

	private int getBufferSize(AudioChannel channel, int maxWindowSize) {
		return channel.getProperties().getFrameSizeInBytes() * maxWindowSize;
	}

	private int getMaxWindowSize(Collection<FeatureProcessor<?, ?>> subscribers) {
		int maxWindowSize = 0;
		for (FeatureProcessor<?, ?> subscriber : subscribers)
			if (maxWindowSize < subscriber.getWindowSize())
				maxWindowSize = subscriber.getWindowSize();

		return maxWindowSize;
	}

	public void publish(Song song) throws IOException {
		double[] sampleBuffer = new double[getMaxWindowSize(subscribers)];

		int writePosition = 0;
		int[] readPositions = new int[subscribers.size()];

		AudioChannel channel = AudioChannel.load(new File(song.getFilePath()));
		ByteBuffer byteBuffer = ByteBuffer.allocate(getBufferSize(channel, sampleBuffer.length));

		while ((writePosition = readSamples(channel, byteBuffer, sampleBuffer, writePosition)) != -1) {
			publishSamples(song, sampleBuffer, readPositions, writePosition);

			int minReadPosition = Ints.min(readPositions);

			sampleBuffer = resetSampleBuffer(sampleBuffer, minReadPosition);

			writePosition = resetWritePosition(writePosition, minReadPosition);
			readPositions = resetReadPositions(readPositions, minReadPosition);
		}

		publishRemainingSamples(song, sampleBuffer, readPositions);
	}

	private int readSamples(AudioChannel channel, ByteBuffer byteBuffer, double[] sampleBuffer, int writePosition)
			throws IOException {
		if (channel.read(byteBuffer) == -1)
			return -1;

		byteBuffer.flip();
		while (!isByteBufferEmpty(channel, byteBuffer) && !isSampleBufferFull(sampleBuffer, writePosition))
			sampleBuffer[writePosition++] = readSample(channel, byteBuffer);

		byteBuffer.compact();

		return writePosition;
	}

	private boolean isByteBufferEmpty(AudioChannel channel, ByteBuffer byteBuffer) {
		return channel.getProperties().getFrameSizeInBytes() > byteBuffer.remaining();
	}

	private boolean isSampleBufferFull(double[] sampleBuffer, int writePosition) {
		return sampleBuffer.length == writePosition;
	}

	private double readSample(AudioChannel channel, ByteBuffer byteBuffer) {
		int numberOfChannels = channel.getProperties().getNumberOfChannels();

		double sample = 0;
		for (int channel1 = 0; channel1 < numberOfChannels; channel1++) {
			sample += getSample(channel, byteBuffer);
		}

		return sample / numberOfChannels;
	}

	private double getSample(AudioChannel channel, ByteBuffer byteBuffer) {
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

	private void publishSamples(Song song, double[] sampleBuffer, int[] readPositions, int writePosition) {
		for (int i = 0; i < readPositions.length; i++) {
			FeatureProcessor<?, ?> subscriber = subscribers.get(i);

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

	private int[] resetReadPositions(int[] readPositions, int minReadPosition) {
		for (int i = 0; i < readPositions.length; i++)
			readPositions[i] -= minReadPosition;

		return readPositions;
	}

	private int resetWritePosition(int writePosition, int minReadPosition) {
		return writePosition - minReadPosition;
	}

	private double[] resetSampleBuffer(double[] sampleBuffer, int minReadPosition) {
		System.arraycopy(sampleBuffer, minReadPosition, sampleBuffer, minReadPosition, sampleBuffer.length - minReadPosition);
		return sampleBuffer;
	}

	private void publishRemainingSamples(Song song, double[] sampleBuffer, int[] readPositions) {
		for (int i = 0; i < readPositions.length; i++) {
			FeatureProcessor<?, ?> subscriber = subscribers.get(i);

			int position = readPositions[i];
			int size = subscriber.getWindowSize();

			subscriber.process(song, copyOfRange(sampleBuffer, position, position + size));
		}
	}

}
package ch.hsr.mixtape;

import static java.lang.System.arraycopy;
import static org.apache.commons.math3.util.FastMath.min;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

import ch.hsr.mixtape.audio.AudioChannel;
import ch.hsr.mixtape.domain.Song;

public class SampleLoader {

	private final AudioChannel channel;
	private final ByteBuffer buffer;

	private final Song song;
	private final Collection<FeatureExtractor<?>> extractors;

	private final int maxWindowSize;

	private double[] window;
	private int positionInWindow;

	public SampleLoader(Song song, Collection<FeatureExtractor<?>> extractors) {
		this.song = song;
		this.extractors = extractors;

		maxWindowSize = getMaxWindowSize(extractors);

		window = new double[maxWindowSize];
		positionInWindow = 0;

		channel = AudioChannel.load(new File(song.getFilePath()));
		buffer = ByteBuffer.allocate(getBufferSize(channel, maxWindowSize));
	}

	private int getBufferSize(AudioChannel channel, int maxWindowSize) {
		return channel.getProperties().getFrameSizeInBytes() * maxWindowSize;
	}

	private int getMaxWindowSize(Collection<FeatureExtractor<?>> subscribers) {
		int maxWindowSize = 0;
		for (FeatureExtractor<?> subscriber : subscribers)
			if (maxWindowSize < subscriber.getWindowSize())
				maxWindowSize = subscriber.getWindowSize();

		return maxWindowSize;
	}

	public void load() throws IOException {
		while (readFromChannel()) {
			buffer.flip();
			readSamples();
			buffer.compact();
		}

		publishWindow(positionInWindow);
	}

	private boolean readFromChannel() throws IOException {
		return channel.read(buffer) != -1;
	}

	private void readSamples() {
		while (!isBufferEmpty()) {
			if (isWindowFull()) {
				publishWindow(maxWindowSize);
				resetWindow();
			}

			readSample();
		}
	}

	private boolean isBufferEmpty() {
		return buffer.remaining() < channel.getProperties().getFrameSizeInBytes();
	}

	private boolean isWindowFull() {
		return positionInWindow == maxWindowSize;
	}

	private void publishWindow(int windowSize) {
		for (FeatureExtractor<?> subscriber : extractors) {
			int subscriberWindowSize = subscriber.getWindowSize();
			for (int i = 0; i < windowSize; i += subscriberWindowSize) {
				double[] subscriberWindow = new double[subscriberWindowSize];
				arraycopy(window, i, subscriberWindow, 0, min(windowSize - i, subscriberWindowSize));

				publishWindow(subscriber, subscriberWindow);
			}
		}
	}

	private void publishWindow(final FeatureExtractor<?> subscriber, final double[] window) {
		subscriber.extract(song, window);
	}

	private void resetWindow() {
		positionInWindow = 0;
	}

	private double readSample() {
		return window[positionInWindow++] = mergeChannels(channel, buffer);
	}

	private double mergeChannels(AudioChannel audioChannel, ByteBuffer buffer) {
		double sample = 0;
		for (int channel = 0; channel < audioChannel.getProperties().getNumberOfChannels(); channel++)
			sample += getSample(audioChannel, buffer);

		return sample;
	}

	private double getSample(AudioChannel audioChannel, ByteBuffer buffer) {
		switch (audioChannel.getProperties().getSampleSizeInBits()) {
		case 8:
			return buffer.get();
		case 32:
			return buffer.getInt();
		default:
			return buffer.getShort();
		}
	}

}
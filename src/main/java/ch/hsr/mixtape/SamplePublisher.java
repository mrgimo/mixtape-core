package ch.hsr.mixtape;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import ch.hsr.mixtape.audio.AudioChannel;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.model.Song;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class SamplePublisher {

	private final ListeningExecutorService extractionExecutor;
	private final ListeningExecutorService postprocessingExecutor;

	private double[] sampleBuffer;

	private int[] readPositions;
	private int writePosition;

	private AudioChannel channel;
	private ByteBuffer byteBuffer;

	private List<FeatureProcessor<?, ?>> processors = Lists.newArrayList();

	public SamplePublisher(ListeningExecutorService extractionExecutor, ListeningExecutorService postprocessingExecutor) {
		this.extractionExecutor = extractionExecutor;
		this.postprocessingExecutor = postprocessingExecutor;
	}

	public <FeaturesOfSong> ListenableFuture<FeaturesOfSong> register(FeatureExtractor<?, FeaturesOfSong> extractor) {
		FeatureProcessor<?, FeaturesOfSong> processor = new FeatureProcessor<>(extractor,
				extractionExecutor,
				postprocessingExecutor);
		processors.add(processor);

		return processor.getFeaturesOfSong();
	}

	private int calcSampleBufferSize(Collection<FeatureProcessor<?, ?>> subscribers) {
		int maxWindowSize = 0;
		for (FeatureProcessor<?, ?> subscriber : subscribers)
			if (maxWindowSize < subscriber.getWindowSize())
				maxWindowSize = subscriber.getWindowSize();

		return 2 * maxWindowSize;
	}

	private int calcByteBufferSize(AudioChannel channel, int sampleBufferSize) {
		return channel.getProperties().getFrameSizeInBytes() * sampleBufferSize;
	}

	public void publish(Song song) throws IOException {
		init(song);

		while (channel.read(byteBuffer) == -1) {
			readSamples();
			publishSamples();

			shift();
		}

		publishRemainingSamples();
	}

	private void init(Song song) {
		sampleBuffer = new double[calcSampleBufferSize(processors)];

		readPositions = new int[processors.size()];
		writePosition = 0;

		channel = AudioChannel.load(new File(song.getFilepath()));
		byteBuffer = ByteBuffer.allocate(calcByteBufferSize(channel, sampleBuffer.length));
	}

	private void shiftWritePosition(int offset) {
		writePosition -= offset;
	}

	private void readSamples() {
		byteBuffer.flip();
		while (!isByteBufferEmpty() && !isSampleBufferFull())
			sampleBuffer[writePosition++] = readSample();

		byteBuffer.compact();
	}

	private boolean isByteBufferEmpty() {
		return channel.getProperties().getFrameSizeInBytes() > byteBuffer.remaining();
	}

	private boolean isSampleBufferFull() {
		return writePosition == sampleBuffer.length;
	}

	private double readSample() {
		int numberOfChannels = channel.getProperties().getNumberOfChannels();

		double sample = 0;
		for (int channel1 = 0; channel1 < numberOfChannels; channel1++)
			sample += nextSample();

		return sample / numberOfChannels;
	}

	private double nextSample() {
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
			FeatureProcessor<?, ?> processor = processors.get(i);

			int windowSize = processor.getWindowSize();
			int hopSize = windowSize - processor.getWindowOverlap();

			int readPosition = readPositions[i];
			while (readPosition + windowSize <= writePosition) {
				double[] windowOfSamples = new double[windowSize];
				arraycopy(sampleBuffer, readPosition, windowOfSamples, 0, windowSize);

				processor.process(windowOfSamples);
				readPosition += hopSize;
			}

			readPositions[i] = readPosition;
		}
	}

	private void shift() {
		int offset = Ints.min(readPositions);
		if (offset == 0)
			return;

		shiftSampleBuffer(offset);
		shiftReadPositions(offset);
		shiftWritePosition(offset);
	}

	private void shiftSampleBuffer(int offset) {
		arraycopy(sampleBuffer, offset, sampleBuffer, offset, sampleBuffer.length - offset);
	}

	private void shiftReadPositions(int offset) {
		for (int i = 0; i < readPositions.length; i++)
			readPositions[i] -= offset;
	}

	private void publishRemainingSamples() {
		for (int i = 0; i < readPositions.length; i++) {
			FeatureProcessor<?, ?> processor = processors.get(i);

			int windowSize = processor.getWindowSize();
			int readPosition = readPositions[i];

			double[] windowOfSamples = new double[windowSize];
			arraycopy(sampleBuffer, readPosition, windowOfSamples, 0, writePosition - readPosition);

			processor.process(windowOfSamples);
			processor.postprocess();
		}
	}

}
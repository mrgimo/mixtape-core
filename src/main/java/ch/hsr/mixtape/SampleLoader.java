package ch.hsr.mixtape;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import ch.hsr.mixtape.audio.AudioChannel;

import com.google.common.collect.Lists;

public class SampleLoader {

	private static final int BUFFER_CAPACITY = 4096;

	public double[] getSamples(AudioChannel channel) {
		try {
			return tryRead(channel);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private double[] tryRead(AudioChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
		List<Double> samples = Lists.newArrayList();

		double amplitudePeek = 0;
		while (channel.read(buffer) != -1) {
			buffer.flip();

			while (buffer.remaining() >= channel.getProperties().getFrameSizeInBytes()) {
				double sample = mergeChannels(channel, buffer);
				double amplitude = Math.abs(sample);

				if (amplitude > amplitudePeek)
					amplitudePeek = amplitude;

				samples.add(sample);
			}

			buffer.compact();
		}

		return normalizeAmplitude(samples, amplitudePeek);
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

	private double[] normalizeAmplitude(List<Double> samples, double amplitudePeek) {
		double[] normalizedSamples = new double[samples.size()];
		for (int i = 0; i < normalizedSamples.length; i++)
			normalizedSamples[i] = samples.get(i) / amplitudePeek;

		return normalizedSamples;
	}

}

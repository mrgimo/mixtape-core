package ch.hsr.mixtape.distancefunction;


import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;

public class NormalizedCompressionDistance implements DistanceFunction {

	private static final int MEMORYSIZE_DOUBLE = 8;
	private static final int BUFFER_SIZE = 1028;

	@Override
	public double computeDistance(double[] featureVector1,
			double[] featureVector2) {
		byte[] compressedVector1 = compress(featureVector1);
		byte[] compressedVector2 = compress(featureVector2);
		
		double[] combinedVectors = Arrays.copyOf(featureVector1, featureVector1.length + featureVector2.length);
		System.arraycopy(featureVector2, 0, combinedVectors, featureVector1.length, featureVector2.length);
		
		byte[] compressedCombinedVectors = compress(combinedVectors);
		
		return computeNCD(compressedVector1.length, compressedVector2.length, compressedCombinedVectors.length);
	}
	

	private double computeNCD(double zX, double zY, double zXY) {
		return (zXY - Math.min(zX, zY)) /
				Math.max(zX, zY);
	}
	
	private byte[] compress(double[] vectorValues) {
		byte[] byteVectorValues = toByteArray(vectorValues);
		ByteArrayOutputStream compressedValues = new ByteArrayOutputStream();
		byte[] compressionBuffer = new byte[BUFFER_SIZE];
		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);
		compressor.setInput(byteVectorValues);
		compressor.finish();
		
		while (!compressor.finished()) {
			int compressedByteCount = compressor.deflate(compressionBuffer);
			compressedValues.write(compressionBuffer, 0, compressedByteCount);
		}
		return compressedValues.toByteArray();
	}

	private byte[] toByteArray(double[] vectorValues) {
		byte[] byteValues = new byte[MEMORYSIZE_DOUBLE * vectorValues.length];
		ByteBuffer byteConverter = ByteBuffer.wrap(byteValues);
		
		for (int i = 0; i < vectorValues.length; i++) {
			byteConverter.putDouble(vectorValues[i]);
		}
		return byteValues;
	}

}

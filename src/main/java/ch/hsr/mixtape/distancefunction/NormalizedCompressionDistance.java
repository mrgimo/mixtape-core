package ch.hsr.mixtape.distancefunction;

/*
 * doesnt help shit -> double values seldom exactly equal
 */

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Deflater;

import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.SpectralFeature;

@Deprecated
public class NormalizedCompressionDistance implements DistanceFunction {

	private static final int MEMORYSIZE_DOUBLE = 8;
	private static final int BUFFER_SIZE = 1028;

	@Override
	public double computeDistance(FeatureVector featureVector1,
			FeatureVector featureVector2) {
		double[] distanceVector = new double[featureVector1.getDimension()];
		
		ArrayList<SpectralFeature> featuresV1 = featureVector1.getFeatures();
		ArrayList<SpectralFeature> featuresV2 = featureVector2.getFeatures();
		
		for (int i = 0; i < featureVector1.getDimension(); i++) {
			distanceVector[i] = computeDistance(featuresV1.get(i).windowValues(), featuresV2.get(i).windowValues());
			System.out.println("NCD Feature  " + i + " " + distanceVector[i]);
		}
		return euclidenVectorLength(distanceVector);
	}


	private double euclidenVectorLength(double[] distanceVector) {
		
		double length = 0.0;
		
		for (int i = 0; i < distanceVector.length; i++) {
			length += distanceVector[i];
		}
		return Math.sqrt(length);
	}


	public double computeDistance(double[] featureValuesV1,
			double[] featureValuesV2) {
		byte[] compressedVector1 = compress(featureValuesV1);
		byte[] compressedVector2 = compress(featureValuesV2);
		
		
		double[] combinedValues = Arrays.copyOf(featureValuesV1, featureValuesV1.length + featureValuesV2.length);
		System.arraycopy(featureValuesV2, 0, combinedValues, featureValuesV1.length, featureValuesV2.length);
		
		byte[] compressedCombinedVectors = compress(combinedValues);
		
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
		
		for (double value : vectorValues) {
			byteConverter.putDouble(value);
		}
		return byteValues;
	}

}

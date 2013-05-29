package ch.hsr.mixtape.processing;

import org.apache.commons.math3.util.FastMath;

public class MathUtils {

	public static double[] fft(double[] window) {
		// TODO Auto-generated method stub
		return null;
	}

	public static double[] multiply(double[] frequencySpectrum, double[] frequencySpectrum2) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static double vectorLength(double[] values) {
		return FastMath.sqrt(squaredSum(values));
	}
	
	public static double squaredSum(double[] values) {
		double sum = 0;
		
		for (int i = 0; i < values.length; i++) {
			sum += values[i] * values[i];
		}
		return sum;
	}
	
}

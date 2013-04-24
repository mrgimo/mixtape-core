package ch.hsr.mixtape.distancefunction.skew;

import java.util.Random;

import cern.colt.Arrays;

public class floatingSort {

	public static void main(String[] args) {

		double[] values = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13};
		System.out.println(values.length + " length");
//		
//		int count = 10000000;
//		double[] values2 = new double[count];
//		
//		System.out.println("generating " + count + " values...");
//		for (int i = 0; i < count; i++) {
//			values2[i] =  new Random().nextDouble();
//		}
		
		System.out.println("computing suffix array...");
		Skew skew = new Skew();

		long startTime = System.currentTimeMillis();
		
		int[] suffixArray = skew.buildSuffixArray(values);
		
		long endTime = System.currentTimeMillis();
		
		
		
		
		System.out.println("done after " + (endTime - startTime) + " ms");
		System.out.println("SA: " + Arrays.toString(suffixArray));

		
	}

}

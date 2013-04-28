package ch.hsr.mixtape.distancefunction.skew;

import java.util.Random;

import cern.colt.Arrays;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.distancefunction.KolmogorovDistance;

public class floatingSort {

	private static final int MAX_VALUE = 5;

	public static void main(String[] args) {
		int[] values = new int[] { 3, 1, 2, 1 , 1, 1, 3, 1, 4, 5, 5};
		System.out.println(values.length + " length");
		
//		int[] values = new int[15];
//		
//		int maxValue = 0;
//		for (int i = 0; i < values.length; i++) {
//			int value = new Random().nextInt(MAX_VALUE);
//			if(value > maxValue)
//				maxValue = value;
//			values[i] = value;
//		}
		
		System.out.println(Arrays.toString(values));
		
		
//		 int count = 10000000;
//		 int[] values2 = new int[count];
//		
//		 System.out.println("generating " + count + " values...");
//		 for (int i = 0; i < count; i++) {
//		 values2[i] = i % count / 2 + 1;
//		 }

		 System.out.println("computing suffix array & lcp array...");
		 SkewInteger skew = new SkewInteger();
		 LCP lcp = new LCP();
		 
		
		 long startTime = System.currentTimeMillis();
		
		 int[] suffixArray = skew.buildSuffixArray(values, MAX_VALUE);
		 int[] lcpArray = lcp.longestCommonPrefixes(values, suffixArray);
		
		 long endTime = System.currentTimeMillis();
		
		 Song song = new Song();
			
//			Feature feature = new Feature("", values.length);
//			feature.addWindowValues(values);
//			feature.setLcp(lcpArray);
//			feature.setSuffixArray(suffixArray);
//			
//			FeatureVector fv = new FeatureVector();
//			fv.addFeature(feature);
//			
//			song.setFeatureVector(fv);
			
			double dist = new KolmogorovDistance().distance(song, song);
			
		
		
		 System.out.println("done after " + (endTime - startTime) + " ms");
		 System.out.println("values : " + Arrays.toString(values));
		 System.out.println("SA  : " + Arrays.toString(suffixArray));
		 System.out.println("LCP : " + Arrays.toString(lcpArray));
		 System.out.println("distance: " + dist);
		 

	}

	private static int[] generateInverseArray(int[] suffixArray) {

		int[] inversed = new int[suffixArray.length];

		for (int i = 0; i < suffixArray.length; i++) {
			inversed[suffixArray[i]] = i;
		}
		return inversed;
	}
}

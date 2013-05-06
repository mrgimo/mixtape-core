package ch.hsr.mixtape.distancefunction.skew;

import java.util.Random;

import cern.colt.Arrays;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.valuemapper.SpectralCentroidValueMaper;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistanceSpeedUp;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistance;

public class floatingSort {

	private static final int MAX_VALUE = 5;

	public static void main(String[] args) {

		
		int[] lcp = new int[]{0, 1, 1, 0, 2, 0};
		
		System.out.println(getAverageValue(lcp));
		//
		// int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		// int[] suffixArray = new SkewInteger().buildSuffixArray(input, 7);
		// int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		// int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		//
		// Song song = new Song();
		//
		//
		// Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		// f.setLcp(lcp);
		// f.setSuffixArray(suffixArray);
		// f.setNFCAs(nfcas);
		// f.addWindowValues(input);
		// FeatureVector fv = new FeatureVector();
		// fv.addFeature(f);
		// song.setFeatureVector(fv);
		//
		//
		//
		// NormalizedInformationDistanceSpeedUp dist = new
		// NormalizedInformationDistanceSpeedUp();
		// double act = dist.distance(song, song);
		//
		//

		// int[] values = new int[] { 3, 1, 2, 1 , 1, 1, 3, 1, 4, 5, 5};
		// System.out.println(values.length + " length");
		//
		// int[] values = new int[15];
		//
		// int maxValue = 0;
		// for (int i = 0; i < values.length; i++) {
		// int value = new Random().nextInt(MAX_VALUE);
		// if(value > maxValue)
		// maxValue = value;
		// values[i] = value;
		// }

		// System.out.println(Arrays.toString(values));

		// int count = 10000000;
		// int[] values2 = new int[count];
		//
		// System.out.println("generating " + count + " values...");
		// for (int i = 0; i < count; i++) {
		// values2[i] = i % count / 2 + 1;
		// }

		// System.out.println("computing suffix array & lcp array...");
		// SkewInteger skew = new SkewInteger();
		// LCP lcp = new LCP();
		//
		//
		// long startTime = System.currentTimeMillis();
		//
		// int[] suffixArray = skew.buildSuffixArray(values, MAX_VALUE);
		// int[] lcpArray = lcp.longestCommonPrefixes(values, suffixArray);
		//
		// long endTime = System.currentTimeMillis();
		//
		// Song song = new Song();

		// Feature feature = new Feature("", values.length);
		// feature.addWindowValues(values);
		// feature.setLcp(lcpArray);
		// feature.setSuffixArray(suffixArray);
		//
		// FeatureVector fv = new FeatureVector();
		// fv.addFeature(feature);
		//
		// song.setFeatureVector(fv);
		//
		// double dist = new NormalizedInformationDistance().distance(song,
		// song);
		//
		//
		//
		// System.out.println("done after " + (endTime - startTime) + " ms");
		// System.out.println("values : " + Arrays.toString(values));
		// System.out.println("SA  : " + Arrays.toString(suffixArray));
		// System.out.println("LCP : " + Arrays.toString(lcpArray));
		// System.out.println("distance: " + dist);

	}

	private static double getAverageValue(int[] lcp) {

		int sum = 0;
		for (int i = 0; i < lcp.length; i++) {
			sum += lcp[i];
		}
		return (double)sum / lcp.length;
	}

	private static int[] generateInverseArray(int[] suffixArray) {

		int[] inversed = new int[suffixArray.length];

		for (int i = 0; i < suffixArray.length; i++) {
			inversed[suffixArray[i]] = i;
		}
		return inversed;
	}
}

package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;

import ch.hsr.mixtape.domain.Feature2;
import ch.hsr.mixtape.domain.Song;

public class NCD {

	public double distance(Song song1, Song song2) {
		ArrayList<Feature2> features1 = song1.getFeatureVector().getFeatures();
		ArrayList<Feature2> features2 = song2.getFeatureVector().getFeatures();

		for (int i = 0; i < features1.size(); i++) {
			double additionalBitsXY = compare(features1.get(i),
					features2.get(i));
		}

		return 0;
	}

	private double compare(Feature2 feature, Feature2 feature2) {
		int[] windowValues1 = feature.windowValues();
		int[] windowValues2 = feature2.windowValues();
		int[] suffixArray1 = feature.getSuffixArray();
		int[] suffixArray2 = feature2.getSuffixArray();
		int[] lcp1 = feature.getLcp();
		int[] lcp2 = feature2.getLcp();
		int[] nfcAs2 = feature2.getNFCAs();

		double totalAdditionalBits = 0.0;
		
		double[] entropie1 = findEntropieOfValues(windowValues1,
				feature.maxValue());

		for (int i = 0, j = 0; i < suffixArray1.length; i++) {

			int matchingSuffixIndex = findFirstMatchingBranch(
					windowValues1[suffixArray1[i]], suffixArray2, nfcAs2);

			if (hasMatchingSuffix(matchingSuffixIndex)) {
				int matches = 1;
				matches += findMatches(windowValues1, windowValues2, suffixArray1,
						suffixArray2, i, matchingSuffixIndex);
				
				while (hasMoreSuffixCandidates(lcp2, matches, ++matchingSuffixIndex)) {
					matches += findMatches(windowValues1, windowValues2, suffixArray1, suffixArray2, i, matchingSuffixIndex);
				}
				
				//suffix matches not completely
				if(matches < (windowValues1.length - suffixArray1[i] - 1)) {
					//add entropy of not matching values
				}
				
			}
		}

		return 0;
	}

	private boolean hasMoreSuffixCandidates(int[] lcp2, int matches, int nextSuffix) {
		 return nextSuffix < lcp2.length && lcp2[nextSuffix] >= matches;
	}

	private int findMatches(int[] windowValues1, int[] windowValues2,
			int[] suffixArray1, int[] suffixArray2, int indexSA1,
			int matchingSuffixIndex) {
		int matches = 0;

		while (windowValues1[suffixArray1[indexSA1] + matches] == windowValues2[suffixArray2[matchingSuffixIndex]
				+ matches])
			matches++;
		
		return matches;
	}

	private boolean hasMatchingSuffix(int matchingBrachIndex) {
		return matchingBrachIndex != -1;
	}

	private double[] findEntropieOfValues(int[] windowValues1, int maxValue) {

		double[] appearance = new double[maxValue];
		double[] entropy = new double[maxValue];

		for (int i = 0; i < windowValues1.length; i++)
			appearance[windowValues1[i]]++;

		for (int i = 0; i < windowValues1.length; i++)
			entropy[windowValues1[i]] = calculateEntropy(windowValues1,
					appearance, i);

		return appearance;
	}

	private double calculateEntropy(int[] windowValues1,
			double[] entropieValues, int index) {
		return Math.log(entropieValues[windowValues1[index]]
				/ windowValues1.length)
				/ Math.log(2);
	}

	private int findFirstMatchingBranch(int suffix1, int[] suffixArray2,
			int[] nfcAs2) {

		return 0;
	}

}

package ch.hsr.mixtape.distancefunction.skew;

public class LCP {
	public int[] longestCommonPrefixes(int[] values, int[] suffixArray) {

		int[] inversedSuffixArray = generateInverseArray(suffixArray);
		int[] lcp = new int[values.length];

		int predecessorSuffix = 0;
		int matches = 0;

		for (int suffix = 0; suffix < suffixArray.length; suffix++) {
			if (inversedSuffixArray[suffix] > 0) {
				predecessorSuffix = suffixArray[inversedSuffixArray[suffix] - 1];
				matches = findMatches(suffix, predecessorSuffix, values,
						matches > 0 ? matches - 1 : 0);
				lcp[inversedSuffixArray[suffix]] = matches;
			}
		}
		return lcp;
	}

	private int findMatches(int suffix, int predecessorSuffix, int[] values,
			int offset) {

		int matches = offset;

		while (suffix + offset < values.length
				&& predecessorSuffix + offset < values.length) {
			
			if (values[suffix++ + offset] != values[predecessorSuffix++
					+ offset])
				return matches;
			
			matches++;
		}
		return matches;
	}

	private int[] generateInverseArray(int[] suffixArray) {

		int[] inversed = new int[suffixArray.length];

		for (int i = 0; i < suffixArray.length; i++)
			inversed[suffixArray[i]] = i;
		
		return inversed;
	}
}

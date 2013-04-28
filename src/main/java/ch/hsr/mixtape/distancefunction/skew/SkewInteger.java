package ch.hsr.mixtape.distancefunction.skew;

import java.util.Arrays;

/**
 * Implementation based on the algorithm proposed in 'Simple Linear Work Suffix
 * Array Construction' by Juha Kärkkäinen and Peter Sanders
 */

public class SkewInteger {

	/*
	 * still some constellations not tested
	 */

	private static final int ADDITIONAL_BORDERVALUES = 3;

	/**
	 * 
	 * @param values
	 *            input 
	 * @param maxValue
	 *            maximum value in input
	 * @return suffix array for the input 
	 */
	public int[] buildSuffixArray(int[] values, int maxValue) {

		int[] input = Arrays.copyOf(values, values.length
				+ ADDITIONAL_BORDERVALUES);
		input[values.length] = input[values.length + 1] = input[values.length + 2] = 0;

		int lengthMod0 = values.length / 3 + 1;

		int lengthMod1 = values.length % 3 == 0 ? values.length / 3
				: values.length / 3 + 1;
		int lengthMod2 = values.length % 3 == 2 ? values.length / 3 + 1
				: values.length / 3;
		int lengthMod12 = lengthMod1 + lengthMod2;

		int[] suffixArray = new int[values.length];
		int[] suffixArrayMod12 = new int[lengthMod12];

		int[] indicesMod0 = new int[lengthMod0];
		int[] suffixArrayMod0 = new int[lengthMod0];

		int[] indicesMod12 = findMod12Positions(values.length + 1, lengthMod12);
		int[] valuesByRanks = new int[lengthMod12];

		suffixArrayMod12 = radixSort(indicesMod12, input, 2, maxValue + 1);
		suffixArrayMod12 = radixSort(suffixArrayMod12, input, 1, maxValue + 1);
		suffixArrayMod12 = radixSort(suffixArrayMod12, input, 0, maxValue + 1);

		int rank = 0;

		int previousTripleValue0 = Integer.MIN_VALUE;
		int previousTripleValue1 = Integer.MAX_VALUE;
		int previousTripleValue2 = Integer.MIN_VALUE;

		for (int i = 0; i < lengthMod12; i++) {
			if (tripleIsUnequal(input, suffixArrayMod12, previousTripleValue0,
					previousTripleValue1, previousTripleValue2, i)) {
				rank++;
				previousTripleValue0 = input[suffixArrayMod12[i]];
				previousTripleValue1 = input[suffixArrayMod12[i] + 1];
				previousTripleValue2 = input[suffixArrayMod12[i] + 2];
			}

			if ((suffixArrayMod12[i] % 3) == 1) {
				// left half
				valuesByRanks[suffixArrayMod12[i] / 3] = rank;
			} else {
				// right half
				valuesByRanks[suffixArrayMod12[i] / 3 + lengthMod1] = rank;
			}

		}

		if (ranksNotUnique(lengthMod12, rank)) {
			suffixArrayMod12 = buildSuffixArray(valuesByRanks, lengthMod12);
			// update unique ranks
			for (int i = 0; i < lengthMod12; i++)
				valuesByRanks[suffixArrayMod12[i]] = i + 1;

		} else {
			// map indices to ranks on a magic way
			for (int i = 0; i < lengthMod12; i++)
				suffixArrayMod12[valuesByRanks[i] - 1] = i;
		}

		// generate mod0 indices
		for (int i = 0, j = 0; i < lengthMod12; i++)
			if (suffixArrayMod12[i] < lengthMod0)
				indicesMod0[j++] = 3 * suffixArrayMod12[i];

		// sort mod0
		suffixArrayMod0 = radixSortByRanks(indicesMod0, valuesByRanks,
				lengthMod1, suffixArrayMod12.length);

		suffixArrayMod0 = radixSort(suffixArrayMod0, input, 0, maxValue + 1);

		int indexMod0 = values.length % 3 == 0 ? 1 : 0;
		int indexMod12 = values.length % 3 != 0 ? 1 : 0;

		// merge mod12 & mod0
		for (int indexSuffixArray = 0; indexSuffixArray < values.length; indexSuffixArray++) {

			int positionMod12 = suffixArrayMod12[indexMod12] < lengthMod1 ? suffixArrayMod12[indexMod12] * 3 + 1
					: (suffixArrayMod12[indexMod12] - lengthMod1) * 3 + 2;
			int positionMod0 = suffixArrayMod0[indexMod0];

			if (mod12IsSmaller(input, lengthMod1, suffixArrayMod12,
					valuesByRanks, indexMod12, positionMod12, positionMod0)) {

				suffixArray[indexSuffixArray] = positionMod12;
				indexMod12++;

				if (indexMod12 == lengthMod12)
					for (indexSuffixArray++; indexMod0 < lengthMod0; indexSuffixArray++, indexMod0++)
						suffixArray[indexSuffixArray] = suffixArrayMod0[indexMod0];
			} else {
				suffixArray[indexSuffixArray] = positionMod0;
				indexMod0++;

				if (indexMod0 == lengthMod0)
					for (indexSuffixArray++; indexMod12 < lengthMod12; indexSuffixArray++, indexMod12++)
						suffixArray[indexSuffixArray] = suffixArrayMod12[indexMod12] < lengthMod1 ? suffixArrayMod12[indexMod12] * 3 + 1
								: (suffixArrayMod12[indexMod12] - lengthMod1) * 3 + 2;
			}

		}

		return suffixArray;
	}

	private int[] radixSort(int[] indices, int[] values, int offset,
			int maxvalue) {

		int[] appearanceCounter = new int[maxvalue];
		int[] sortedValues = new int[indices.length];

		for (int i = 0; i < indices.length; i++)
			appearanceCounter[values[indices[i] + offset]]++;

		calculateOffsets(appearanceCounter);

		for (int i = 0; i < indices.length; i++)
			sortedValues[appearanceCounter[values[indices[i] + offset]]++] = indices[i];

		return sortedValues;
	}

	private int[] radixSortByRanks(int[] indices, int[] valuesByRanks,
			int lengthMod1, int maxvalue) {

		int[] appearanceCounter = new int[maxvalue + 2];
		int[] sortedValues = new int[indices.length];

		for (int i = 0; i < indices.length; i++)
			appearanceCounter[valuesByRanks[indices[i] / 3]]++;

		calculateOffsets(appearanceCounter);

		for (int i = 0; i < indices.length; i++)
			sortedValues[appearanceCounter[valuesByRanks[indices[i] / 3]]++] = indices[i];
		return sortedValues;
	}

	private void calculateOffsets(int[] appearanceCounter) {
		for (int i = 0, sum = 0; i < appearanceCounter.length; i++) {
			int count = appearanceCounter[i];
			appearanceCounter[i] = sum;
			sum += count;
		}
	}

	private boolean mod12IsSmaller(int[] values, int lengthMod1,
			int[] sortedIndicesMod12, int[] indicesMod12ByRanks,
			int mod12Count, int positionMod12, int positionMod0) {

		if (sortedIndicesMod12[mod12Count] < lengthMod1)
			return isLexOrder(values[positionMod12],
					indicesMod12ByRanks[sortedIndicesMod12[mod12Count]
							+ lengthMod1], values[positionMod0],
					indicesMod12ByRanks[positionMod0 / 3]);

		return isLexOrder(
				values[positionMod12],
				values[positionMod12 + 1],
				indicesMod12ByRanks[sortedIndicesMod12[mod12Count] - lengthMod1
						+ 1],
				values[positionMod0],
				values[positionMod0 + 1],
				values[positionMod0 + 1] != 0 ? indicesMod12ByRanks[positionMod0
						/ 3 + lengthMod1]
						: -1);
	}

	private boolean isLexOrder(int a1, int a2, int a3, int b1, int b2, int b3) {
		return a1 < b1 || (a1 == b1 && isLexOrder(a2, a3, b2, b3));
	}

	private boolean isLexOrder(int a1, int a2, int b1, int b2) {
		return a1 < b1 || (a1 == b1 && a2 < b2);
	}

	private boolean tripleIsUnequal(int[] input, int[] sortedIndicesMod12,
			int previousTripleValue0, int previousTripleValue1,
			int previousTripleValue2, int i) {
		return input[sortedIndicesMod12[i]] != previousTripleValue0

		|| input[sortedIndicesMod12[i] + 1] != previousTripleValue1
				|| input[sortedIndicesMod12[i] + 2] != previousTripleValue2;
	}

	private boolean ranksNotUnique(int lengthMod12, int ranks) {
		return ranks < lengthMod12;
	}

	private int[] findMod12Positions(int length, int lengthMod12) {
		int[] s12 = new int[lengthMod12];

		for (int i = 0, j = 0; i < length; i++) {
			if (i % 3 != 0)
				s12[j++] = i;
		}

		return s12;
	}
}

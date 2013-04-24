package ch.hsr.mixtape.distancefunction.skew;

import java.util.Arrays;

public class CopyOfSkew {

	/*
	 * problems with some constellations -> see unit tests
	 */

	private static final int ADDITIONAL_BORDERVALUES = 3;

	public int[] buildSuffixArray(double[] values) {

		double[] input = Arrays.copyOf(values, values.length
				+ ADDITIONAL_BORDERVALUES);
		input[values.length] = input[values.length + 1] = input[values.length + 2] = Double.NEGATIVE_INFINITY;

		int lengthMod0 = values.length % 3 != 0 ? values.length / 3 + 1
				: values.length / 3;
		
		//changed from == 2
		int lengthMod1 = values.length % 3 != 0 ? values.length / 3 + 1
				: values.length / 3;

		int lengthMod2 = values.length / 3;
		int lengthMod12 = lengthMod1 + lengthMod2;
		int lengthDifMod12 = lengthMod1 - lengthMod2;

		int[] suffixArray = new int[values.length];
		int[] suffixArrayMod12 = new int[lengthMod12 + lengthDifMod12];

		int[] indicesMod0 = new int[lengthMod0];
		int[] suffixArrayMod0 = new int[lengthMod0];

		int[] indicesMod12 = findMod12Positions(input.length - 2,
				lengthMod12);
		double[] valuesByRanks = new double[lengthMod12 + lengthDifMod12];

		suffixArrayMod12 = sort(indicesMod12, input, 2);
		suffixArrayMod12 = sort(suffixArrayMod12, input, 1);
		suffixArrayMod12 = sort(suffixArrayMod12, input, 0);

		int rank = 1;

		double previousTripleValue0 = Double.NEGATIVE_INFINITY;
		double previousTripleValue1 = Double.POSITIVE_INFINITY;
		double previousTripleValue2 = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < lengthMod12 + lengthDifMod12; i++) {
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
			suffixArrayMod12 = buildSuffixArray(valuesByRanks);
			// update unique ranks
			for (int i = 0; i < lengthMod12; i++)
				valuesByRanks[(int) suffixArrayMod12[i]] = i + 1;

		} else {
			// map indices to ranks on a magic way
			for (int i = 0; i < lengthMod12 + lengthDifMod12; i++)
				suffixArrayMod12[(int) valuesByRanks[i] - 1] = i;
		}

		// generate mod0 indices
		for (int i = 0, j = 0; i < lengthMod12 + lengthDifMod12; i++)
			if (suffixArrayMod12[i] < lengthMod0)
				indicesMod0[j++] = 3 * suffixArrayMod12[i];

		// sort by first position (enough?)
		suffixArrayMod0 = radixSortByRanks(indicesMod0, valuesByRanks, lengthMod1, rank);
		suffixArrayMod0 = sort(suffixArrayMod0, input, 0);

		int indexMod0 = 0;
		int indexMod12 = lengthDifMod12;

		// merge mod12 & mod0
		for (int indexSuffixArray = 0; indexSuffixArray < values.length; indexSuffixArray++) {

			int positionMod12 = suffixArrayMod12[indexMod12] < lengthMod1 ? suffixArrayMod12[indexMod12] * 3 + 1
					: (suffixArrayMod12[indexMod12] - lengthMod1) * 3 + 2;
			int positionMod0 = suffixArrayMod0[indexMod0];

			if (mod12IsSmaller(input, lengthMod1, suffixArrayMod12,
					valuesByRanks, indexMod12, positionMod12, positionMod0)) {

				suffixArray[indexSuffixArray] = positionMod12;
				indexMod12++;

				if (indexMod12 == lengthMod12 + lengthDifMod12)
					for (indexSuffixArray++; indexMod0 < lengthMod0; indexSuffixArray++, indexMod0++)
						suffixArray[indexSuffixArray] = suffixArrayMod0[indexMod0];
			} else {
				suffixArray[indexSuffixArray] = positionMod0;
				indexMod0++;

				if (indexMod0 == lengthMod0)
					for (indexSuffixArray++; indexMod12 < lengthMod12
							+ lengthDifMod12; indexSuffixArray++, indexMod12++)
						suffixArray[indexSuffixArray] = suffixArrayMod12[indexMod12] < lengthMod1 ? suffixArrayMod12[indexMod12] * 3 + 1
								: (suffixArrayMod12[indexMod12] - lengthMod1) * 3 + 2;
			}

		}

		return suffixArray;
	}
	
	private int[] radixSort(int[] indices, int[] values, int offset, int maxvalue) {
		
		int[] appearanceCounter = new int[maxvalue];
		int[] sortedValues = new int[indices.length];
		
		// count appearance
		for (int i = 0; i < indices.length; i++) 
			appearanceCounter[values[indices[i] + offset]] ++;
		
		calculateOffsets(appearanceCounter);
		
		for (int i = 0; i < indices.length; i++) 
			sortedValues[appearanceCounter[values[indices[i] + offset]]] = indices[i];
		
		return sortedValues;
	}
	
	private int[] radixSortByRanks(int[] indices, double[] valuesByRanks, int lengthMod1, int maxvalue) {
		
		int[] appearanceCounter = new int[maxvalue + 2];
		int[] sortedValues = new int[indices.length];
		
		// if last pos mod0 -> rank(pos+1) no rank, but is smallest so shift ranks 1
		
		for (int i = 0; i < indices.length; i++) 
			appearanceCounter[(int) valuesByRanks[indices[i] / 3]] ++;
		
		calculateOffsets(appearanceCounter);
		
		for (int i = 0; i < indices.length; i++) 
			sortedValues[appearanceCounter[(int) valuesByRanks[indices[i] / 3]]] = indices[i];
		return sortedValues;
	}

	private void calculateOffsets(int[] appearanceCounter) {
		for (int i = 0, sum = 0; i < appearanceCounter.length; i++) {
			int count = appearanceCounter[i];
			appearanceCounter[i] = sum;
			sum += count;
		}
	}

	private boolean mod12IsSmaller(double[] values, int lengthMod1,
			int[] sortedIndicesMod12, double[] indicesMod12ByRanks,
			int mod12Count, int positionMod12, int positionMod0) {

		if (sortedIndicesMod12[mod12Count] < lengthMod1)
			return isLexOrder(values[positionMod12],
					indicesMod12ByRanks[sortedIndicesMod12[mod12Count]
							+ lengthMod1], values[positionMod0],
					indicesMod12ByRanks[positionMod0 / 3]);

		return isLexOrder(values[positionMod12], values[positionMod12 + 1],
				indicesMod12ByRanks[sortedIndicesMod12[mod12Count] - lengthMod1
						+ 1], values[positionMod0], values[positionMod0 + 1],
				indicesMod12ByRanks[sortedIndicesMod12[mod12Count] - lengthMod1
						+ 1]);
	}

	private boolean isLexOrder(double a1, double a2, double a3, double b1,
			double b2, double b3) {
		return a1 < b1 || (a1 == b1 && isLexOrder(a2, a3, b2, b3));
	}

	private boolean isLexOrder(double a1, double a2, double b1, double b2) {
		return a1 < b1 || (a1 == b1 && a2 < b2);
	}

	private boolean tripleIsUnequal(double[] input, int[] sortedIndicesMod12,
			double previousTripleValue0, double previousTripleValue1,
			double previousTripleValue2, int i) {
		return input[sortedIndicesMod12[i]] != previousTripleValue0

				|| input[sortedIndicesMod12[i] + 1] != previousTripleValue1
				|| input[sortedIndicesMod12[i] + 2] != previousTripleValue2;
	}

	private boolean ranksNotUnique(int lengthMod12, int ranks) {
		return ranks < lengthMod12;
	}

	private int[] sort(int[] sourceIndices, double[] input, int offset) {
		// radix sort O(n) intended but next to impossible with doubles in java
		// -> nlogn sort

		IndexValuePair[] indexValuePairs = new IndexValuePair[sourceIndices.length];

		for (int i = 0; i < sourceIndices.length; i++) {
			indexValuePairs[i] = new IndexValuePair(i, sourceIndices[i],
					input[sourceIndices[i] + offset]);
		}

		Arrays.sort(indexValuePairs);

		int[] sortedIndices = new int[indexValuePairs.length];

		for (int i = 0; i < indexValuePairs.length; i++) {
			sortedIndices[i] = indexValuePairs[i].getSourceIndex();
		}
		return sortedIndices;
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

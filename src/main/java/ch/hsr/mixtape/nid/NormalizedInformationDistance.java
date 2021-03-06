package ch.hsr.mixtape.nid;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.processing.Feature;

public class NormalizedInformationDistance {
	

	public double distanceBetween(Feature x, Feature y) {
		if(x.values.length == 0 || y.values.length == 0)
			return 1;
		
		int zX = compressedSize(x);
		int zY = compressedSize(y);

		int zXY = compressedCombindedSize(x, y);
		int zYX = compressedCombindedSize(y, x);

		if (zX > zY)
			return (double) (zX - (zY - zYX)) / zX;
		else
			return (double) (zY - (zX - zXY)) / zY;
	}

	private int compressedSize(Feature feature) {

		int[] values = feature.values;

		int[] sA = feature.getSuffixArray();
		int[] lcp = feature.getLcpOfSuffixArray();
		
		int lz77Triples = 0;
		for (int pos = 0; pos < values.length;) {
			int firstMatchingSuffix = findFirstMatchingSuffix(values[pos], sA,
					lcp, values);
			int match = findBestInternalMatch(values, pos, firstMatchingSuffix,
					sA, lcp) + 1;

			pos += match;
			lz77Triples++;
		}

		return lz77Triples;
	}

	private int compressedCombindedSize(Feature featureX, Feature featureY) {
		int[] valuesX = featureX.values;
		int[] valuesY = featureY.values;
		
		 int[] saX = featureX.getSuffixArray();
		 int[] saY = featureY.getSuffixArray();

		 int[] lcpX = featureX.getLcpOfSuffixArray();
		 int[] lcpY = featureY.getLcpOfSuffixArray();

		int lz77Triples = 0;
		for (int posX = 0; posX < valuesX.length;) {
			int firstMatchingSuffixX = findFirstMatchingSuffix(valuesX[posX],
					saX, lcpX, valuesX);
			int bestMatchX = firstMatchingSuffixX != -1 ? findBestInternalMatch(
					valuesX, posX, firstMatchingSuffixX, saX, lcpX) : 0;

			int firstMatchingSuffixY = findFirstMatchingSuffix(valuesX[posX],
					saY, lcpY, valuesY);
			int bestMatchY = firstMatchingSuffixY != -1 ? findBestMatch(
					valuesX, posX, firstMatchingSuffixY, saY, lcpY, valuesY)
					: 0;

			posX += FastMath.max(bestMatchX, bestMatchY) + 1;
			lz77Triples++;
		}

		return lz77Triples;
	}

	private int findBestInternalMatch(int[] values, int pos,
			int firstMatchingSuffix, int[] sA, int[] lcp) {
		int matches = 0;
		int sAIndex = firstMatchingSuffix;

		while (morePossibleMatches(values.length, pos, sAIndex, sA, matches)) {
			if (legitSuffix(pos, matches, sA[sAIndex])
					&& equal(values, pos, sA, matches, sAIndex))
				matches++;
			else if (hasMoreSuffixCandidates(sAIndex, sA, lcp, matches))
				sAIndex++;
			else
				return matches;
		}

		return matches;
	}

	private boolean equal(int[] values, int pos, int[] sA, int matches,
			int sAIndex) {
		return values[pos + matches] == values[sA[sAIndex] + matches];
	}

	private boolean legitSuffix(int pos, int matches, int sAIndex) {
		return sAIndex > pos + matches;
	}

	private int findBestMatch(int[] valuesX, int posX,
			int firstMatchingSuffixY, int[] saY, int[] lcpY, int[] valuesY) {
		int matches = 1;
		int sAIndexY = firstMatchingSuffixY;

		while (hasMoreValuesX(valuesX.length, posX, matches)) {
			if (morePossibleSuffixYMatches(saY, valuesY, matches, sAIndexY)
					&& equal(valuesX, posX, sAIndexY, saY, valuesY, matches))
				matches++;
			else if (hasMoreSuffixCandidates(sAIndexY, saY, lcpY, matches))
				sAIndexY++;
			else
				return matches;
		}

		return matches;
	}

	private boolean morePossibleSuffixYMatches(int[] saY, int[] valuesY,
			int matches, int sAIndexY) {
		return saY[sAIndexY] + matches < valuesY.length;
	}

	private boolean equal(int[] valuesX, int posX, int sAIndexY, int[] saY,
			int[] valuesY, int matches) {
		return valuesY[saY[sAIndexY] + matches] == valuesX[posX + matches];
	}

	private boolean morePossibleMatches(int lengthValues, int pos, int sAIndex,
			int[] sA, int matches) {
		return pos + matches < lengthValues
				&& sA[sAIndex] + matches < lengthValues;
	}

	private boolean hasMoreValuesX(int lengthValuesX, int posX, int matches) {
		return posX + matches < lengthValuesX;
	}

	private boolean hasMoreSuffixCandidates(int sAIndex, int[] sA, int[] lcp,
			int matches) {
		return sAIndex + 1 < sA.length && lcp[sAIndex + 1] >= matches;
	}

	private int findFirstMatchingSuffix(int value, int[] sA, int[] lcp,
			int[] values) {
		int posLeft = 0;
		int posRight = sA.length - 1;

		while (posLeft <= posRight) {
			int currentPos = (posLeft + posRight) / 2;
			int comparingValue = values[sA[currentPos]];

			if (comparingValue == value)
				return firstOccurence(lcp, currentPos);

			if (comparingValue > value)
				posRight = currentPos - 1;
			else
				posLeft = currentPos + 1;
		}

		return -1;
	}

	private int firstOccurence(int[] lcp, int currentPos) {
		while (lcp[currentPos] >= 1)
			currentPos--;

		return currentPos;
	}

}
package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.distancefunction.skew.LcpBuilder;
import ch.hsr.mixtape.distancefunction.skew.SuffixArrayBuilder;

public class LZ77V2 implements DistanceFunction {

	private SuffixArrayBuilder sABuilder = new SuffixArrayBuilder();
	private LcpBuilder lcpBuilder = new LcpBuilder();

	@Override
	public double distance(Song songX, Song songY) {

		ArrayList<Feature> featuresX = songX.getFeatureVector().getFeatures();
		ArrayList<Feature> featuresY = songY.getFeatureVector().getFeatures();

		double[] distanceVector = new double[featuresX.size()];

		for (int i = 0; i < featuresX.size(); i++) {

			int[] windowValuesX = featuresX.get(i).windowValues();
			int[] windowValuesY = featuresY.get(i).windowValues();

			int maxValueX = featuresX.get(i).maxValue();
			int maxValueY = featuresY.get(i).maxValue();

			int zX = compressedSize(windowValuesX, maxValueX);
			System.out.println("\n");
			int zY = compressedSize(windowValuesY, maxValueY);

			int maxValueXY = max(maxValueX, maxValueY);

//			System.out.println("\n\ncomputing k(x|y) \n");
			int zXY = compressedCombindedSize(windowValuesX, windowValuesY,
					maxValueXY);
			
//			System.out.println("\ncomputing k(y|x)");
			int zYX = compressedCombindedSize(windowValuesY, windowValuesX,
					maxValueXY);

//			System.out.println("\nzX: " + zX);
//			System.out.println("zY: " + zY);
//			System.out.println("zXY: " + zXY);
//			System.out.println("zYX: " + zYX);
//			System.out.println("distance " + featuresX.get(i).getName() + " : "
//					+ (max(zXY, zYX)) + " / " + max(zX, zY) + " = "
//					+ ((double) max(zXY, zYX) / max(zX, zY)));
//			System.out.println("\n");

			// System.out.println("other distance: " + ((double) (zXY + zYX) /
			// (windowValuesX.length + windowValuesY.length)));

			distanceVector[i] = (double) max(zXY, zYX) / max(zX, zY);

		}
		double vectorLength = vectorLength(distanceVector);
		System.out.println("\n\n" + songX.getName() + " \n" + songY.getName()
				+ "\n\nDistance: " + vectorLength
				+ "\n\n----------------------------------------------");
		return vectorLength;
	}

	private int compressedSize(int[] values, int maxValue) {

		int[] sA = sABuilder.buildSuffixArray(values, maxValue);
		int[] lcp = lcpBuilder.longestCommonPrefixes(values, sA);

		int lz77Tripples = 0;

		for (int pos = 0; pos < values.length;) {

			int firstMatchingSuffix = findFirstMatchingSuffix(values[pos], sA,
					lcp, values);

			int match = findBestInternalMatch(values, pos, firstMatchingSuffix,
					sA, lcp) + 1; // + 1 -> otherwise the
									// "1 match would be same as no match"
									// problem
									// occurs....

			// System.out.println("internal match: " + match);
//			if(match == 1) {
//				System.out.print((char) (values[pos] - 1));
////				System.out.print((char) (values[pos + 1] - 1));
//			} else {
//				System.out.print("[" + match + "]");
//			}
			
			pos += match;

			lz77Tripples++;

		}

		return lz77Tripples;
	}

	private double vectorLength(double[] distances) {
		double sqSum = 0.0;

		for (int i = 0; i < distances.length; i++) {
			sqSum += distances[i] * distances[i];
		}
		return Math.sqrt(sqSum);
	}

	private int max(int valueX, int valueY) {
		return valueX > valueY ? valueX : valueY;
	}

	private int compressedCombindedSize(int[] valuesX, int[] valuesY,
			int maxValue) {

		int[] saX = sABuilder.buildSuffixArray(valuesX, maxValue);
		int[] saY = sABuilder.buildSuffixArray(valuesY, maxValue);

		int[] lcpX = lcpBuilder.longestCommonPrefixes(valuesX, saX);
		int[] lcpY = lcpBuilder.longestCommonPrefixes(valuesY, saY);

		int lz77Tripples = 0;

		for (int posX = 0; posX < valuesX.length;) {
		//	System.out.println("find best match for position: " + posX + "\n");

			int firstMatchingSuffixX = findFirstMatchingSuffix(valuesX[posX],
					saX, lcpX, valuesX);

			int bestMatchX = firstMatchingSuffixX != -1 ? findBestInternalMatch(
					valuesX, posX, firstMatchingSuffixX, saX, lcpX) : 0;

			int firstMatchingSuffixY = findFirstMatchingSuffix(valuesX[posX],
					saY, lcpY, valuesY);

			int bestMatchY = firstMatchingSuffixY != -1 ? findBestMatch(
					valuesX, posX, firstMatchingSuffixY, saY, lcpY, valuesY)
					: 0;

		//	System.out.println("best selfMatch: " + bestMatchX);
		//	System.out.println("best other match: " + bestMatchY);
//					if(bestMatchX == 0) {
//				System.out.print((char) (valuesX[posX] - 1));
//				//System.out.print(bestMatchY + " : ");
//				// printNextChars(posX, valuesX);
//			} else {
//				System.out.print("[" + max(bestMatchX, bestMatchY) + "]");
//			}
		//	System.out.println();
			// TODO: changed formula
			posX += max(bestMatchY, bestMatchX) + 1;
			// posX += bestMatchY + 1; // + 1 -> otherwise the
			// "1 match would be same as no match"
			// problem occurs....

			lz77Tripples++;
		}

		return lz77Tripples;
	}

	private void printNextChars(int posX, int[] valuesX) {
			int range = posX + 80 > valuesX.length ? valuesX.length : posX + 80;
			System.out.println("none matching character");
			for (int i = posX; i < range; i++) {
				System.out.print((char)(valuesX[i] - 1));
			}
			System.out.println("\n");
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

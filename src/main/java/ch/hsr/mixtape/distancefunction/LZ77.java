package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.distancefunction.skew.LcpBuilder;
import ch.hsr.mixtape.distancefunction.skew.SuffixArrayBuilder;

public class LZ77 implements DistanceFunction {

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
			
			int zX = compressedSize(windowValuesX, 0, maxValueX);
			int zY = compressedSize(windowValuesY, 0, maxValueY);

			int[] windowValuesXY = merge(windowValuesY, windowValuesX);
			int[] windowValuesYX = merge(windowValuesX, windowValuesY);

			int maxValueXY = max(maxValueX, maxValueY);

			int zXY = compressedSize(windowValuesXY, windowValuesY.length, maxValueXY);
			int zYX = compressedSize(windowValuesYX, windowValuesX.length, maxValueXY);
			
			System.out.println("distance: " + (max(zXY, zYX)) + " / " + max(zX, zY));
			System.out.println(((double)(max(zXY, zYX)) / max(zX, zY)));
			distanceVector[i] = ((double)max(zXY, zYX)) / max(zX, zY);

		}
		double vectorLength = vectorLength(distanceVector);
		System.out.println("\n\n" + songX.getName() + " \n" + songY.getName()
				+ "\n\nDistance: " + vectorLength
				+ "\n\n----------------------------------------------");
		return vectorLength;
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

	private int compressedSize(int[] values, int startPos, int maxValue) {

		if (values.length > 1) {

			SuffixArrayBuilder sABuilder = new SuffixArrayBuilder();
			LcpBuilder lcpBuilder = new LcpBuilder();

			int valuesInDictionary = startPos;
			int lz77Tripples = 0;

			while (valuesInDictionary < values.length) {

				int[] dictionary = Arrays.copyOf(values, valuesInDictionary);
				int[] sADictionary = sABuilder.buildSuffixArray(dictionary,
						maxValue);
				int[] lcp = lcpBuilder.longestCommonPrefixes(dictionary,
						sADictionary, values.length);

				int lookAheadStartPos = valuesInDictionary;
				int firstMatchingSuffix = findFirstMatchingSuffix(
						values[lookAheadStartPos], sADictionary, lcp, values);

				valuesInDictionary += firstMatchingSuffix != -1 ? findBestMatch(
						firstMatchingSuffix, sADictionary, lcp,
						valuesInDictionary, values) + 1 : 1;

				lz77Tripples++;

			}

			return lz77Tripples;
		}

		return 1;
	}

	private int findBestMatch(int suffixIndex, int[] sADictionary, int[] lcp,
			int labStartPos, int[] values) {

		int matches = 1;

		while (hasMatchingCandidates(suffixIndex, sADictionary, lcp,
				labStartPos, values, matches)) {

			if (values[sADictionary[suffixIndex] + matches] == values[labStartPos
					+ matches])
				matches++;

			else if (hasMoreSuffixCandidates(suffixIndex, sADictionary, lcp,
					matches))
				suffixIndex++;

			else
				return matches;
		}

		return matches;
	}

	private boolean hasMoreSuffixCandidates(int suffixIndex,
			int[] sADictionary, int[] lcp, int matches) {
		return suffixIndex + 1 < sADictionary.length
				&& lcp[suffixIndex + 1] >= matches;
	}

	private boolean hasMatchingCandidates(int suffixIndex, int[] sADictionary,
			int[] lcp, int labStartPos, int[] values, int matches) {
		return labStartPos + matches < values.length
				&& sADictionary[suffixIndex] + matches < labStartPos;
	}

	private int findFirstMatchingSuffix(int value, int[] sADictionary, int[] lcp, int[] values) {

		int posLeft = 0;
		int posRight = sADictionary.length - 1;

		while (posLeft <= posRight) {
			int currentPos = (posLeft + posRight) / 2;

			int comparingValue = values[sADictionary[currentPos]];
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

	private int[] merge(int[] valuesX, int[] valuesY) {
		int[] valuesXY = Arrays
				.copyOf(valuesX, valuesX.length + valuesY.length);

		System.arraycopy(valuesY, 0, valuesXY, valuesX.length, valuesY.length);

		return valuesXY;
	}

}

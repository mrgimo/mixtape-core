package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;

import ch.hsr.mixtape.domain.Feature2;
import ch.hsr.mixtape.domain.Song;


/*

Time Measurement -> Distance computation normal


Available Processors: 6

Feature extraction and building of suffix array and lcp array done after: 186411 ms -> 3min 6.411s

Distance generation done in 666321 ms -> 11min 6.321s

Clustering done after 38 ms 

Task completed for 36 songs after 852773 ms -> 14min 12.77s



Time Measurement -> Distance computation speedUp


Available Processors: 6

Feature extraction and building of suffix array and lcp array done after: 186193 ms -> 3min 6.193s

Distance generation done in 441704 ms -> 7min 21.7s

Clustering done after 38 ms 

Task completed for 36 songs after 627947 ms  -> 10min 27.9s



==> Time saved 3min 44.6sec
 */


/**
 * Implementation for finding the normalized information distance (NID) between
 * two songs with the help of the suffix array and the longest common prefix
 * (lcp) array.
 * 
 * The formula used to compute the distance between two features is (maxXY -
 * maxZXY) / maxXY where maxXY is the higher value (max) of the maximum possible
 * matches for song X and song Y and maxZXY is the higher value of the matches
 * for song X taking song Y as input and vic versa.
 * <p>
 * The distances between two features are in the range [0,1] where 0 is similar and 1 is as different as
 * it gets.
 * <p>
 * The length of the distance vector is computed using the euclidean distance |v| = sqrt(sum(xi^2))
 * <p>
 * <p>
 * 
 * Dont know if my writing makes any sense but this was as clear as i could get
 * :)
 */

public class NormalizedInformationDistance {

	/**
	 * Computes the distance beteween two songs. The distance vector is
	 * generated computing the distance between each feature of the songs.
	 * 
	 * @param song1
	 *            song1 to compare
	 * @param song2
	 *            song2 to compare
	 * @return length of the distance vector
	 */

	public double distance(Song song1, Song song2) {

		ArrayList<Feature2> features1 = song1.getFeatureVector().getFeatures();
		ArrayList<Feature2> features2 = song2.getFeatureVector().getFeatures();

		double[] distanceVector = new double[features1.size()];

		for (int i = 0; i < features1.size(); i++) {
			int zXY = findMatches(features1.get(i), features2.get(i));
			int zYX = findMatches(features2.get(i), features1.get(i));

			double maxZXY = zXY > zYX ? zXY : zYX;

			int maxX = features1.get(i).getSuffixArray().length;
			int maxY = features2.get(i).getSuffixArray().length;

			double maxXY = maxX > maxY ? maxX : maxY;

			distanceVector[i] = maxXY > 0 ? (maxXY - maxZXY) / maxXY : 0;

		}

		double vectorLength = vectorLength(distanceVector);
		System.out.println("\ndistance " + song1.getName() + " to "
				+ song2.getName() + " : " + vectorLength);
		return vectorLength;
	}

	private double vectorLength(double[] distances) {
		double sqSum = 0.0;

		for (int i = 0; i < distances.length; i++) {
			sqSum += distances[i] * distances[i];
		}
		return Math.sqrt(sqSum);
	}

	private int findMatches(Feature2 spectralCentroidFeature1,
			Feature2 spectralCentroidFeature2) {

		int[] values1 = spectralCentroidFeature1.windowValues();

		int[] values2 = spectralCentroidFeature2.windowValues();
		int[] suffixArray2 = spectralCentroidFeature2.getSuffixArray();
		int[] lcp2 = spectralCentroidFeature2.getLcp();

		int totalMatches = 0;
		for (int i = 0; i < values1.length;) {
			int matches = findCommonPrefix(i, values1, suffixArray2, lcp2,
					values2);
			totalMatches += matches;

			i += matches > 0 ? matches : 1;
		}
		return totalMatches;
	}

	private int findCommonPrefix(int indexValues1, int[] values1,
			int[] suffixArray2, int[] lcp2, int[] values2) {

		int matches = 0;

		int suffix1 = values1[indexValues1];

		for (int i = 0; i < suffixArray2.length; i++) {
			int suffix2 = values2[suffixArray2[i]];
			if (suffix1 < suffix2)
				return matches;

			if (suffix1 == suffix2) {
				matches++;
				matches = compare(indexValues1, values1, suffixArray2[i],
						values2, matches);

				int nextSuffix2 = i + 1;
				while (hasMoreSuffixCandidates(lcp2, matches, nextSuffix2)) {
					matches = compare(indexValues1, values1,
							suffixArray2[nextSuffix2], values2, matches);
					nextSuffix2++;
				}
				return matches;
			}
		}

		return matches;
	}

	private boolean hasMoreSuffixCandidates(int[] lcp2, int matches,
			int nextSuffix) {
		return nextSuffix < lcp2.length && lcp2[nextSuffix] >= matches;
	}

	private int compare(int suffixIndex1, int[] values1, int suffixIndex2,
			int[] values2, int matches) {
		int currentMatches = matches;
		while (matches(suffixIndex1, values1, suffixIndex2, values2,
				currentMatches))
			currentMatches++;
		return currentMatches;
	}

	private boolean matches(int suffixIndex1, int[] values1, int suffixIndex2,
			int[] values2, int matches) {
		return suffixIndex1 + matches < values1.length
				&& suffixIndex2 + matches < values2.length
				&& values1[suffixIndex1 + matches] == values2[suffixIndex2
						+ matches];
	}

}

package ch.hsr.mixtape.distancefunction;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.Feature;

public class KolmogorovDistance {

	public double distance(Song song1, Song song2) {

		ArrayList<Feature> features1 = song1.getFeatureVector()
				.getFeatures();
		ArrayList<Feature> features2 = song2.getFeatureVector()
				.getFeatures();

		double[] distances = new double[features1.size()];
		
		for (int i = 0; i < features1.size(); i++) {
			int zXY = findMatches(features1.get(i),
					features2.get(i));
			int zYX = findMatches(features2.get(i), features1.get(i));
			
			double maxZXY = 0.0;
			
			if(zXY > zYX) {
				maxZXY = zXY;
			}else {
				maxZXY = zYX;
			}
			
			int maxX = features1.get(i).suffixArray().length;
			int maxY = features2.get(i).suffixArray().length;
			
			double maxXY = maxX > maxY ? maxX : maxY; 
			
			distances[i] = maxXY > 0 ? (maxXY - maxZXY) / maxXY : 0;
			
		}
		
		double vectorLength = vectorLength(distances);
		System.out.println("\ndistance " + song1.getName() + " to " + song2.getName() + " : " + vectorLength);
		return vectorLength;
	}

	private double vectorLength(double[] distances) {
		double sqSum = 0.0;
		
		for (int i = 0; i < distances.length; i++) {
			sqSum += distances[i] * distances[i];
		}
		return Math.sqrt(sqSum);
	}

	private int findMatches(
			Feature spectralCentroidFeature1,
			Feature spectralCentroidFeature2) {

		int[] values1 = spectralCentroidFeature1.windowValues();

		int[] values2 = spectralCentroidFeature2.windowValues();
		int[] suffixArray2 = spectralCentroidFeature2.suffixArray();
		int[] lcp2 = spectralCentroidFeature2.lcp();

		
		int totalMatches = 0;
		for (int i = 0; i < values1.length;) {
			int matches = findCommonPrefix(i, values1,
					suffixArray2, lcp2, values2);
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
		while (matches(suffixIndex1, values1, suffixIndex2, values2, currentMatches))
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

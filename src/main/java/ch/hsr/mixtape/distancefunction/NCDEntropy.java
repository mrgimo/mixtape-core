package ch.hsr.mixtape.distancefunction;

import java.util.Arrays;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.distancefunction.skew.LCP;
import ch.hsr.mixtape.distancefunction.skew.SkewInteger;

public class NCDEntropy implements DistanceFunction {

	AvgEntropyEstimator entropyEstimator = new AvgEntropyEstimator();

	@Override
	public double distance(Song songX, Song songY) {
		
		double[] distances = new double[songX.getFeatureVector().getDimension()];

		for (int i = 0; i < songX.getFeatureVector().getDimension(); i++) {

			Feature featureX = songX.getFeatureVector().getFeatures().get(i);
			Feature featureY = songY.getFeatureVector().getFeatures().get(i);

			int[] lcpX = featureX.getLcp();
			int[] lcpY = featureY.getLcp();

			double entropyX = entropyEstimator.estimateEntropy(lcpX);
			double entropyY = entropyEstimator.estimateEntropy(lcpY);
			

			double entropyXY = getCombinedEntropy(featureX, featureY);
			System.out.println(songX.getName() + "\n" + songY.getName());
			System.out.println("eX " + entropyX + "eY " + entropyY + "eXY " + entropyXY);
			
			distances[i] = (entropyXY - min(entropyX, entropyY)) / max(entropyX, entropyY);
			
		}
		double vectorLength = vectorLength(distances);
		System.out.println("Distance: \n" + songX.getName() + "\n" + songY.getName() + "\n" + vectorLength + "\n");
		return vectorLength(distances);
	}
	
	private double max(double valueX, double valueY) {
		return  valueX > valueY ? valueX : valueY;
	}

	private double min(double valueX, double valueY) {
		return valueX < valueY ? valueX : valueY;
	}

	private double vectorLength(double[] distances) {
		double sqSum = 0.0;

		for (int i = 0; i < distances.length; i++) {
			sqSum += distances[i] * distances[i];
		}
		return Math.sqrt(sqSum);
	}

	private double getCombinedEntropy(Feature featureX, Feature featureY) {

		SkewInteger skew = new SkewInteger();
		LCP lcpBuilder = new LCP();

		int[] windowValuesX = featureX.windowValues();
		int[] windowValuesY = featureY.windowValues();

		int[] windowValuesXY = Arrays.copyOf(windowValuesX,
				windowValuesX.length + windowValuesY.length);
		System.arraycopy(windowValuesY, 0, windowValuesXY,
				windowValuesX.length, windowValuesY.length);

		int maxValue = featureX.maxValue() > featureY.maxValue() ? featureX
				.maxValue() : featureY.maxValue();

		int[] suffixArrayXY = skew.buildSuffixArray(windowValuesXY, maxValue);

		int[] lcpXY = lcpBuilder.longestCommonPrefixes(windowValuesXY,
				suffixArrayXY);

		return entropyEstimator.estimateEntropy(lcpXY);
	}

}

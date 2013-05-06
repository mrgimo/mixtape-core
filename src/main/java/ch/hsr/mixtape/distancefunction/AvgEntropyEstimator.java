package ch.hsr.mixtape.distancefunction;
/***
 * Entropy estimated based on the average value
 *
 */
public class AvgEntropyEstimator {

	public double estimateEntropy(int[] lcp) {
		
		double averageCommonPrefixes = getAverageValue(lcp);
		
		double logN = (Math.log(lcp.length) / Math.log(2));
		
		return logN / averageCommonPrefixes;
	}

	private double getAverageValue(int[] lcp) {
		
		int sum = 0;
		for (int i = 1; i < lcp.length; i++) {
			sum += lcp[i] + 1;
		}
		return (double)sum / lcp.length;
	}
}

package ch.hsr.mixtape.distancefunction;
/***
 * Entropy estimated based on the average value
 *
 */
public class EntropyEstimator {

	public double estimateEntropy(int[] lcp) {
		
		double logN = (Math.log(lcp.length) / Math.log(2));
		
		double sum = 0;
		for (int i = 1; i < lcp.length; i++) {
			sum += logN / (lcp[i] + 1);
		}
		
		return sum / lcp.length;
	}

}

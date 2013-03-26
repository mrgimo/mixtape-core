package ch.hsr.mixtape.features;

public class ZeroCrossings {

	public long extractFeature(double[] window) {
		long numberOfZeroCrossings = 0;
		for (int i = 0; i < window.length - 1; i++) {
			if (window[i] > 0.0 && window[i + 1] < 0.0)
				numberOfZeroCrossings++;
			else if (window[i] < 0.0 && window[i + 1] > 0.0)
				numberOfZeroCrossings++;
			else if (window[i] == 0.0 && window[i + 1] != 0.0)
				numberOfZeroCrossings++;
		}

		return numberOfZeroCrossings;
	}

}
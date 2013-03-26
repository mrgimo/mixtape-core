package ch.hsr.mixtape.features;

public class StrengthOfStrongestBeat {

	public double extractFeature(double[] beatHistogram, double beatSum) {
		double highestBeat = Double.NEGATIVE_INFINITY;
		for (double beat : beatHistogram)
			if (highestBeat < beat)
				highestBeat = beat;

		return highestBeat / beatSum;
	}

}

package ch.hsr.mixtape.features;

public class HarmonicSpectralCentroid {

	public double extractFeature(double[] peaks) {
		double total = 0.0;
		double weightedTotal = 0.0;

		for (int i = 0; i < peaks.length; ++i) {
			weightedTotal += i / 2 * peaks[i];
			total += peaks[i];
		}

		return weightedTotal / total;
	}
}

package ch.hsr.mixtape.features;

public class HarmonicSpectralSmoothness {

	public double extractFeature(double[] peaks) {
		double result = 0.0;
		for (int i = 1; i < peaks.length - 1; i++)
			result += Math.abs(20 * Math.log(peaks[i]) - 20
					* (Math.log(peaks[i - 1]) + Math.log(peaks[i]) + Math.log(peaks[i + 1])) / 3);

		return result;
	}

}

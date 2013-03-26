package ch.hsr.mixtape.features;

public class HarmonicSpectralFlux {

	public double extractFeature(double[] samples, double sampling_rate, double[] currentPeaks, double[] previousPeaks) {
		double x, y, xy, x2, y2;
		x = y = xy = x2 = y2 = 0.0;
		int peakCount = Math.min(previousPeaks.length, currentPeaks.length);

		for (int i = 0; i < peakCount; ++i) {
			double previousPeak = previousPeaks[i];
			double currentPeak = currentPeaks[i];

			x += previousPeak;
			y += currentPeak;
			xy += previousPeak * currentPeak;
			x2 = previousPeak * previousPeak;
			y2 = currentPeak * currentPeak;
		}

		double top = xy - (x * y) / peakCount;
		double bottom = Math.sqrt(Math.abs((x2 - ((x * x) / peakCount)) * (y2 - ((y * y) / peakCount))));

		return top / bottom;
	}

}

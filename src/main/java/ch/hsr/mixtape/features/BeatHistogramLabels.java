package ch.hsr.mixtape.features;

public class BeatHistogramLabels {

	private static final int NUMBER_OF_BINS = 256;

	public double[] extractFeature(double[] samples, double samplingRate, double[] beatHistogram) {
		double effective_sampling_rate = samplingRate / ((double) NUMBER_OF_BINS);

		int min_lag = (int) (0.286 * effective_sampling_rate);
		int max_lag = (int) (3.0 * effective_sampling_rate);

		double[] labels = getAutoCorrelationLabels(effective_sampling_rate, min_lag, max_lag);
		for (int i = 0; i < labels.length; i++)
			labels[i] *= 60.0;

		return labels;
	}

	private double[] getAutoCorrelationLabels(double sampling_rate, int min_lag, int max_lag) {
		double[] labels = new double[max_lag - min_lag + 1];
		for (int i = 0; i < labels.length; i++)
			labels[i] = sampling_rate / ((double) (i + min_lag));
		return labels;
	}

}

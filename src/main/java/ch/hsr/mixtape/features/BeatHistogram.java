package ch.hsr.mixtape.features;

public class BeatHistogram {

	public double[] extractFeature(double[] samples, double sampling_rate, double[] rootMeanSquares) {
		double effective_sampling_rate = sampling_rate / ((double) rootMeanSquares.length);

		int min_lag = (int) (0.286 * effective_sampling_rate);
		int max_lag = (int) (3.0 * effective_sampling_rate);

		return getAutoCorrelation(rootMeanSquares, min_lag, max_lag);
	}

	public static double[] getAutoCorrelation(double[] signal, int min_lag, int max_lag) {
		double[] autocorrelation = new double[max_lag - min_lag + 1];
		for (int lag = min_lag; lag <= max_lag; lag++) {
			int auto_indice = lag - min_lag;
			autocorrelation[auto_indice] = 0.0;
			for (int samp = 0; samp < signal.length - lag; samp++)
				autocorrelation[auto_indice] += signal[samp] * signal[samp + lag];
		}
		return autocorrelation;
	}

}
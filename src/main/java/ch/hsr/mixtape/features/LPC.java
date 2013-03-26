package ch.hsr.mixtape.features;

public class LPC {

	private static final double LAMBDA = 0.0;
	private static final int NUMBER_OF_DIMENSIONS = 10;

	public double[] extractFeature(double[] samples) {
		double[] R = new double[NUMBER_OF_DIMENSIONS + 1];
		double K[] = new double[NUMBER_OF_DIMENSIONS];
		double A[] = new double[NUMBER_OF_DIMENSIONS];
		double[] dl = new double[samples.length];
		double[] Rt = new double[samples.length];
		double r1 = 0, r2 = 0, r1t = 0;

		for (int k = 0; k < samples.length; k++) {
			Rt[0] += samples[k] * samples[k];

			dl[k] = r1 - LAMBDA * (samples[k] - r2);
			r1 = samples[k];
			r2 = dl[k];
		}
		for (int i = 1; i < R.length; i++) {
			Rt[i] = 0;
			r1 = 0;
			r2 = 0;
			for (int k = 0; k < samples.length; k++) {
				Rt[i] += dl[k] * samples[k];

				r1t = dl[k];
				dl[k] = r1 - LAMBDA * (r1t - r2);
				r1 = r1t;
				r2 = dl[k];
			}
		}
		for (int i = 0; i < R.length; i++)
			R[i] = Rt[i];

		double Am1[] = new double[62];

		if (R[0] == 0.0) {
			for (int i = 1; i < NUMBER_OF_DIMENSIONS; i++) {
				K[i] = 0.0;
				A[i] = 0.0;
			}
		} else {
			double km, Em1, Em;
			int k, s, m;
			for (k = 0; k < NUMBER_OF_DIMENSIONS; k++) {
				A[0] = 0;
				Am1[0] = 0;
			}
			A[0] = 1;
			Am1[0] = 1;
			km = 0;
			Em1 = R[0];
			for (m = 1; m < NUMBER_OF_DIMENSIONS; m++) {
				double err = 0.0f;
				for (k = 1; k <= m - 1; k++)
					err += Am1[k] * R[m - k];
				km = (R[m] - err) / Em1;
				K[m - 1] = -km;
				A[m] = km;
				for (k = 1; k <= m - 1; k++)
					A[k] = Am1[k] - km * Am1[m - k];
				Em = (1 - km * km) * Em1;
				for (s = 0; s < NUMBER_OF_DIMENSIONS; s++)
					Am1[s] = A[s];
				Em1 = Em;
			}
		}
		return K;
	}

}

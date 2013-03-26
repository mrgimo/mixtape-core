package ch.hsr.mixtape.features;

public class AreaMomentsConstantQMFCC {

	private double x;
	private double y;
	private double x2;
	private double xy;
	private double y2;
	private double x3;
	private double y3;

	public double[] extractFeature(double[] samples, double sampling_rate, double[][] constantQDerivedMfccs) {
		double[] ret = new double[10];
		double sum = 0.0;

		for (int i = 0; i < constantQDerivedMfccs.length; ++i) {
			for (int j = 0; j < constantQDerivedMfccs[i].length; ++j) {
				sum += constantQDerivedMfccs[i][j];
			}
		}

		for (int i = 0; i < constantQDerivedMfccs.length; ++i) {
			for (int j = 0; j < constantQDerivedMfccs[i].length; ++j) {
				double tmp = constantQDerivedMfccs[i][j] / sum;
				x += tmp * i;
				y += tmp * j;
				x2 += tmp * i * i;
				xy += tmp * i * j;
				y2 += tmp * j * j;
				x3 += tmp * i * i * i;
				y3 += tmp * j * j * j;
			}
		}

		ret[0] = sum;
		ret[1] = x;
		ret[2] = y;
		ret[3] = x2 - x * x;
		ret[4] = xy - x * y;
		ret[5] = y2 - y * y;
		ret[6] = 2 * Math.pow(x, 3.0) - 3 * x * x2 + x3;
		ret[7] = 2 * x * xy - y * x2 + x2 * y;
		ret[8] = 2 * y * xy - x * y2 + y2 * x;
		ret[9] = 2 * Math.pow(y, 3.0) - 3 * y * y2 + y3;

		return ret;
	}

}

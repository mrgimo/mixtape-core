package ch.hsr.mixtape.features;

public class AreaMomentsMFCC {

	private double x;
	private double y;
	private double x2;
	private double xy;
	private double y2;
	private double x3;
	private double y3;

	public double[] extractFeature(double[][] mfccs) {
		double sum = 0.0;
		for (int i = 0; i < mfccs.length; ++i) {
			for (int j = 0; j < mfccs[i].length; ++j) {
				sum += mfccs[i][j];
			}
		}

		for (int i = 0; i < mfccs.length; ++i) {
			for (int j = 0; j < mfccs[i].length; ++j) {
				double tmp = mfccs[i][j] / sum;
				x += tmp * i;
				y += tmp * j;
				x2 += tmp * i * i;
				xy += tmp * i * j;
				y2 += tmp * j * j;
				x3 += tmp * i * i * i;
				y3 += tmp * j * j * j;
			}
		}

		double[] result = new double[10];

		result[0] = sum;
		result[1] = x;
		result[2] = y;
		result[3] = x2 - x * x;
		result[4] = xy - x * y;
		result[5] = y2 - y * y;
		result[6] = 2 * Math.pow(x, 3.0) - 3 * x * x2 + x3;
		result[7] = 2 * x * xy - y * x2 + x2 * y;
		result[8] = 2 * y * xy - x * y2 + y2 * x;
		result[9] = 2 * Math.pow(y, 3.0) - 3 * y * y2 + y3;

		return result;
	}

}

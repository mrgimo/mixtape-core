package ch.hsr.mixtape.features;

public class Derivative {

	public double[] extractFeature(double[] currentValues, double[] previousValues) {
		double[] result = new double[currentValues.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = currentValues[i] - previousValues[i];
		}

		return result;
	}

}

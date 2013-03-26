package ch.hsr.mixtape.features;

public class Mean {

	public double extractFeature(double[] values) {
		double sum = 0;
		for (double value : values)
			sum += value;

		return sum / values.length;
	}

}

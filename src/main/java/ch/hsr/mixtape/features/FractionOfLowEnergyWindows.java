package ch.hsr.mixtape.features;

public class FractionOfLowEnergyWindows {

	public double extractFeature(double[] rootMeanSquares) throws Exception {
		double average = 0.0;
		for (int i = 0; i < rootMeanSquares.length; i++)
			average += rootMeanSquares[i];

		average = average / ((double) rootMeanSquares.length);

		int count = 0;
		for (int i = 0; i < rootMeanSquares.length; i++)
			if (rootMeanSquares[i] < average)
				count++;

		return ((double) count) / ((double) rootMeanSquares.length);
	}

}
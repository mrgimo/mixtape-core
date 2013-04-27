package ch.hsr.mixtape.data;

public class SpectralCentroidFeature {

	private String name;
	private double[] windowValues;
	private int addedWindows = 0;
	private int maxValue = 0;
	private int[] suffixArray = new int[0];
	private int[] lcp = new int[0];
	private int[] values = new int[0];

	public SpectralCentroidFeature(String name, int windowCount) {
		this.name = name;
		windowValues = new double[windowCount];
		values = new int[windowCount];

	}

	public double[] windowValues() {
		return windowValues;
	}

	public String getName() {
		return name;
	}

	public void addWindowValue(double windowValue) {
		windowValues[addedWindows] = windowValue;
		values[addedWindows++] = (int) (windowValue * 100 + 1);
	}

	public double meanValue() {
		if (windowValues.length > 0) {
			double sum = 0;
			for (double value : windowValues)
				sum += value;
			return sum / windowValues.length;
		}
		return 0;
	}

	//TODO: maxVal needs to be set earlier :)
	public int[] intValues() {
		int[] values = new int[windowValues.length];

		for (int i = 0; i < windowValues.length; i++) {
			int value = ((int) windowValues[i]) + 1;

			if (value > maxValue)
				maxValue = value;

			values[i] = value;
		}

		return values;
	}
	
	public int maxValue() {
		
		int maxValue = 0;
		for (int i = 0; i < values.length; i++) {
			if(values[i] > maxValue)
				maxValue = values[i];
		}
		return maxValue;
	}

	public int[] suffixArray() {
		return suffixArray ;
	}

	public int[] lcp() {
		return lcp ;
	}

	public void setLcp(int[] lcpValues) {
		lcp = lcpValues;
	}

	public void setSuffixArray(int[] suffixArray) {
		this.suffixArray = suffixArray;
	}

	public void addWindowValues(double[] input) {
		windowValues = input;
	}
	
	public int[] getValues() {
		return values;
	}

	public void addWindowValues(int[] values) {
		this.values = values;
	}
}

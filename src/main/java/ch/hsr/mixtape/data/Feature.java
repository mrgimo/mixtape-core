package ch.hsr.mixtape.data;



public class Feature {
	
	private String name;
	private double[] windowValues;
	private int addedWindows = 0;
	
	public Feature(String name, int windowCount) {
		this.name = name;
		windowValues = new double[windowCount];
		
	}

	public double[] windowValues() {
		return windowValues;
	}

	public String getName() {
		return name;
	}

	public void addWindowValue(double windowValue) {
		windowValues[addedWindows++] = windowValue;
	}

	public double meanValue() {
		if(windowValues.length > 0) {
			double sum = 0;
			for (double value : windowValues)
				sum += value;
			return sum / windowValues.length;
		}
		return 0;
	}
}

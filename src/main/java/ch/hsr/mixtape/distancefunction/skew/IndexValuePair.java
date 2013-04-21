package ch.hsr.mixtape.distancefunction.skew;

public class IndexValuePair implements Comparable<IndexValuePair> {

	private int currentIndex;
	private int sourceIndex;
	private double value;

	public IndexValuePair(int currentIndex, int sourceIndex, double value) {
		this.currentIndex = currentIndex;
		this.sourceIndex = sourceIndex;
		this.value = value;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}

	public int getSourceIndex() {
		return sourceIndex;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int compareTo(IndexValuePair pair) {
		if (value > pair.getValue())
			return 1;
		if (value < pair.getValue())
			return -1;

		return currentIndex > pair.getCurrentIndex() ? 1 : -1;
	}
}

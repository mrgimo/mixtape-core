package ch.hsr.mixtape.features;

public class StrongestFrequencyViaZeroCrossings {

	public double extractFeature(int numberOfSamples, double samplingRate, int zeroCrossings) {
		return 0.5 * zeroCrossings * (samplingRate / numberOfSamples);
	}

}
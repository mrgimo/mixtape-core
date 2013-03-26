package ch.hsr.mixtape.features;

public class StrongestFrequencyViaSpectralCentroid {

	public double extractFeature(double samplingRate, double spectralCentroid, int numberOfPowerSpectra) {
		return (spectralCentroid / numberOfPowerSpectra) * (samplingRate / 2.0);
	}

}

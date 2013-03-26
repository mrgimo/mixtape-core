package ch.hsr.mixtape.features;

public class StrongestFrequencyViaFFTMaximum {

	public double extractFeature(double[] powerSpectrum, double[] fftBinFrequencyLabels) {
		int maxIndex = 0;
		for (int index = 1; index < powerSpectrum.length; index++)
			if (powerSpectrum[maxIndex] < powerSpectrum[index])
				maxIndex = index;

		return fftBinFrequencyLabels[maxIndex];
	}

}
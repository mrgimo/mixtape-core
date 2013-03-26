package ch.hsr.mixtape.features;

public class SpectralFlux {

	public double extractFeature(double[] window, double[] previousMagnitudeSpectrum, double[] currentMagnitudeSpectrum) {
		double spectralFlux = 0.0;
		for (int i = 0; i < currentMagnitudeSpectrum.length; i++) {
			double difference = currentMagnitudeSpectrum[i] - previousMagnitudeSpectrum[i];
			spectralFlux += difference * difference;
		}

		return spectralFlux;
	}

}
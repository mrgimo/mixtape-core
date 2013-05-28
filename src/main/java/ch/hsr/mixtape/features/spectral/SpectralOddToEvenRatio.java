package ch.hsr.mixtape.features.spectral;

public class SpectralOddToEvenRatio {

	public double extractFeature(double[] powerSpectrum) {
		double sumEvenFrequencies = 0.0;
		double sumOddFrequencies = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++)
			if (i % 2 == 0)
				sumEvenFrequencies += powerSpectrum[i] * powerSpectrum[i];
			else
				sumOddFrequencies += powerSpectrum[i] * powerSpectrum[i];

		return sumOddFrequencies / sumEvenFrequencies;
	}

}
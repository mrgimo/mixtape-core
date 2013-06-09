package ch.hsr.mixtape.processing.spectral;

public class SpectralOddToEvenRatio {

	public double extractFeature(double[] powerSpectrum) {
		double sumEvenFrequencies = 0.0;
		double sumOddFrequencies = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++)
			if (i % 2 == 0)
				sumEvenFrequencies += powerSpectrum[i] * powerSpectrum[i];
			else
				sumOddFrequencies += powerSpectrum[i] * powerSpectrum[i];

		//TODO: what to return if evenFrequency is 0 ? infinite?
		return sumEvenFrequencies != 0.0 ? sumOddFrequencies / sumEvenFrequencies : sumOddFrequencies;
	}

}
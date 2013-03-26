package ch.hsr.mixtape.features;

public class Compactness {

	public double extractFeature(double[] magnitudeSpectrum) {
		double compactness = 0.0;
		for (int i = 1; i < magnitudeSpectrum.length - 1; i++) {
			if ((magnitudeSpectrum[i - 1] > 0.0) && (magnitudeSpectrum[i] > 0.0) && (magnitudeSpectrum[i + 1] > 0.0)) {
				compactness += Math.abs(20.0
						* Math.log(magnitudeSpectrum[i])
						- 20.0
						* (Math.log(magnitudeSpectrum[i - 1]) + Math.log(magnitudeSpectrum[i]) + Math
								.log(magnitudeSpectrum[i + 1])) / 3.0);
			}
		}

		return compactness;
	}

}
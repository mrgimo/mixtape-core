package ch.hsr.mixtape.features;

public class ConstantQMFCC {

	private static final int NUMBER_OF_CEPSTRAL_COEFFICIENTS = 13;

	public double[] extractFeature(double[] logOfConstantQ) {
		return cepCoefficients(logOfConstantQ);
	}

	public double[] cepCoefficients(double[] logOfConstantQ) {
		double cepstralCoefficients[] = new double[NUMBER_OF_CEPSTRAL_COEFFICIENTS];
		for (int i = 0; i < cepstralCoefficients.length; i++) {
			for (int j = 1; j <= logOfConstantQ.length; j++) {
				cepstralCoefficients[i] += logOfConstantQ[j - 1]
						* Math.cos(Math.PI * i / logOfConstantQ.length * (j - 0.5));
			}
		}

		return cepstralCoefficients;
	}

}

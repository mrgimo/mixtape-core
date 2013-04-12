package ch.hsr.mixtape.features;

public class SpectralSpread {

	public double extractFeature(double[] powerSpectrum, double spectralCentroid) {

		double totalSpread = 0.0;
		double totalPower = summatePower(powerSpectrum);
		
		if(totalPower != 0.0){
			for (int i = 0; i < powerSpectrum.length; i++) {
				double spectralDeviation = i - spectralCentroid;
				totalSpread += Math
						.sqrt((spectralDeviation * spectralDeviation)
								* powerSpectrum[i] / totalPower);
			}
		}


		return totalSpread / powerSpectrum.length;
	}

	private double summatePower(double[] powerSpectrum) {
		double sum = 0.0;

		for (int i = 0; i < powerSpectrum.length; i++) {
			sum += powerSpectrum[i];
		}
		return sum;
	}

}

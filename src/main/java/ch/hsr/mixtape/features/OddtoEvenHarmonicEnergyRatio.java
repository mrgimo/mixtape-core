package ch.hsr.mixtape.features;

public class OddtoEvenHarmonicEnergyRatio {
	
	public double extractFeature(double[] powerSpectrum, int[] harmonics) {
		
		double sumEven = 0.0;
		double sumOdd = 0.0;
		
		for (int i = 0; i < harmonics.length; i++) {
			
			int frequency = harmonics[i];
			if(frequency % 2 == 0)
				sumEven += powerSpectrum[frequency] * powerSpectrum[frequency];
			else
				sumOdd += powerSpectrum[frequency] * powerSpectrum[frequency];
		}
		
		return sumEven != 0.0 ? sumOdd / sumEven : 0;
	}

}

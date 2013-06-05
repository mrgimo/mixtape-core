package ch.hsr.mixtape.features.spectral;

import java.util.List;


public class SpectralQuantizer {
	
	
	public SpectralFeaturesOfSong quantize(List<SpectralFeaturesOfWindow> featuresOfWindows) {
		SpectralFeaturesOfSong spectralFeaturesOfSong = new SpectralFeaturesOfSong();
		
		spectralFeaturesOfSong.spectralCentroid = new int[featuresOfWindows.size()];
		spectralFeaturesOfSong.spectralKurtosis = new int[featuresOfWindows.size()];
		spectralFeaturesOfSong.spectralSkewness = new int[featuresOfWindows.size()];
		spectralFeaturesOfSong.spectralSpread = new int[featuresOfWindows.size()];
		spectralFeaturesOfSong.spectralOddToEvenRatio = new int[featuresOfWindows.size()];
		
		for (int i = 0; i < featuresOfWindows.size(); i++) {
			
			SpectralFeaturesOfWindow featuresOfWindow = featuresOfWindows.get(i);
			
			spectralFeaturesOfSong.spectralCentroid[i] = quantizeCentroid(featuresOfWindow.spectralCentroid);
			spectralFeaturesOfSong.spectralKurtosis[i] = quantizeKurtosis(featuresOfWindow.spectralKurtosis);
			spectralFeaturesOfSong.spectralSkewness[i] = quantizeSkewness(featuresOfWindow.spectralSkewness);
			spectralFeaturesOfSong.spectralSpread[i] = quantizeSpread(featuresOfWindow.spectralSpread);
			spectralFeaturesOfSong.spectralOddToEvenRatio[i] = quantizeOddToEvenRatio(featuresOfWindow.spectralOddToEvenRatio);
		}
		return spectralFeaturesOfSong;
	}


	private int quantizeSpread(double spectralSpread) {
//		System.out.println("spread: " + ( (spectralSpread / 100 + 1)));
		return (int) (spectralSpread / 100) + 1;
	}


	private int quantizeSkewness(double spectralSkewness) {
//		System.out.println("skewness " + ( (spectralSkewness * 10 + 1)));
		return  (int) (spectralSkewness * 10) + 100;
	}


	private int quantizeKurtosis(double spectralKurtosis) {
//		System.out.println("kurtosis: " + ( (spectralKurtosis * 10)));
		return (int) (spectralKurtosis * 10) + 1;
	}


	private int quantizeCentroid(double spectralCentroid) {
//		System.out.println("centroid: " + ( (spectralCentroid / 100)));
		return (int) (spectralCentroid / 100) + 1;
	}


	private int quantizeOddToEvenRatio(double spectralOddToEvenRatio) {
//		System.out.println("odd to even: " + ( (spectralOddToEvenRatio * 100 + 1)));
		return (int) spectralOddToEvenRatio * 100 + 1;
	}
	

}

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
		return (int) (spectralSpread / 100);
	}


	private int quantizeSkewness(double spectralSkewness) {
		return  (int) (spectralSkewness / 100);
	}


	private int quantizeKurtosis(double spectralKurtosis) {
		return (int) (spectralKurtosis / 100);
	}


	private int quantizeCentroid(double spectralCentroid) {
		return (int) (spectralCentroid / 100);
	}


	private int quantizeOddToEvenRatio(double spectralOddToEvenRatio) {
		return (int) spectralOddToEvenRatio;
	}
	

}

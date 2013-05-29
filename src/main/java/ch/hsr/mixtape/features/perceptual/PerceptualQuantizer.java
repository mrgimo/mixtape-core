package ch.hsr.mixtape.features.perceptual;

import java.util.List;

public class PerceptualQuantizer {

	public PerceptualFeaturesOfSong quantize(
			List<PerceptualFeaturesOfWindow> featuresOfWindows) {
		
		PerceptualFeaturesOfSong perceptualFeaturesOfSong = new PerceptualFeaturesOfSong();
		
		perceptualFeaturesOfSong.mfcc1 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc2 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc3 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc4 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc5 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc6 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc7 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc8 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc9 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc10 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc11 = new int[featuresOfWindows.size()];
		perceptualFeaturesOfSong.mfcc12 = new int[featuresOfWindows.size()];
		
		for (int i = 0; i < featuresOfWindows.size(); i++) {
			
			PerceptualFeaturesOfWindow featuresOfWindow = featuresOfWindows.get(i);
			
			perceptualFeaturesOfSong.mfcc1[i] = quantizeMfcc(featuresOfWindow.mfcc1);
			perceptualFeaturesOfSong.mfcc2[i] = quantizeMfcc(featuresOfWindow.mfcc2);
			perceptualFeaturesOfSong.mfcc3[i] = quantizeMfcc(featuresOfWindow.mfcc3);
			perceptualFeaturesOfSong.mfcc4[i] = quantizeMfcc(featuresOfWindow.mfcc4);
			perceptualFeaturesOfSong.mfcc5[i] = quantizeMfcc(featuresOfWindow.mfcc5);
			perceptualFeaturesOfSong.mfcc6[i] = quantizeMfcc(featuresOfWindow.mfcc6);
			perceptualFeaturesOfSong.mfcc7[i] = quantizeMfcc(featuresOfWindow.mfcc7);
			perceptualFeaturesOfSong.mfcc8[i] = quantizeMfcc(featuresOfWindow.mfcc8);
			perceptualFeaturesOfSong.mfcc9[i] = quantizeMfcc(featuresOfWindow.mfcc9);
			perceptualFeaturesOfSong.mfcc10[i] = quantizeMfcc(featuresOfWindow.mfcc10);
			perceptualFeaturesOfSong.mfcc11[i] = quantizeMfcc(featuresOfWindow.mfcc11);
			perceptualFeaturesOfSong.mfcc12[i] = quantizeMfcc(featuresOfWindow.mfcc12);
			
		}
		
		return perceptualFeaturesOfSong;
	}

	private int quantizeMfcc(double mfcc) {
		return (int) ((mfcc + 100) * 10);
	}
	
	

}

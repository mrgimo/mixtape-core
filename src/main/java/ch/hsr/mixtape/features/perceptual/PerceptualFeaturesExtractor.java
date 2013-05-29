package ch.hsr.mixtape.features.perceptual;

import java.util.List;

import ch.hsr.mixtape.features.FeatureExtractor;

public class PerceptualFeaturesExtractor implements
		FeatureExtractor<PerceptualFeaturesOfWindow, PerceptualFeaturesOfSong> {

	@Override
	public PerceptualFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PerceptualFeaturesOfSong postprocess(List<PerceptualFeaturesOfWindow> featuresOfWindows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double distanceBetween(PerceptualFeaturesOfSong x, PerceptualFeaturesOfSong y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWindowSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWindowOverlap() {
		// TODO Auto-generated method stub
		return 0;
	}

}
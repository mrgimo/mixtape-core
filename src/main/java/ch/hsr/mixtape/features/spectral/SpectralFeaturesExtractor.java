package ch.hsr.mixtape.features.spectral;

import java.util.List;

import ch.hsr.mixtape.features.FeatureExtractor;

public class SpectralFeaturesExtractor implements FeatureExtractor<SpectralFeaturesOfWindow, SpectralFeaturesOfSong> {

	@Override
	public SpectralFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpectralFeaturesOfSong postprocess(List<SpectralFeaturesOfWindow> featuresOfWindows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double distanceBetween(SpectralFeaturesOfSong x, SpectralFeaturesOfSong y) {
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
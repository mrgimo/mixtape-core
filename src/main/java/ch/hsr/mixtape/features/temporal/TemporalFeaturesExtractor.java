package ch.hsr.mixtape.features.temporal;

import java.util.List;

import ch.hsr.mixtape.features.FeatureExtractor;

// TODO: see TempoExtractionController!
public class TemporalFeaturesExtractor implements FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

	@Override
	public TemporalFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TemporalFeaturesOfSong postprocess(List<TemporalFeaturesOfWindow> featuresOfWindows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double distanceBetween(TemporalFeaturesOfSong x, TemporalFeaturesOfSong y) {
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

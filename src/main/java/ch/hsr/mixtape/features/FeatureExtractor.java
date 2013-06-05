package ch.hsr.mixtape.features;

import java.util.List;

public interface FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> {

	FeaturesOfWindow extractFrom(double[] windowOfSamples);

	FeaturesOfSong postprocess(List<FeaturesOfWindow> featuresOfWindows);

	double distanceBetween(FeaturesOfSong x, FeaturesOfSong y);

	int getWindowSize();

	int getWindowOverlap();

}
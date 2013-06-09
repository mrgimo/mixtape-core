package ch.hsr.mixtape.processing;

import java.util.Iterator;

public interface FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> {

	FeaturesOfWindow extractFrom(double[] windowOfSamples);

	FeaturesOfSong postprocess(Iterator<FeaturesOfWindow> featuresOfWindows);

	double distanceBetween(FeaturesOfSong x, FeaturesOfSong y);

	int getWindowSize();

	int getWindowOverlap();

}
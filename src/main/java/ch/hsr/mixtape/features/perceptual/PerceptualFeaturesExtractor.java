package ch.hsr.mixtape.features.perceptual;

import static ch.hsr.mixtape.MathUtils.frequencySpectrum;
import static ch.hsr.mixtape.MathUtils.vectorLength;

import java.util.Iterator;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.metrics.NormalizedInformationDistance;

public class PerceptualFeaturesExtractor implements
		FeatureExtractor<PerceptualFeaturesOfWindow, PerceptualFeaturesOfSong> {

	private static final int WINDOW_SIZE = 512;
	private static final int WINDOW_OVERLAP = 0;
	private static final double SAMPLE_RATE = 44100;

	private static final int PERCEPTUAL_FEATURES_DIMENSION = 12;

	private NormalizedInformationDistance nid = new NormalizedInformationDistance();
	private PerceptualQuantizer perceptualQuantizer = new PerceptualQuantizer();

	private MelFrequencyCepstralCoefficients melFrequencyCepstralCoefficients = new MelFrequencyCepstralCoefficients(
			SAMPLE_RATE);

	@Override
	public PerceptualFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		PerceptualFeaturesOfWindow perceptualFeaturesOfWindow = new PerceptualFeaturesOfWindow();

		double[] magnitudeSpectrum = frequencySpectrum(windowOfSamples);
		double[] mfccs = melFrequencyCepstralCoefficients.extractFeature(windowOfSamples, magnitudeSpectrum);

		perceptualFeaturesOfWindow.mfcc1 = mfccs[1];
		perceptualFeaturesOfWindow.mfcc2 = mfccs[2];
		perceptualFeaturesOfWindow.mfcc3 = mfccs[3];
		perceptualFeaturesOfWindow.mfcc4 = mfccs[4];
		perceptualFeaturesOfWindow.mfcc5 = mfccs[5];
		perceptualFeaturesOfWindow.mfcc6 = mfccs[6];
		perceptualFeaturesOfWindow.mfcc7 = mfccs[7];
		perceptualFeaturesOfWindow.mfcc8 = mfccs[8];
		perceptualFeaturesOfWindow.mfcc9 = mfccs[9];
		perceptualFeaturesOfWindow.mfcc10 = mfccs[10];
		perceptualFeaturesOfWindow.mfcc11 = mfccs[11];
		perceptualFeaturesOfWindow.mfcc12 = mfccs[12];

		return perceptualFeaturesOfWindow;
	}

	@Override
	public PerceptualFeaturesOfSong postprocess(Iterator<PerceptualFeaturesOfWindow> featuresOfWindows) {
		return perceptualQuantizer.quantize(featuresOfWindows);
	}

	@Override
	public double distanceBetween(PerceptualFeaturesOfSong x, PerceptualFeaturesOfSong y) {

		double[] distances = new double[PERCEPTUAL_FEATURES_DIMENSION];

		distances[0] = nid.distanceBetween(x.mfcc1, y.mfcc1);
		distances[1] = nid.distanceBetween(x.mfcc2, y.mfcc2);
		distances[2] = nid.distanceBetween(x.mfcc3, y.mfcc3);
		distances[3] = nid.distanceBetween(x.mfcc4, y.mfcc4);
		distances[4] = nid.distanceBetween(x.mfcc5, y.mfcc5);
		distances[5] = nid.distanceBetween(x.mfcc6, y.mfcc6);
		distances[6] = nid.distanceBetween(x.mfcc7, y.mfcc7);
		distances[7] = nid.distanceBetween(x.mfcc8, y.mfcc8);
		distances[8] = nid.distanceBetween(x.mfcc9, y.mfcc9);
		distances[9] = nid.distanceBetween(x.mfcc10, y.mfcc10);
		distances[10] = nid.distanceBetween(x.mfcc11, y.mfcc11);
		distances[11] = nid.distanceBetween(x.mfcc12, y.mfcc12);

		return vectorLength(distances);
	}

	@Override
	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	@Override
	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
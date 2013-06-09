package ch.hsr.mixtape.processing.spectral;

import static ch.hsr.mixtape.util.MathUtils.powerSpectrum;
import static ch.hsr.mixtape.util.MathUtils.sum;
import static ch.hsr.mixtape.util.MathUtils.vectorLength;

import java.util.Iterator;

import ch.hsr.mixtape.nid.NormalizedInformationDistance;
import ch.hsr.mixtape.processing.FeatureExtractor;

public class SpectralFeaturesExtractor implements
		FeatureExtractor<SpectralFeaturesOfWindow, SpectralFeaturesOfSong> {

	private static final int WINDOW_SIZE = 512;
	private static final int WINDOW_OVERLAP = 0;
	private static final int SPECTRAL_FEATURES_DIMENSION = 5;

	private SpectralCentroid spectralCentroid = new SpectralCentroid();
	private SpectralKurtosis spectralKurtosis = new SpectralKurtosis();
	private SpectralOddToEvenRatio spectralOddToEvenRatio = new SpectralOddToEvenRatio();
	private SpectralSpread spectralSpread = new SpectralSpread();
	private SpectralSkewness spectralSkewness = new SpectralSkewness();

	private SpectralQuantizer spectralQuantizer = new SpectralQuantizer();
	private NormalizedInformationDistance nid = new NormalizedInformationDistance();

	@Override
	public SpectralFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		SpectralFeaturesOfWindow spectralFeaturesOfWindows = new SpectralFeaturesOfWindow();

		double[] powerSpectrum = powerSpectrum(windowOfSamples);
		double totalPower = sum(powerSpectrum);

		spectralFeaturesOfWindows.spectralCentroid = spectralCentroid.extractFeature(
				windowOfSamples,
				powerSpectrum,
				totalPower);

		spectralFeaturesOfWindows.spectralSpread = spectralSpread.extractFeature(
				powerSpectrum,
				spectralFeaturesOfWindows.spectralCentroid,
				totalPower);

		spectralFeaturesOfWindows.spectralOddToEvenRatio = spectralOddToEvenRatio.extractFeature(
				powerSpectrum);

		spectralFeaturesOfWindows.spectralSkewness = spectralSkewness.extractFeature(
				powerSpectrum,
				spectralFeaturesOfWindows.spectralCentroid,
				spectralFeaturesOfWindows.spectralSpread,
				totalPower);

		spectralFeaturesOfWindows.spectralKurtosis = spectralKurtosis.extracFeature(
				powerSpectrum,
				spectralFeaturesOfWindows.spectralCentroid,
				spectralFeaturesOfWindows.spectralSpread,
				totalPower);

		return spectralFeaturesOfWindows;
	}

	public SpectralFeaturesOfSong postprocess(Iterator<SpectralFeaturesOfWindow> featuresOfWindows) {
		return spectralQuantizer.quantize(featuresOfWindows);
	}

	public double distanceBetween(SpectralFeaturesOfSong x, SpectralFeaturesOfSong y) {
		double[] spectralDistances = new double[SPECTRAL_FEATURES_DIMENSION];

		spectralDistances[0] = nid.distanceBetween(x.spectralCentroid, y.spectralCentroid);
		spectralDistances[1] = nid.distanceBetween(x.spectralKurtosis, y.spectralKurtosis);
		spectralDistances[2] = nid.distanceBetween(x.spectralOddToEvenRatio, y.spectralOddToEvenRatio);
		spectralDistances[3] = nid.distanceBetween(x.spectralSkewness, y.spectralSkewness);
		spectralDistances[4] = nid.distanceBetween(x.spectralSpread, y.spectralSpread);

		return vectorLength(spectralDistances);
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
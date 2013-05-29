package ch.hsr.mixtape.features.spectral;

import java.util.List;

import org.apache.commons.math3.transform.FastFourierTransformer;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.spectral.valueMapper.SpectralValueMapper;
import ch.hsr.mixtape.metrics.NormalizedInformationDistance;
import ch.hsr.mixtape.processing.MathUtils;

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
	
	private SpectralValueMapper spectralValueMapper = new SpectralValueMapper();

	private NormalizedInformationDistance nid = new NormalizedInformationDistance();
	
	@Override
	public SpectralFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		SpectralFeaturesOfWindow spectralFeaturesOfWindows = new SpectralFeaturesOfWindow();

		double[] powerSpectrum = getPowerSpectrum(windowOfSamples);

		spectralFeaturesOfWindows.spectralCentroid = spectralCentroid
				.extractFeature(windowOfSamples, powerSpectrum);

		spectralFeaturesOfWindows.spectralSpread = spectralSpread
				.extractFeature(powerSpectrum,
						spectralFeaturesOfWindows.spectralCentroid);

		spectralFeaturesOfWindows.spectralOddToEvenRatio = spectralOddToEvenRatio
				.extractFeature(powerSpectrum);

		spectralFeaturesOfWindows.spectralSkewness = spectralSkewness
				.extractFeature(powerSpectrum,
						spectralFeaturesOfWindows.spectralCentroid,
						spectralFeaturesOfWindows.spectralSpread);

		spectralFeaturesOfWindows.spectralKurtosis = spectralKurtosis
				.extracFeature(powerSpectrum,
						spectralFeaturesOfWindows.spectralCentroid,
						spectralFeaturesOfWindows.spectralSpread);

		return spectralFeaturesOfWindows;
	}

	@Override
	public SpectralFeaturesOfSong postprocess(
			List<SpectralFeaturesOfWindow> featuresOfWindows) {
		spectralValueMapper.quantize(featuresOfWindows);
		return null;
	}

	@Override
	public double distanceBetween(SpectralFeaturesOfSong x,
			SpectralFeaturesOfSong y) {
		double[] spectralDistances = new double[SPECTRAL_FEATURES_DIMENSION];
		
		spectralDistances[0] = nid.distanceBetween(x.spectralCentroid, y.spectralCentroid);
		spectralDistances[1] = nid.distanceBetween(x.spectralKurtosis, y.spectralKurtosis);
		spectralDistances[2] = nid.distanceBetween(x.spectralOddToEvenRatio, y.spectralOddToEvenRatio);
		spectralDistances[3] = nid.distanceBetween(x.spectralSkewness, y.spectralSkewness);
		spectralDistances[4] = nid.distanceBetween(x.spectralSpread, y.spectralSpread);
		
		return MathUtils.vectorLength(spectralDistances);
	}

	@Override
	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	@Override
	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

	private double[] getPowerSpectrum(double[] samples) {
		double[] fft = MathUtils.fft(samples);
		return MathUtils.multiply(fft, fft);
	}

}
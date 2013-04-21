package ch.hsr.mixtape.extraction;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.data.SpectralFeature;
import ch.hsr.mixtape.features.FastFourierTransform;
import ch.hsr.mixtape.features.PowerSpectrum;
import ch.hsr.mixtape.features.SpectralCentroid;
import ch.hsr.mixtape.features.SpectralKurtosis;
import ch.hsr.mixtape.features.SpectralRolloffPoint;
import ch.hsr.mixtape.features.SpectralSkewness;
import ch.hsr.mixtape.features.SpectralSpread;

public class FeatureExtractor {

	private static final int WINDOW_SIZE = 1024;

	public ArrayList<SpectralFeature> extractFeatures(double[] samples) {
		ArrayList<SpectralFeature> features = new ArrayList<SpectralFeature>();

		features.addAll(extractSpectralFeatures(samples));
		return features;
	}

	private ArrayList<SpectralFeature> extractSpectralFeatures(double[] samples) {
		ArrayList<SpectralFeature> spectralFeatures = new ArrayList<SpectralFeature>();

		int windowCount = samples.length % WINDOW_SIZE == 0 ? samples.length
				/ WINDOW_SIZE : samples.length / WINDOW_SIZE + 1;

		SpectralFeature scFeature = new SpectralFeature("spectral centroid",
				windowCount);
		SpectralFeature spFeature = new SpectralFeature("spectral spread",
				windowCount);
		SpectralFeature skFeature = new SpectralFeature("spectral kurtosis",
				windowCount);
		SpectralFeature sropFeature = new SpectralFeature(
				"spectral rolloff point", windowCount);
		SpectralFeature ssFeature = new SpectralFeature("spectral skewness",
				windowCount);

		PowerSpectrum ps = new PowerSpectrum();
		SpectralCentroid sc = new SpectralCentroid();
		SpectralSpread sp = new SpectralSpread();
		SpectralKurtosis sk = new SpectralKurtosis();
		SpectralRolloffPoint srop = new SpectralRolloffPoint();
		SpectralSkewness ss = new SpectralSkewness();

		for (int windowStartIndex = 0; windowStartIndex < samples.length; windowStartIndex += WINDOW_SIZE) {

			int windowEndIndex = nextWindowEndIndex(samples, windowStartIndex);

			double[] currentWindow = Arrays.copyOfRange(samples, windowStartIndex,windowEndIndex);

			FastFourierTransform fft = new FastFourierTransform(currentWindow);

			double[] powerSpectrum = ps.extractFeature(
					fft.getRealValues(), fft.getImaginaryValues());

			double spectralCentroidValue = sc.extractFeature(currentWindow,
					powerSpectrum);
			scFeature.addWindowValue(spectralCentroidValue);

			double spectralSpreadValue = sp.extractFeature(powerSpectrum,
					spectralCentroidValue);
			spFeature.addWindowValue(spectralSpreadValue);

			double spectralKurtosisValue = sk.extracFeature(powerSpectrum,
					spectralCentroidValue, spectralSpreadValue);
			skFeature.addWindowValue(spectralKurtosisValue);

			double spectralRollofPointValue = srop
					.extractFeature(powerSpectrum);
			sropFeature.addWindowValue(spectralRollofPointValue);

			double spectralSkewnessValue = ss.extractFeature(powerSpectrum,
					spectralCentroidValue, spectralSpreadValue);
			ssFeature.addWindowValue(spectralSkewnessValue);
		}

		spectralFeatures.add(ssFeature);
		// spectralFeatures.add(sropFeature);
		spectralFeatures.add(skFeature);
		spectralFeatures.add(spFeature);
		spectralFeatures.add(scFeature);

		return spectralFeatures;
	}

	private int nextWindowEndIndex(double[] samples, int i) {
		return i + WINDOW_SIZE < samples.length ? i + WINDOW_SIZE
				: samples.length;
	}
}

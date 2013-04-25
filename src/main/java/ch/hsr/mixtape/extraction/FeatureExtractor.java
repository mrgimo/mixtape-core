package ch.hsr.mixtape.extraction;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.features.FastFourierTransform;
import ch.hsr.mixtape.features.PowerSpectrum;
import ch.hsr.mixtape.features.SpectralCentroid;
import ch.hsr.mixtape.features.SpectralKurtosis;
import ch.hsr.mixtape.features.SpectralRolloffPoint;
import ch.hsr.mixtape.features.SpectralSkewness;
import ch.hsr.mixtape.features.SpectralSpread;

public class FeatureExtractor {

	private static final int WINDOW_SIZE = 1024;

	public ArrayList<Feature> extractFeatures(double[] samples) {
		ArrayList<Feature> features = new ArrayList<Feature>();

		features.addAll(extractSpectralFeatures(samples));
		return features;
	}

	private ArrayList<Feature> extractSpectralFeatures(double[] samples) {
		ArrayList<Feature> spectralFeatures = new ArrayList<Feature>();

		int windowCount = samples.length % WINDOW_SIZE == 0 ? samples.length
				/ WINDOW_SIZE : samples.length / WINDOW_SIZE + 1;

		Feature scFeature = new Feature("spectral centroid",
				windowCount);
		Feature spFeature = new Feature("spectral spread",
				windowCount);
		Feature skFeature = new Feature("spectral kurtosis",
				windowCount);
		Feature sropFeature = new Feature(
				"spectral rolloff point", windowCount);
		Feature ssFeature = new Feature("spectral skewness",
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

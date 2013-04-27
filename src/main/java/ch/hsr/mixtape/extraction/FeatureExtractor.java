package ch.hsr.mixtape.extraction;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.data.SpectralCentroidFeature;
import ch.hsr.mixtape.distancefunction.skew.LCP;
import ch.hsr.mixtape.distancefunction.skew.SkewInteger;
import ch.hsr.mixtape.features.FastFourierTransform;
import ch.hsr.mixtape.features.MFCC;
import ch.hsr.mixtape.features.MagnitudeSpectrum;
import ch.hsr.mixtape.features.PowerSpectrum;
import ch.hsr.mixtape.features.SpectralCentroid;
import ch.hsr.mixtape.features.SpectralKurtosis;
import ch.hsr.mixtape.features.SpectralRolloffPoint;
import ch.hsr.mixtape.features.SpectralSkewness;
import ch.hsr.mixtape.features.SpectralSpread;

public class FeatureExtractor {

	private static final int WINDOW_SIZE = 512;
	
	SkewInteger skew = new SkewInteger();
	LCP lcp = new LCP();

	public ArrayList<SpectralCentroidFeature> extractFeatures(double[] samples) {
		ArrayList<SpectralCentroidFeature> features = new ArrayList<SpectralCentroidFeature>();

		features.addAll(extractSpectralFeatures(samples));
		return features;
	}

	private ArrayList<SpectralCentroidFeature> extractSpectralFeatures(double[] samples) {
		ArrayList<SpectralCentroidFeature> spectralFeatures = new ArrayList<SpectralCentroidFeature>();

		int windowCount = samples.length % WINDOW_SIZE == 0 ? samples.length
				/ WINDOW_SIZE : samples.length / WINDOW_SIZE + 1;

		SpectralCentroidFeature scFeature = new SpectralCentroidFeature("spectral centroid",
				windowCount);
		SpectralCentroidFeature spFeature = new SpectralCentroidFeature("spectral spread",
				windowCount);
		SpectralCentroidFeature skFeature = new SpectralCentroidFeature("spectral kurtosis",
				windowCount);
		SpectralCentroidFeature sropFeature = new SpectralCentroidFeature(
				"spectral rolloff point", windowCount);
		SpectralCentroidFeature ssFeature = new SpectralCentroidFeature("spectral skewness",
				windowCount);
		
		MFCC mfcc = new MFCC();
		MagnitudeSpectrum ms = new MagnitudeSpectrum();
		
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
			double[] magnitudeSpectrum = ms.extractFeature(fft.getRealValues(), fft.getImaginaryValues());
			try {
				double[] mfccs = mfcc.extractFeature(samples, 44100, magnitudeSpectrum );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		computeSuffixTree(scFeature);
		
		spectralFeatures.add(ssFeature);
		// spectralFeatures.add(sropFeature);
//		spectralFeatures.add(skFeature);
		spectralFeatures.add(spFeature);
		spectralFeatures.add(scFeature);

		return spectralFeatures;
	}

	private void computeSuffixTree(SpectralCentroidFeature feature) {
		
		int[] values = feature.getValues();
		
		int[] suffixArray = skew.buildSuffixArray(values, feature.maxValue());
		int[] lcpValues = lcp.longestCommonPrefixes(values, suffixArray);
		
		feature.setLcp(lcpValues);
		feature.setSuffixArray(suffixArray);
		
	}

	private int nextWindowEndIndex(double[] samples, int i) {
		return i + WINDOW_SIZE < samples.length ? i + WINDOW_SIZE
				: samples.length;
	}
}

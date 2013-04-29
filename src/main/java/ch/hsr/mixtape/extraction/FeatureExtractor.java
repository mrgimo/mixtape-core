package ch.hsr.mixtape.extraction;

import java.util.ArrayList;
import java.util.Arrays;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.valuemapper.MFCCValueMapper;
import ch.hsr.mixtape.data.valuemapper.SpectralCentroidValueMaper;
import ch.hsr.mixtape.data.valuemapper.SpectralKurtosisValueMapper;
import ch.hsr.mixtape.data.valuemapper.SpectralSkewnessValueMapper;
import ch.hsr.mixtape.data.valuemapper.SpectralSpreadValueMapper;
import ch.hsr.mixtape.distancefunction.skew.LCP;
import ch.hsr.mixtape.distancefunction.skew.NFCA;
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

	private SkewInteger skew = new SkewInteger();
	private LCP lcp = new LCP();
	private NFCA nfca = new NFCA();
	
	
	public ArrayList<Feature> extractFeatures(double[] samples) {
		ArrayList<Feature> features = new ArrayList<Feature>();

		features.addAll(extractSpectralFeatures(samples));
		return features;
	}

	private ArrayList<Feature> extractSpectralFeatures(double[] samples) {
		
		ArrayList<Feature> features = new ArrayList<Feature>();

		int windowCount = samples.length % WINDOW_SIZE == 0 ? samples.length
				/ WINDOW_SIZE : samples.length / WINDOW_SIZE + 1;

		Feature scFeature = new Feature("spectral centroid",
				windowCount, new SpectralCentroidValueMaper());
		Feature spFeature = new Feature("spectral spread",
				windowCount, new SpectralSpreadValueMapper());
		Feature skFeature = new Feature("spectral kurtosis",
				windowCount, new SpectralKurtosisValueMapper());
		Feature sropFeature = new Feature(
				"spectral rolloff point", windowCount, new SpectralCentroidValueMaper());
		Feature ssFeature = new Feature("spectral skewness",
				windowCount, new SpectralSkewnessValueMapper());
		
		Feature mfccFeature1 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		Feature mfccFeature2 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		Feature mfccFeature3 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		Feature mfccFeature4 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		Feature mfccFeature5 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		Feature mfccFeature6 = new Feature("mfcc", windowCount, new MFCCValueMapper());
		
		
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
//
//			double spectralRollofPointValue = srop
//					.extractFeature(powerSpectrum);
//			sropFeature.addWindowValue(spectralRollofPointValue);
//
			double spectralSkewnessValue = ss.extractFeature(powerSpectrum,
					spectralCentroidValue, spectralSpreadValue);
			ssFeature.addWindowValue(spectralSkewnessValue);
			
			double[] magnitudeSpectrum = ms.extractFeature(fft.getRealValues(), fft.getImaginaryValues());
			try {
				double[] mfccs = mfcc.extractFeature(samples, 44100, magnitudeSpectrum );
				mfccFeature1.addWindowValue(mfccs[1]);
				mfccFeature2.addWindowValue(mfccs[2]);
				mfccFeature3.addWindowValue(mfccs[3]);
				mfccFeature4.addWindowValue(mfccs[4]);
				mfccFeature5.addWindowValue(mfccs[5]);
				mfccFeature6.addWindowValue(mfccs[6]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		computeSuffixTreeInformation(scFeature);
		computeSuffixTreeInformation(skFeature);
		computeSuffixTreeInformation(spFeature);
		computeSuffixTreeInformation(ssFeature);
		
		computeSuffixTreeInformation(mfccFeature1);
		computeSuffixTreeInformation(mfccFeature2);
		computeSuffixTreeInformation(mfccFeature3);
		computeSuffixTreeInformation(mfccFeature4);
		computeSuffixTreeInformation(mfccFeature5);
		computeSuffixTreeInformation(mfccFeature6);
		
//		spectralFeatures.add(sropFeature);
		features.add(ssFeature);
		features.add(skFeature);
		features.add(spFeature);
		features.add(scFeature);
		
		features.add(mfccFeature1);
		features.add(mfccFeature2);
		features.add(mfccFeature3);
		features.add(mfccFeature4);
		features.add(mfccFeature5);
		features.add(mfccFeature6);

		return features;
	}

	private void computeSuffixTreeInformation(Feature feature) {
		
		int[] values = feature.windowValues();
		
		int[] suffixArray = skew.buildSuffixArray(values, feature.maxValue());
		int[] lcpValues = lcp.longestCommonPrefixes(values, suffixArray);
		int[] nfcas = nfca.numberOfFirsCommontAncestors(lcpValues);
		
		feature.setSuffixArray(suffixArray);
		feature.setLcp(lcpValues);
		feature.setNFCAs(nfcas);
		
	}

	private int nextWindowEndIndex(double[] samples, int i) {
		return i + WINDOW_SIZE < samples.length ? i + WINDOW_SIZE
				: samples.length;
	}
}

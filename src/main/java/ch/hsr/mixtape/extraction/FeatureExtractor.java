package ch.hsr.mixtape.extraction;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.features.FastFourierTransform;
import ch.hsr.mixtape.features.PowerSpectrum;
import ch.hsr.mixtape.features.RMS;
import ch.hsr.mixtape.features.SpectralCentroid;
import ch.hsr.mixtape.features.SpectralKurtosis;
import ch.hsr.mixtape.features.SpectralRolloffPoint;
import ch.hsr.mixtape.features.SpectralSkewness;
import ch.hsr.mixtape.features.SpectralSpread;
import ch.hsr.mixtape.features.ZeroCrossings;

public class FeatureExtractor {
	

	public ArrayList<Feature<?>>  extractFeatures(double[] samples) {
		ArrayList<Feature<?>> features = new ArrayList<Feature<?>>();
		
		features.add(extractRMS(samples));
		features.add(extractZC(samples));
		features.addAll(extractSpectralFeatures(samples));
		return features;
	}

	private ArrayList<Feature<?>> extractSpectralFeatures(double[] samples) {
		ArrayList<Feature<?>> spectralFeatures = new ArrayList<Feature<?>>();
		
		FastFourierTransform fft = new FastFourierTransform(samples);
		double[] powerSpectrum = extractPowerSpectrum(fft);
		
		SpectralCentroid sc = new SpectralCentroid();
		double spectralCentroidValue = sc.extractFeature(samples, powerSpectrum);
		Feature<Double> spectralCentroidFeature = new Feature<Double>("spectral centroid", spectralCentroidValue);
		spectralFeatures.add(spectralCentroidFeature);
		
		SpectralSpread sp = new SpectralSpread();
		double spectralSpreadValue = sp.extractFeature(powerSpectrum, spectralCentroidValue);
		Feature<Double> spectralSpreadFeature = new Feature<Double>("spectral spread", spectralSpreadValue);
		spectralFeatures.add(spectralSpreadFeature);
		
		SpectralKurtosis sk = new SpectralKurtosis();
		double spectralKurtosisValue = sk.extracFeature(powerSpectrum, spectralCentroidValue, spectralSpreadValue);
		Feature<Double> spectralKurtosisFeature = new Feature<Double>("spectral kurtosis", spectralKurtosisValue);
		spectralFeatures.add(spectralKurtosisFeature);
		
		SpectralRolloffPoint srop = new SpectralRolloffPoint();
		double spectralRollofPointValue = srop.extractFeature(powerSpectrum);
		Feature<Double> spectralRolloffPointFeature = new Feature<Double>("spectral rolloff point", spectralRollofPointValue);
		spectralFeatures.add(spectralRolloffPointFeature);
		
		SpectralSkewness ss = new SpectralSkewness();
		double spectralSkewnessValue = ss.extractFeature(powerSpectrum, spectralCentroidValue, spectralSpreadValue);
		Feature<Double> spectralSkewnessFeature = new Feature<Double>("spectral skewness", spectralSkewnessValue);
		spectralFeatures.add(spectralSkewnessFeature);
		
		return spectralFeatures;
	}

	private double[] extractPowerSpectrum(FastFourierTransform fft) {
		PowerSpectrum powerSpectrum = new PowerSpectrum();
		return powerSpectrum.extractFeature(fft.getRealValues(), fft.getImaginaryValues());
	}

	private Feature<Long> extractZC(double[] samples) {
		
		ZeroCrossings zc = new ZeroCrossings();
		long zcValue = zc.extractFeature(samples);
		Feature<Long> zcFeature = new Feature<Long>("zc", zcValue);
		return zcFeature;
	}

	private Feature<Double> extractRMS(double[] samples) {
		
		RMS rms = new RMS();
	
		double rmsValue = rms.extractFeature(samples);
		Feature<Double> rmsFeature = new Feature<Double>("rms", rmsValue);
		return rmsFeature;
	}
	

}

package ch.hsr.mixtape.features.controller;

import java.util.ArrayList;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.features.RMS;
import ch.hsr.mixtape.features.ZeroCrossings;

public class FeatureController {
	
	private ZeroCrossings zc = new ZeroCrossings();

	public ArrayList<Feature<?>>  extractFeatures(double[] samples) {
		ArrayList<Feature<?>> features = new ArrayList<Feature<?>>();
		
		features.add(extractRMS(samples));
		features.add(extractZC(samples));
		return features;
	}

	private Feature<Long> extractZC(double[] samples) {
		
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

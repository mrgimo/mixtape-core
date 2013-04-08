package ch.hsr.mixtape.features.controller;

import java.util.ArrayList;

import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.features.RMS;
import ch.hsr.mixtape.features.ZeroCrossings;

public class FeatureController {
	
	private RMS rms = new RMS();
	private ZeroCrossings zc = new ZeroCrossings();

	public void extractFeatures(ArrayList<Song> songs) {
		for (Song song : songs) {
			FeatureVector featureVector = new FeatureVector();
			featureVector.RMS = rms.extractFeature(song.getSamples());
			featureVector.ZC = zc.extractFeature(song.getSamples());
			song.setFeatureVector(featureVector);
		}
	}
	

}

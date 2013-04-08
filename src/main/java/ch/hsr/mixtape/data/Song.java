package ch.hsr.mixtape.data;

import java.io.File;

import ch.hsr.mixtape.distancefunction.DistanceFunction;
import ch.hsr.mixtape.distancefunction.NormalizedCompressionDistance;


public class Song {

	private String name;
	private File audioFile;
	private double[] samples;

	DistanceFunction distanceFunction = new NormalizedCompressionDistance();

	private FeatureVector featureVector = new FeatureVector();

	public Song(String name, double[] samples) {
		this.name = name;
		this.samples = samples;
	}

	public Song(File audoFile) {
		audioFile = audoFile;
		this.name = audioFile.getName();
	}

	public String getName() {
		return name;
	}

	public double[] getSamples() {
		return samples;
	}

	public void setFeatureVector(FeatureVector featureVector) {
		this.featureVector = featureVector;
	}

	public FeatureVector getFeatureVector() {
		return featureVector;
	}

	public double distanceTo(Song song) {
		double d = distanceFunction.computeDistance(featureVector.getFeatureValues(), song.getFeatureVector().getFeatureValues());
		System.out.println(d);
		return d;
	}

	public File getAudioFile() {
		return audioFile;
	}
}

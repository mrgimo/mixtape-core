package ch.hsr.mixtape.data;

import java.io.File;

import ch.hsr.mixtape.distancefunction.DistanceFunction;
import ch.hsr.mixtape.distancefunction.NormalizedCompressionDistance;


public class Song {

	private String name;
	private File audioFile;

	DistanceFunction distanceFunction = new NormalizedCompressionDistance();

	private FeatureVector featureVector = new FeatureVector();

	public Song(File audoFile) {
		audioFile = audoFile;
		this.name = audioFile.getName();
	}

	public String getName() {
		return name;
	}

	public void setFeatureVector(FeatureVector featureVector) {
		this.featureVector = featureVector;
	}

	public FeatureVector getFeatureVector() {
		return featureVector;
	}

	public double distanceTo(Song song) {
		return distanceFunction.computeDistance(featureVector, song.getFeatureVector());
	}

	public File getAudioFile() {
		return audioFile;
	}
}

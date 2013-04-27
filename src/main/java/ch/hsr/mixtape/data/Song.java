package ch.hsr.mixtape.data;

import java.io.File;

import ch.hsr.mixtape.distancefunction.KolmogorovDistance;


public class Song {

	private String name;
	private File audioFile;

	private KolmogorovDistance distanceFunction = new KolmogorovDistance();
	private FeatureVector featureVector = new FeatureVector();

	public Song(File audoFile) {
		audioFile = audoFile;
		this.name = audioFile.getName();
	}
	
	public Song() {
		
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
		return distanceFunction.distance(this, song);
	}

	public File getAudioFile() {
		return audioFile;
	}
}

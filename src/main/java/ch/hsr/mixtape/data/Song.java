package ch.hsr.mixtape.data;

import java.io.File;
import java.util.HashMap;


public class Song {

	private String name;
	private File audioFile;

	private FeatureVector featureVector = new FeatureVector();
	
	private HashMap<Song, Double> distances = new HashMap<Song, Double>();

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
		return distances.containsKey(song) ? distances.get(song) : 1;
	}

	public File getAudioFile() {
		return audioFile;
	}

	public void setDistance(Song songToCompare, double distance) {
		distances.put(songToCompare, distance);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}

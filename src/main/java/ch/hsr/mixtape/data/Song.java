package ch.hsr.mixtape.data;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


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
	
	public void printSongsWithDistances(Logger logger) {
		ValueComperator vc = new ValueComperator(distances);
		TreeMap<Song, Double> sorted = new TreeMap<Song, Double>(vc);
		sorted.putAll(distances);
		
		logger.log(Level.INFO, "\n\nDistances from " + name + " to: \n");
		for (Map.Entry<Song, Double> entry : sorted.entrySet()) {
			logger.log(Level.INFO, "Song: " + entry.getKey().getName() + "\tDistance: " + entry.getValue() + "\n");
		}
	}
	
	@Override
	public boolean equals(Object song) {
		return this.name.equals(((Song) song).getName());
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
static class ValueComperator implements Comparator<Song> {
		
		Map<Song, Double> base;

		public ValueComperator(HashMap<Song, Double> base) {
			this.base = base;
		}

		@Override
		public int compare(Song x, Song y) {
			Double valX = base.get(x);
			Double valY = base.get(y);
			
			if(valX.equals(valY))
				return x.getName().compareTo(y.getName());
		
			return valX.compareTo(valY);
		}
		
	}

}

package ch.hsr.mixtape.data;


public class Song {

	private String name;
	private double[] samples;

	private FeatureVector featureVector = new FeatureVector();

	public Song(String name, double[] samples) {
		this.name = name;
		this.samples = samples;
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
		return Math.sqrt((featureVector.RMS - song.getFeatureVector().RMS) * (featureVector.RMS - song.getFeatureVector().RMS) + 
		(featureVector.ZC - song.getFeatureVector().ZC) * (featureVector.ZC - song.getFeatureVector().ZC));
	}
}

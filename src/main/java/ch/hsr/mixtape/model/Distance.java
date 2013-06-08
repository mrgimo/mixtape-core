package ch.hsr.mixtape.model;

public class Distance {

	private Song songX;
	private Song songY;

	private double harmonicDistance;
	private double perceptualDistance;
	private double spectralDistance;
	private double temporalDistance;

	public Distance(Song songX, Song songY,
			double harmonicDistance, double perceptualDistance, double spectralDistance, double temporalDistance) {
		this.songX = songX;
		this.songY = songY;

		this.harmonicDistance = harmonicDistance;
		this.perceptualDistance = perceptualDistance;
		this.spectralDistance = spectralDistance;
		this.temporalDistance = temporalDistance;
	}

	public Song getSongX() {
		return songX;
	}

	public Song getSongY() {
		return songY;
	}

	public double getHarmonicDistance() {
		return harmonicDistance;
	}

	public double getPerceptualDistance() {
		return perceptualDistance;
	}

	public double getSpectralDistance() {
		return spectralDistance;
	}

	public double getTemporalDistance() {
		return temporalDistance;
	}

}
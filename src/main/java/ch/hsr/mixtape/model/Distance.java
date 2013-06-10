package ch.hsr.mixtape.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

@Entity
@NamedQuery(name = "getAllDistances", query = "SELECT d FROM Distance d")
public class Distance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@OneToOne
	private Song songX;

	@OneToOne
	private Song songY;

	private double harmonicDistance;
	private double perceptualDistance;
	private double spectralDistance;
	private double temporalDistance;

	public Distance() {
	}

	public Distance(Song songX, Song songY, double harmonicDistance,
			double perceptualDistance, double spectralDistance,
			double temporalDistance) {
		this.songX = songX;
		this.songY = songY;

		this.harmonicDistance = harmonicDistance;
		this.perceptualDistance = perceptualDistance;
		this.spectralDistance = spectralDistance;
		this.temporalDistance = temporalDistance;
	}

	public int gedId() {
		return id;
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
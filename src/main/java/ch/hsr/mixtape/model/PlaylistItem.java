package ch.hsr.mixtape.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Stefan Derungs
 */
@Entity
public class PlaylistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private Song current;

	/**
	 * This field should be set to true, if the current item was selected by a
	 * user. This should be false, if a song is selected by pathfinding.
	 */
	private boolean userWish;

	private Song antecessor;

	private int harmonic;

	private int spectral;

	private int temporal;

	private int perceptual;

	public PlaylistItem() {
	}

	public PlaylistItem(Song current, Song antecessor, int harmonic,
			int perceptual, int spectral, int temporal, boolean isUserWish) {
		this.current = current;
		this.userWish = isUserWish;
		this.antecessor = antecessor;
		this.harmonic = harmonic;
		this.perceptual = perceptual;
		this.spectral = spectral;
		this.temporal = temporal;
	}

	public Song getCurrent() {
		return current;
	}

	public boolean isUserWish() {
		return userWish;
	}

	public void setUserWish(boolean userWish) {
		this.userWish = userWish;
	}

	public Song getAntecessor() {
		return antecessor;
	}

	public void resetAntecessor(Song antecessor, int harmonic,
			int perceptual, int spectral, int temporal) {
		this.antecessor = antecessor;
		this.harmonic = harmonic;
		this.perceptual = perceptual;
		this.spectral = spectral;
		this.temporal = temporal;
	}

	public int getTotal() {
		return (harmonic + perceptual + spectral + temporal) / 4;
	}

	public int getHarmonic() {
		return harmonic;
	}
	
	public int getPerceptual() {
		return perceptual;
	}

	public int getSpectral() {
		return spectral;
	}
	
	public int getTemporal() {
		return temporal;
	}

}

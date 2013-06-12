package ch.hsr.mixtape.model;


/**
 * @author Stefan Derungs
 */
public class PlaylistItem {

	private Song current;

	/**
	 * This field should be set to true, if the current item was selected by a
	 * user. This should be false, if a song is selected by pathfinding.
	 */
	private boolean userWish;

	private Song antecessorSimilarity;

	private int harmonicSimilarity;

	private int spectralSimilarity;

	private int temporalSimilarity;

	private int perceptualSimilarity;

	public PlaylistItem() {
	}

	public PlaylistItem(Song current, Song antecessor, int harmonicSimilarity,
			int perceptualSimilarity, int spectralSimilarity, int temporalSimilarity, boolean isUserWish) {
		this.current = current;
		this.userWish = isUserWish;
		this.antecessorSimilarity = antecessor;
		this.harmonicSimilarity = harmonicSimilarity;
		this.perceptualSimilarity = perceptualSimilarity;
		this.spectralSimilarity = spectralSimilarity;
		this.temporalSimilarity = temporalSimilarity;
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
		return antecessorSimilarity;
	}
	
	public void setAntecessor(Song antecessor) {
		this.antecessorSimilarity = antecessor;
	}

	public int getTotal() {
		return (harmonicSimilarity + perceptualSimilarity + spectralSimilarity + temporalSimilarity) / 4;
	}

	public int getHarmonicSimilarity() {
		return harmonicSimilarity;
	}
	
	public int getPerceptualSimilarity() {
		return perceptualSimilarity;
	}

	public int getSpectralSimilarity() {
		return spectralSimilarity;
	}
	
	public int getTemporalSimilarity() {
		return temporalSimilarity;
	}
	
	public void setHarmonicSimilarity(int harmonicSimilarity) {
		this.harmonicSimilarity = harmonicSimilarity;
	}
	
	public void setPerceptualSimilarity(int perceptualSimilarity) {
		this.perceptualSimilarity = perceptualSimilarity;
	}
	
	public void setSpectralSimilarity(int spectralSimilarity) {
		this.spectralSimilarity = spectralSimilarity;
	}
	
	public void setTemporalSimilarity(int temporalSimilarity) {
		this.temporalSimilarity = temporalSimilarity;
	}

}

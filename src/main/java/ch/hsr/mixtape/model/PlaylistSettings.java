package ch.hsr.mixtape.model;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSettings {

	/**
	 * Song from where to start building a playlist.
	 */
	private List<Song> startSongs = new ArrayList<Song>();

	/**
	 * Minimum amount of playtime to set up initial playlist.
	 */
	private int startLengthInMinutes;

	/**
	 * Minimum number of songs to fill initial playlist with.
	 */
	private int startLengthInSongs;

	private int harmonicSimilarity;

	private int perceptualSimilarity;

	private int spectralSimilarity;

	private int temporalSimilarity;

	private boolean allowDuplicates = false;

	public List<Song> getStartSongs() {
		return startSongs;
	}

	public void setStartSongs(List<Song> startSongs) {
		this.startSongs = startSongs;
	}

	public int getStartLengthInMinutes() {
		return startLengthInMinutes;
	}

	public void setStartLengthInMinutes(int startLengthInMinutes) {
		this.startLengthInMinutes = startLengthInMinutes;
	}

	public int getStartLengthInSongs() {
		return startLengthInSongs;
	}

	public void setStartLengthInSongs(int startLengthInSongs) {
		this.startLengthInSongs = startLengthInSongs;
	}

	public int getHarmonicSimilarity() {
		return harmonicSimilarity;
	}

	public void setHarmonicSimilarity(int harmonicSimilarity) {
		this.harmonicSimilarity = harmonicSimilarity;
	}

	public int getPerceptualSimilarity() {
		return perceptualSimilarity;
	}

	public void setPerceptualSimilarity(int perceptualSimilarity) {
		this.perceptualSimilarity = perceptualSimilarity;
	}

	public int getSpectralSimilarity() {
		return spectralSimilarity;
	}

	public void setSpectralSimilarity(int spectralSimilarity) {
		this.spectralSimilarity = spectralSimilarity;
	}

	public int getTemporalSimilarity() {
		return temporalSimilarity;
	}

	public void setTemporalSimilarity(int temporalSimilarity) {
		this.temporalSimilarity = temporalSimilarity;
	}

	public boolean allowDuplicates() {
		return allowDuplicates;
	}

	public void allowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

}

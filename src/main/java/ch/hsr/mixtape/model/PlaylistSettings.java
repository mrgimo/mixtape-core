package ch.hsr.mixtape.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class PlaylistSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Temporal(TemporalType.DATE)
	private Date creationDate;

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

	public PlaylistSettings() {
		creationDate = new Date();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		// Do nothing.
		// Just keep it for JSP's sake.
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		// Do nothing.
		// Just keep it for JSP's sake.
	}

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

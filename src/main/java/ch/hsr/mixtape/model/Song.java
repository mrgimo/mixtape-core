package ch.hsr.mixtape.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.hsr.mixtape.application.MusicDirectoryScanner;

/**
 * @author Stefan Derungs
 */
@NamedQueries({
		@NamedQuery(name = "countAllSongs", query = "SELECT COUNT(s) FROM Song s"),
		@NamedQuery(name = "getAllSongs", query = "SELECT s FROM Song s"),
		@NamedQuery(name = "countPendingSongs", query = "SELECT COUNT(s) FROM Song s WHERE s.analyzeDate IS NULL"),
		@NamedQuery(name = "getPendingSongs", query = "SELECT s FROM Song s WHERE s.analyzeDate IS NULL"),
		@NamedQuery(name = "countAnalyzedSongs", query = "SELECT COUNT(s) FROM Song s WHERE s.analyzeDate IS NOT NULL"),
		@NamedQuery(name = "getAnalysedSongs", query = "SELECT s FROM Song s WHERE s.analyzeDate IS NOT NULL"),
		@NamedQuery(name = "findSongsByTerm", query = "SELECT s FROM Song s WHERE s.title LIKE :term OR s.artist LIKE :term OR s.album LIKE :term") })
@Entity
public class Song {

	public static enum SongStatusType {
		New, Analyzing, Analyzed
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date lastModified;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, updatable = false)
	private Date scanDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	private Date analyzeDate;

	private int lengthInSeconds;

	private int sampleRateInHz;

	/**
	 * Filepath relative to
	 * {@link MusicDirectoryScanner#MUSIC_DIRECTORY_FILEPATH}.
	 */
	@Column(unique = true)
	private String relativeFilepath;

	private String title;

	private String album;

	private String artist;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private FeaturesOfSong features;

	public Song() {
	}

	/**
	 * 
	 * @param relativeFilepath
	 * @throws IllegalArgumentException
	 *             If there are any problems accessing the provided filepath.
	 */
	public Song(String relativeFilepath, Date scanDate)
			throws IllegalArgumentException {
		this();
		this.relativeFilepath = relativeFilepath;
		this.scanDate = scanDate;
	}

	/**
	 * TODO: remove in the future
	 * 
	 * @deprecated
	 */
	public Song(int id, String filepath) {
		this();
		this.id = id;
		this.relativeFilepath = filepath;
	}

	@PrePersist
	@PreUpdate
	protected void onUpdate() {
		lastModified = new Date();
	}

	public int getId() {
		return id;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public Date getAnalyzeDate() {
		return analyzeDate;
	}

	public void setAnalyzeDate(Date analyzeDate) {
		this.analyzeDate = analyzeDate;
	}

	public int getLengthInSeconds() {
		return lengthInSeconds;
	}

	public void setLengthInSeconds(int lengthInSeconds) {
		this.lengthInSeconds = lengthInSeconds;
	}

	public int getSampleRateInHz() {
		return sampleRateInHz;
	}

	public void setSampleRateInHz(int sampleRateInHz) {
		this.sampleRateInHz = sampleRateInHz;
	}

	public String getFilepath() {
		return relativeFilepath;
	}

	public void setFilepath(String relativeFilepath) {
		this.relativeFilepath = relativeFilepath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public FeaturesOfSong getFeatures() {
		return features;
	}

	public void setFeatures(FeaturesOfSong features) {
		this.features = features;
	}

	@Override
	public int hashCode() {
		return relativeFilepath.hashCode();
	}

	@Override
	public boolean equals(Object anObject) {
		return this.id == ((Song) anObject).id;
	}

	@Override
	public String toString() {
		return "===========\nId: " + id + "\nFilepath: " + relativeFilepath
				+ "\nTitle: " + title + "\nArtist: " + artist + "\nAlbum: "
				+ album + "\nLengthInSeconds: " + lengthInSeconds
				+ "\nSampleRateInHz: " + sampleRateInHz + "\n===========";
	}

}

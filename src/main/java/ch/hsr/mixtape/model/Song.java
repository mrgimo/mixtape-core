package ch.hsr.mixtape.model;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import ch.hsr.mixtape.application.SongTagExtractor;

/**
 * @author Stefan Derungs
 */
@NamedQueries({
		@NamedQuery(name = "getAllSongs", query = "SELECT s FROM Song s"),
		@NamedQuery(name = "getNewSongs", query = "SELECT s FROM Song s WHERE s.analyzeDate IS NULL"),
		@NamedQuery(name = "getAnalysedSongs", query = "SELECT s FROM Song s WHERE s.analyzeDate IS NOT NULL"),
		@NamedQuery(name = "findSongsByTerm", query = "SELECT s FROM Song s WHERE s.title LIKE :term OR s.artist LIKE :term OR s.album LIKE :term") })
@Entity
public class Song {

	public static enum SongStatusType {
		New, Analyzing, Analyzed
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Temporal(TemporalType.DATE)
	private Date lastModified;

	@Temporal(TemporalType.DATE)
	private Date scanDate;

	@Temporal(TemporalType.DATE)
	private Date analyzeDate;

	private int lengthInSeconds;

	private int sampleRateInHz;

	private String filepath;

	private String title;

	private String album;

	private String artist;

	// @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Transient
	private FeaturesOfSong features;

	public Song() {
		scanDate = new Date();
	}

	public Song(String filepath, String title, String artist, String album) {
		this();
		this.filepath = filepath;
		this.title = title;
		this.artist = artist;
		this.album = album;
	}

	/**
	 * 
	 * @param filepath
	 * @throws IllegalArgumentException
	 *             If there are any problems accessing the provided filepath.
	 */
	public Song(String filepath) throws IllegalArgumentException {
		this();
		checkFilepathIsValid(filepath);
		this.filepath = filepath;

		SongTagExtractor extractor = new SongTagExtractor();
		extractor.extractTagsFromSong(this);
	}

	/**
	 * TODO: remove in the future
	 * @deprecated
	 */
	public Song(int id, String filepath) {
		this.id = id;
		this.filepath = filepath;
	}

	/**
	 * Checks if the provided filepath is valid by format, reachable and if the
	 * file is readable.
	 * 
	 * @return If everything is ok, true is returned. False else.
	 * @throws IllegalArgumentException
	 *             If there are any problems accessing the provided filepath.
	 */
	private boolean checkFilepathIsValid(String filepath)
			throws IllegalArgumentException {
		try {
			Path path = Paths.get(filepath);
			if (!Files.isRegularFile(path))
				throw new IllegalArgumentException(
						"The provided filepath cannot be resolved to a "
								+ "regular file or the file does not exist.");

			if (!Files.isReadable(path)) {
				throw new IllegalArgumentException(
						"The file represented by the provided filepath is not "
								+ "readable. Please make sure you have proper access "
								+ "rights to this file (" + filepath + ").");
			}

			return true;
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException(
					"The provided filepath is incorrect (" + filepath + ").");
		} catch (SecurityException e) {
			throw new IllegalArgumentException(
					"There exist security restrictions which deny the access "
							+ "to the provided filepath.");
		}
	}

	@PrePersist
	private void updateModifiedDate() {
		lastModified = new Date();
	}

	public long getId() {
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
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
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

	@Override
	public int hashCode() {
		return title.hashCode();
	}

	@Override
	public boolean equals(Object anObject) {
		return this.id == ((Song) anObject).id;
	}

	@Override
	public String toString() {
		return "===========\nFilepath: " + filepath + "\nTitle: " + title
				+ "\nArtist: " + artist + "\nAlbum: " + album
				+ "\nLengthInSeconds: " + lengthInSeconds
				+ "\nSampleRateInHz: " + sampleRateInHz + "\n===========";
	}
}

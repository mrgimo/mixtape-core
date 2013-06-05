package ch.hsr.mixtape.model;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import ch.hsr.mixtape.application.SongTagExtractor;

/**
 * @author Stefan Derungs
 */
@Entity
public class Song {

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

	@Transient
	private boolean userWish;

	@Transient
	private boolean inStreamQueue;
	
	@Transient
	private SongSimilarity songSimilarity;

	private static Random random = new Random(); // TODO: remove as soon as
													// working with real data

	public Song() {
		id = random.nextLong(); // TODO: remove as soon as working with real
								// data
		scanDate = new Date();
	}

	public Song(String filepath, String title, String artist, String album,
			boolean isUserWish) {
		this();
		this.filepath = filepath;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.userWish = isUserWish;
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

	public SongSimilarity getSongSimilarity() {
		return songSimilarity;
	}

	public void setSongSimilarity(SongSimilarity similarity) {
		this.songSimilarity = similarity;
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

	public boolean isUserWish() {
		return userWish;
	}

	public void setUserWish(boolean userWish) {
		this.userWish = userWish;
	}

	public boolean isInStreamQueue() {
		return inStreamQueue;
	}

	public void setInStreamQueue(boolean inStreamQueue) {
		this.inStreamQueue = inStreamQueue;
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

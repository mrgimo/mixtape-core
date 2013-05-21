package ch.hsr.mixtape.model;

import java.util.Date;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

	private String filepath;

	private String title;

	private String album;

	private String artist;

	private boolean userWish;

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

	public String getTitle() {
		return title;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
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

	@Override
	public int hashCode() {
		return title.hashCode();
	}

}

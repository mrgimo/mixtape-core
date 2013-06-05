package ch.hsr.mixtape.domain;


public class Song {

	private final int id;

	private final String filePath;

	public Song(int id, String filePath) {
		this.id = id;
		this.filePath = filePath;
	}

	public int getId() {
		return id;
	}

	public String getFilePath() {
		return filePath;
	}

}

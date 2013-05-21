package ch.hsr.mixtape.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SongSimilarity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private Song current;

	private Song antecessor;

	private int total;

	private int rhythmic;

	private int melodic;

	private int mfcc;

	private int perceptional;
	
	public SongSimilarity() {
	}

	public SongSimilarity(Song current, Song antecessor, int total, int rhythmic,
			int melodic, int mfcc, int perceptional) {
		this.current = current;
		this.antecessor = antecessor;
		this.total = total;
		this.rhythmic = rhythmic;
		this.melodic = melodic;
		this.mfcc = mfcc;
		this.perceptional = perceptional;
	}

	public Song getCurrent() {
		return current;
	}

	public Song getAntecessor() {
		return antecessor;
	}

	public int getTotal() {
		return total;
	}

	public int getRhythmic() {
		return rhythmic;
	}

	public int getMelodic() {
		return melodic;
	}

	public int getMfcc() {
		return mfcc;
	}

	public int getPerceptional() {
		return perceptional;
	}

}

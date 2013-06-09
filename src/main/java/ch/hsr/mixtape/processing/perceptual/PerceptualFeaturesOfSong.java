package ch.hsr.mixtape.processing.perceptual;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PerceptualFeaturesOfSong implements Serializable {

	private static final long serialVersionUID = 6188651484736279216L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	public int[] mfcc1;
	public int[] mfcc2;
	public int[] mfcc3;
	public int[] mfcc4;
	public int[] mfcc5;
	public int[] mfcc6;
	public int[] mfcc7;
	public int[] mfcc8;
	public int[] mfcc9;
	public int[] mfcc10;
	public int[] mfcc11;
	public int[] mfcc12;

}

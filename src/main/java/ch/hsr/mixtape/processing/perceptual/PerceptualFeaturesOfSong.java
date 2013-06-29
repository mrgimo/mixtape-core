package ch.hsr.mixtape.processing.perceptual;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import ch.hsr.mixtape.processing.Feature;

@Entity
public class PerceptualFeaturesOfSong implements Serializable {

	private static final long serialVersionUID = 6188651484736279216L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc1 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc2 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc3 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc4 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc5 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc6 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc7 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc8 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc9 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc10 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc11 = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature mfcc12 = new Feature();

}

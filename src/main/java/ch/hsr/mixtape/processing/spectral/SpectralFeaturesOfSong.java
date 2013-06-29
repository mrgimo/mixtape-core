package ch.hsr.mixtape.processing.spectral;

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
public class SpectralFeaturesOfSong implements Serializable {
	
	private static final long serialVersionUID = -2292333611837659872L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature spectralCentroid = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature spectralKurtosis = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature spectralOddToEvenRatio = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature spectralSkewness = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature spectralSpread = new Feature();

}

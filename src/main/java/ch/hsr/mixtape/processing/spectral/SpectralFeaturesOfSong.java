package ch.hsr.mixtape.processing.spectral;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SpectralFeaturesOfSong implements Serializable {
	
	private static final long serialVersionUID = -2292333611837659872L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	public int[] spectralCentroid;
	public int[] spectralKurtosis;
	public int[] spectralOddToEvenRatio;
	public int[] spectralSkewness;
	public int[] spectralSpread;

}

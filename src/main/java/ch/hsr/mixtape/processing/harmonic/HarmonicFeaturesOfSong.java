package ch.hsr.mixtape.processing.harmonic;

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
public class HarmonicFeaturesOfSong implements Serializable {

	private static final long serialVersionUID = -1562206373425514927L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature fundamentals = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature inharmonicity = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature oddToEvenEnergyRatio = new Feature();
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature tristimulus = new Feature();

}

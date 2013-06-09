package ch.hsr.mixtape.processing.harmonic;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class HarmonicFeaturesOfSong implements Serializable {

	private static final long serialVersionUID = -1562206373425514927L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	public int[] fundamentals;
	public int[] inharmonicity;
	public int[] oddToEvenHarmonicEnergyRatio;
	public int[] tristimulus;

}

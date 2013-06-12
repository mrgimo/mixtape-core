package ch.hsr.mixtape.processing.temporal;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TemporalFeaturesOfSong implements Serializable {
	
	private static final long serialVersionUID = -7369688455752075199L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	public int[] beats;

}

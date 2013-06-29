package ch.hsr.mixtape.processing.temporal;

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
public class TemporalFeaturesOfSong implements Serializable {
	
	private static final long serialVersionUID = -7369688455752075199L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Feature beats = new Feature();

}

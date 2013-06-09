package ch.hsr.mixtape.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import ch.hsr.mixtape.processing.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.processing.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.processing.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.processing.temporal.TemporalFeaturesOfSong;

/**
 * @author Stefan Derungs
 */
@Entity
public class FeaturesOfSong {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Lob
	public HarmonicFeaturesOfSong harmonic;
	
	@Lob
	public PerceptualFeaturesOfSong perceptual;
	
	@Lob
	public SpectralFeaturesOfSong spectral;
	
	@Lob
	public TemporalFeaturesOfSong temporal;
	
	public FeaturesOfSong() {
	}

	public FeaturesOfSong(HarmonicFeaturesOfSong harmonic,
			PerceptualFeaturesOfSong perceptual,
			SpectralFeaturesOfSong spectral,
			TemporalFeaturesOfSong temporal) {
		this.harmonic = harmonic;
		this.perceptual = perceptual;
		this.spectral = spectral;
		this.temporal = temporal;
	}

}

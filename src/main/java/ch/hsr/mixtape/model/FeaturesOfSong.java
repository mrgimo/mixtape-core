package ch.hsr.mixtape.model;

import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfSong;

/**
 * @author Stefan Derungs
 */
public class FeaturesOfSong {
	
	public HarmonicFeaturesOfSong harmonic;
	
	public PerceptualFeaturesOfSong perceptual;
	
	public SpectralFeaturesOfSong spectral;
	
	public TemporalFeaturesOfSong temporal;
	
}

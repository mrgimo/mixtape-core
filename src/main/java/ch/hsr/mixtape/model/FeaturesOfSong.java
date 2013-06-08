package ch.hsr.mixtape.model;

import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesOfSong;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesOfSong;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesOfSong;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesOfSong;

/**
 * @author Stefan Derungs
 */
public class FeaturesOfSong {

	public final HarmonicFeaturesOfSong harmonic;
	public final PerceptualFeaturesOfSong perceptual;
	public final SpectralFeaturesOfSong spectral;
	public final TemporalFeaturesOfSong temporal;

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

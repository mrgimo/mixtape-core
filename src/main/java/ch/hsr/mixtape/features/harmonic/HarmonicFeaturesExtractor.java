package ch.hsr.mixtape.features.harmonic;

import java.util.List;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.processing.MathUtils;

public class HarmonicFeaturesExtractor implements FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> {

	private static final int WINDOW_SIZE = 4096;
	private static final int WINDOW_OVERLAP = 2048;

	private FundamentalFrequencies fundamentals;
	private Harmonics harmonics;

	private Inharmonicity inharmonicity;
	private OddToEvenHarmonicEnergyRatio oddToEvenHarmonicEnergyRatio;

	public HarmonicFeaturesOfWindow extractFrom(double[] window) {
		double[] frequencySpectrum = MathUtils.fft(window);
		double[] powerSpectrum = MathUtils.multiply(frequencySpectrum, frequencySpectrum);

		HarmonicFeaturesOfWindow harmony = new HarmonicFeaturesOfWindow();

		harmony.fundamentals = fundamentals.extract(frequencySpectrum);
		harmony.harmonics = harmonics.extract(powerSpectrum, harmony.fundamentals);
		harmony.inharmonicity = inharmonicity.extract(powerSpectrum, harmony.fundamentals, harmony.harmonics);
		harmony.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio.extract(powerSpectrum, harmony.harmonics);

		return harmony;
	}

	public HarmonicFeaturesOfSong postprocess(List<HarmonicFeaturesOfWindow> x) {
		return null;
	}

	public double distanceBetween(HarmonicFeaturesOfSong x, HarmonicFeaturesOfSong y) {
		return 0;
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
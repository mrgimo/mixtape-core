package ch.hsr.mixtape.features.harmonic;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.metrics.Metric;
import ch.hsr.mixtape.processing.MathUtils;

public class HarmonyExtractor implements FeatureExtractor<Harmony> {

	private static final int WINDOW_SIZE = 4096;
	private static final int WINDOW_OVERLAP = 2048;

	private FundamentalFrequencies fundamentals;
	private Harmonics harmonics;

	private Inharmonicity inharmonicity;
	private OddToEvenHarmonicEnergyRatio oddToEvenHarmonicEnergyRatio;

	public Harmony extract(double[] window) {
		double[] frequencySpectrum = MathUtils.fft(window);
		double[] powerSpectrum = MathUtils.multiply(frequencySpectrum, frequencySpectrum);

		Harmony harmony = new Harmony();

		harmony.fundamentals = fundamentals.extract(frequencySpectrum);

		harmony.harmonics = harmonics.extract(
				powerSpectrum,
				harmony.fundamentals);

		harmony.inharmonicity = inharmonicity.extract(powerSpectrum,
				harmony.fundamentals,
				harmony.harmonics);

		harmony.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio.extract(
				powerSpectrum,
				harmony.harmonics);

		return harmony;
	}

	public Metric<Harmony> getMetric() {
		return null;
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
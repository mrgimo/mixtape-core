package ch.hsr.mixtape.features.harmonic;

import java.util.List;

import com.google.common.primitives.Ints;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.processing.MathUtils;

public class HarmonicFeaturesExtractor implements FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> {

	private static final int WINDOW_SIZE = 4096;
	private static final int WINDOW_OVERLAP = 2048;

	private FundamentalFrequencies fundamentals = new FundamentalFrequencies(WINDOW_SIZE);
	private Harmonics harmonics = new Harmonics();

	private Inharmonicity inharmonicity = new Inharmonicity();
	private OddToEvenHarmonicEnergyRatio oddToEvenHarmonicEnergyRatio = new OddToEvenHarmonicEnergyRatio();

	public HarmonicFeaturesOfWindow extractFrom(double[] window) {
		double[] frequencySpectrum = MathUtils.fft(window);
		double[] powerSpectrum = MathUtils.multiply(frequencySpectrum, frequencySpectrum);

		HarmonicFeaturesOfWindow featuresOfWindow = new HarmonicFeaturesOfWindow();

		featuresOfWindow.fundamentals = fundamentals.extract(frequencySpectrum);
		featuresOfWindow.harmonics = harmonics.extract(powerSpectrum, featuresOfWindow.fundamentals);
		featuresOfWindow.inharmonicity = inharmonicity.extract(powerSpectrum, featuresOfWindow.fundamentals,
				featuresOfWindow.harmonics);
		featuresOfWindow.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio.extract(powerSpectrum,
				featuresOfWindow.harmonics);

		return featuresOfWindow;
	}

	public HarmonicFeaturesOfSong postprocess(List<HarmonicFeaturesOfWindow> featuresOfWindows) {
		HarmonicFeaturesOfSong featuresOfSong = new HarmonicFeaturesOfSong();

		int[] fundamentals = new int[0];
		int[] harmonics = new int[0];
		int[] inharmonicity = new int[featuresOfWindows.size()];
		int[] oddToEvenHarmonicEnergyRatio = new int[featuresOfWindows.size()];

		for (int i = 0; i < featuresOfWindows.size(); i++) {
			HarmonicFeaturesOfWindow featuresOfWindow = featuresOfWindows.get(i);

			fundamentals = Ints.concat(fundamentals, featuresOfWindow.fundamentals);
			harmonics = Ints.concat(harmonics, featuresOfWindow.harmonics);
			inharmonicity[i] = quantizeInharmonicity(featuresOfWindow.inharmonicity);
			oddToEvenHarmonicEnergyRatio[i] = quantizeOddToEvenHarmonicEnergyRatio(featuresOfWindow.oddToEvenHarmonicEnergyRatio);
		}

		featuresOfSong.fudamentals = fundamentals;
		featuresOfSong.harmonics = harmonics;
		featuresOfSong.inharmonicity = inharmonicity;
		featuresOfSong.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio;

		return featuresOfSong;
	}

	private int quantizeInharmonicity(double inharmonicity) {
		return 0;
	}

	private int quantizeOddToEvenHarmonicEnergyRatio(double oddToEvenHarmonicEnergyRatio) {
		return 0;
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
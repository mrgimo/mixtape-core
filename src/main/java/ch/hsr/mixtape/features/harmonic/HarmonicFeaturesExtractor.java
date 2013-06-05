package ch.hsr.mixtape.features.harmonic;

import java.util.List;

import ch.hsr.mixtape.MathUtils;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.metrics.NormalizedInformationDistance;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;

public class HarmonicFeaturesExtractor implements FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> {

	private static final int WINDOW_SIZE = 4096;
	private static final int WINDOW_OVERLAP = 2048;

	private FundamentalFrequencies fundamentals = new FundamentalFrequencies(WINDOW_SIZE);
	private Harmonics harmonics = new Harmonics();

	private Inharmonicity inharmonicity = new Inharmonicity();
	private OddToEvenHarmonicEnergyRatio oddToEvenHarmonicEnergyRatio = new OddToEvenHarmonicEnergyRatio();
	private Tristimulus tristimulus = new Tristimulus();

	private NormalizedInformationDistance nid = new NormalizedInformationDistance();

	public HarmonicFeaturesOfWindow extractFrom(double[] window) {
		double[] frequencySpectrum = MathUtils.frequencySpectrum(window);
		double[] powerSpectrum = MathUtils.multiply(frequencySpectrum, frequencySpectrum);

		HarmonicFeaturesOfWindow featuresOfWindow = new HarmonicFeaturesOfWindow();

		featuresOfWindow.fundamentals = fundamentals.extract(frequencySpectrum);

		int binOfFundamental = featuresOfWindow.fundamentals[0];
		int[] binsOfHarmonics = harmonics.extract(powerSpectrum, binOfFundamental);

		featuresOfWindow.inharmonicity = inharmonicity.extract(powerSpectrum, binOfFundamental,
				binsOfHarmonics);

		featuresOfWindow.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio.extract(powerSpectrum,
				binsOfHarmonics);

		featuresOfWindow.tristimulus = tristimulus.extract(powerSpectrum, binsOfHarmonics);

		return featuresOfWindow;
	}

	public HarmonicFeaturesOfSong postprocess(List<HarmonicFeaturesOfWindow> featuresOfWindows) {
		HarmonicFeaturesOfSong featuresOfSong = new HarmonicFeaturesOfSong();

		int[] fundamentals = new int[0];
		int[] inharmonicity = new int[featuresOfWindows.size()];
		int[] oddToEvenHarmonicEnergyRatio = new int[featuresOfWindows.size()];
		int[] tristimulus = new int[0];

		for (int i = 0; i < featuresOfWindows.size(); i++) {
			HarmonicFeaturesOfWindow featuresOfWindow = featuresOfWindows.get(i);

			fundamentals = Ints.concat(fundamentals, quantizeFundamentals(featuresOfWindow.fundamentals));
			inharmonicity[i] = quantizeInharmonicity(featuresOfWindow.inharmonicity);
			oddToEvenHarmonicEnergyRatio[i] = quantizeOddToEvenHarmonicEnergyRatio(featuresOfWindow.oddToEvenHarmonicEnergyRatio);
			tristimulus = Ints.concat(tristimulus, quantizeTristimulus(featuresOfWindow.tristimulus));
		}

		featuresOfSong.fudamentals = fundamentals;
		featuresOfSong.inharmonicity = inharmonicity;
		featuresOfSong.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio;
		featuresOfSong.tristimulus = tristimulus;

		return featuresOfSong;
	}

	private int[] quantizeFundamentals(int[] binsOfFundamentals) {
		int[] pianoKeys = new int[binsOfFundamentals.length];
		for (int i = 0; i < pianoKeys.length; i++)
			pianoKeys[i] = frequencyToPianoKey(MathUtils.binToFrequency(binsOfFundamentals[i], 44100, WINDOW_SIZE));

		return pianoKeys;
	}

	private int frequencyToPianoKey(double f) {
		return (int) (12 * DoubleMath.log2(f / 440.0) + 48);
	}

	private int quantizeInharmonicity(double inharmonicity) {
		return (int) (inharmonicity * 100) + 1;
	}

	private int quantizeOddToEvenHarmonicEnergyRatio(double oddToEvenHarmonicEnergyRatio) {
		return (int) (oddToEvenHarmonicEnergyRatio * 10) + 1;
	}

	private int[] quantizeTristimulus(double[] tristimulus) {
		int[] quantized = new int[tristimulus.length];
		for (int i = 0; i < quantized.length; i++)
			quantized[i] = (int) (tristimulus[i] * 10) + 1;

		return quantized;
	}

	public double distanceBetween(HarmonicFeaturesOfSong x, HarmonicFeaturesOfSong y) {
		return MathUtils.vectorLength(
				nid.distanceBetween(x.fudamentals, y.fudamentals),
				nid.distanceBetween(x.inharmonicity, y.inharmonicity),
				nid.distanceBetween(x.oddToEvenHarmonicEnergyRatio, y.oddToEvenHarmonicEnergyRatio),
				nid.distanceBetween(x.tristimulus, y.tristimulus));
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
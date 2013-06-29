package ch.hsr.mixtape.processing.harmonic;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.nid.NormalizedInformationDistance;
import ch.hsr.mixtape.processing.FeatureExtractor;
import ch.hsr.mixtape.util.MathUtils;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;

public class HarmonicFeaturesExtractor implements FeatureExtractor<HarmonicFeaturesOfWindow, HarmonicFeaturesOfSong> {

	private static final int WINDOW_SIZE = 4096;
	private static final int WINDOW_OVERLAP = 2048;
	
	private static final int NUMBER_OF_HARMONIC_FEATURES = 4;
	private static final double NORMALIZATION_FACTOR = 1.0 / FastMath.sqrt(NUMBER_OF_HARMONIC_FEATURES);

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

		featuresOfWindow.inharmonicity = inharmonicity.extract(
				powerSpectrum,
				binOfFundamental,
				binsOfHarmonics);

		featuresOfWindow.oddToEvenHarmonicEnergyRatio = oddToEvenHarmonicEnergyRatio.extract(
				powerSpectrum,
				binsOfHarmonics);

		featuresOfWindow.tristimulus = tristimulus.extract(
				powerSpectrum,
				binsOfHarmonics);

		return featuresOfWindow;
	}

	public HarmonicFeaturesOfSong postprocess(Iterator<HarmonicFeaturesOfWindow> featuresOfWindows) {
		List<Integer> fundamentals = Lists.newArrayList();
		List<Integer> inharmonicity = Lists.newArrayList();
		List<Integer> oddToEvenHarmonicEnergyRatio = Lists.newArrayList();
		List<Integer> tristimulus = Lists.newArrayList();

		while (featuresOfWindows.hasNext()) {
			HarmonicFeaturesOfWindow featuresOfWindow = featuresOfWindows.next();

			fundamentals.addAll(quantizeFundamentals(featuresOfWindow.fundamentals));
			inharmonicity.add(quantizeInharmonicity(featuresOfWindow.inharmonicity));
			oddToEvenHarmonicEnergyRatio.add(quantizeOddToEvenHarmonicEnergyRatio(featuresOfWindow.oddToEvenHarmonicEnergyRatio));
			tristimulus.addAll(quantizeTristimulus(featuresOfWindow.tristimulus));
		}

		HarmonicFeaturesOfSong featuresOfSong = new HarmonicFeaturesOfSong();

		featuresOfSong.fundamentals.values = Ints.toArray(fundamentals);
		featuresOfSong.inharmonicity.values = Ints.toArray(inharmonicity);
		featuresOfSong.oddToEvenEnergyRatio.values = Ints.toArray(oddToEvenHarmonicEnergyRatio);
		featuresOfSong.tristimulus.values = Ints.toArray(tristimulus);

		return featuresOfSong;
	}

	private List<Integer> quantizeFundamentals(int[] binsOfFundamentals) {
		List<Integer> pianoKeys = Lists.newArrayList(binsOfFundamentals.length);
		for (int i = 0; i < binsOfFundamentals.length; i++)
			pianoKeys.add(frequencyToPianoKey(MathUtils.binToFrequency(binsOfFundamentals[i], 44100, WINDOW_SIZE)));

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

	private List<Integer> quantizeTristimulus(double[] tristimulus) {
		List<Integer> quantized = Lists.newArrayList(tristimulus.length);
		for (int i = 0; i < tristimulus.length; i++)
			quantized.add((int) (tristimulus[i] * 10) + 1);

		return quantized;
	}

	public double distanceBetween(HarmonicFeaturesOfSong x, HarmonicFeaturesOfSong y) {
		return MathUtils.vectorLength(
				nid.distanceBetween(x.fundamentals, y.fundamentals),
				nid.distanceBetween(x.inharmonicity, y.inharmonicity),
				nid.distanceBetween(x.oddToEvenEnergyRatio, y.oddToEvenEnergyRatio),
				nid.distanceBetween(x.tristimulus, y.tristimulus))
				* NORMALIZATION_FACTOR;
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
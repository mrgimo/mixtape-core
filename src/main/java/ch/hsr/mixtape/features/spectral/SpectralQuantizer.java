package ch.hsr.mixtape.features.spectral;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class SpectralQuantizer {

	public SpectralFeaturesOfSong quantize(Iterator<SpectralFeaturesOfWindow> featuresOfWindows) {
		List<Integer> spectralCentroid = Lists.newArrayList();
		List<Integer> spectralKurtosis = Lists.newArrayList();
		List<Integer> spectralSkewness = Lists.newArrayList();
		List<Integer> spectralSpread = Lists.newArrayList();
		List<Integer> spectralOddToEvenRatio = Lists.newArrayList();

		while (featuresOfWindows.hasNext()) {
			SpectralFeaturesOfWindow featuresOfWindow = featuresOfWindows.next();

			spectralCentroid.add(quantizeCentroid(featuresOfWindow.spectralCentroid));
			spectralKurtosis.add(quantizeKurtosis(featuresOfWindow.spectralKurtosis));
			spectralSkewness.add(quantizeSkewness(featuresOfWindow.spectralSkewness));
			spectralSpread.add(quantizeSpread(featuresOfWindow.spectralSpread));
			spectralOddToEvenRatio.add(quantizeOddToEvenRatio(featuresOfWindow.spectralOddToEvenRatio));
		}

		SpectralFeaturesOfSong spectralFeaturesOfSong = new SpectralFeaturesOfSong();

		spectralFeaturesOfSong.spectralCentroid = Ints.toArray(spectralCentroid);
		spectralFeaturesOfSong.spectralKurtosis = Ints.toArray(spectralKurtosis);
		spectralFeaturesOfSong.spectralSkewness = Ints.toArray(spectralSkewness);
		spectralFeaturesOfSong.spectralSpread = Ints.toArray(spectralSpread);
		spectralFeaturesOfSong.spectralOddToEvenRatio = Ints.toArray(spectralOddToEvenRatio);

		return spectralFeaturesOfSong;
	}

	private int quantizeSpread(double spectralSpread) {
		// System.out.println("spread: " + ( (spectralSpread / 100 + 1)));
		return (int) (spectralSpread / 100) + 1;
	}

	private int quantizeSkewness(double spectralSkewness) {
		// System.out.println("skewness " + ( (spectralSkewness * 10 + 1)));
		return (int) (spectralSkewness * 10) + 100;
	}

	private int quantizeKurtosis(double spectralKurtosis) {
		// System.out.println("kurtosis: " + ( (spectralKurtosis * 10)));
		return (int) (spectralKurtosis * 10) + 1;
	}

	private int quantizeCentroid(double spectralCentroid) {
		// System.out.println("centroid: " + ( (spectralCentroid / 100)));
		return (int) (spectralCentroid / 100) + 1;
	}

	private int quantizeOddToEvenRatio(double spectralOddToEvenRatio) {
		// System.out.println("odd to even: " + ( (spectralOddToEvenRatio * 100
		// + 1)));
		return (int) spectralOddToEvenRatio * 100 + 1;
	}

}

package ch.hsr.mixtape.processing.perceptual;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class PerceptualQuantizer {

	public PerceptualFeaturesOfSong quantize(Iterator<PerceptualFeaturesOfWindow> featuresOfWindows) {
		List<Integer> mfcc1 = Lists.newArrayList();
		List<Integer> mfcc2 = Lists.newArrayList();
		List<Integer> mfcc3 = Lists.newArrayList();
		List<Integer> mfcc4 = Lists.newArrayList();
		List<Integer> mfcc5 = Lists.newArrayList();
		List<Integer> mfcc6 = Lists.newArrayList();
		List<Integer> mfcc7 = Lists.newArrayList();
		List<Integer> mfcc8 = Lists.newArrayList();
		List<Integer> mfcc9 = Lists.newArrayList();
		List<Integer> mfcc10 = Lists.newArrayList();
		List<Integer> mfcc11 = Lists.newArrayList();
		List<Integer> mfcc12 = Lists.newArrayList();

		while (featuresOfWindows.hasNext()) {
			PerceptualFeaturesOfWindow featuresOfWindow = featuresOfWindows.next();

			mfcc1.add(quantizeMfcc(featuresOfWindow.mfcc1));
			mfcc2.add(quantizeMfcc(featuresOfWindow.mfcc2));
			mfcc3.add(quantizeMfcc(featuresOfWindow.mfcc3));
			mfcc4.add(quantizeMfcc(featuresOfWindow.mfcc4));
			mfcc5.add(quantizeMfcc(featuresOfWindow.mfcc5));
			mfcc6.add(quantizeMfcc(featuresOfWindow.mfcc6));
			mfcc7.add(quantizeMfcc(featuresOfWindow.mfcc7));
			mfcc8.add(quantizeMfcc(featuresOfWindow.mfcc8));
			mfcc9.add(quantizeMfcc(featuresOfWindow.mfcc9));
			mfcc10.add(quantizeMfcc(featuresOfWindow.mfcc10));
			mfcc11.add(quantizeMfcc(featuresOfWindow.mfcc11));
			mfcc12.add(quantizeMfcc(featuresOfWindow.mfcc12));
		}

		PerceptualFeaturesOfSong perceptualFeaturesOfSong = new PerceptualFeaturesOfSong();

		perceptualFeaturesOfSong.mfcc1.values = Ints.toArray(mfcc1);
		perceptualFeaturesOfSong.mfcc2.values = Ints.toArray(mfcc2);
		perceptualFeaturesOfSong.mfcc3.values = Ints.toArray(mfcc3);
		perceptualFeaturesOfSong.mfcc4.values = Ints.toArray(mfcc4);
		perceptualFeaturesOfSong.mfcc5.values = Ints.toArray(mfcc5);
		perceptualFeaturesOfSong.mfcc6.values = Ints.toArray(mfcc6);
		perceptualFeaturesOfSong.mfcc7.values = Ints.toArray(mfcc7);
		perceptualFeaturesOfSong.mfcc8.values = Ints.toArray(mfcc8);
		perceptualFeaturesOfSong.mfcc9.values = Ints.toArray(mfcc9);
		perceptualFeaturesOfSong.mfcc10.values = Ints.toArray(mfcc10);
		perceptualFeaturesOfSong.mfcc11.values = Ints.toArray(mfcc11);
		perceptualFeaturesOfSong.mfcc12.values = Ints.toArray(mfcc12);

		return perceptualFeaturesOfSong;
	}

	private int quantizeMfcc(double mfcc) {
		return (int) (mfcc + 100) + 1;
	}

}
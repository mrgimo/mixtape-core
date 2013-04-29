package ch.hsr.mixtape.distancefunction.skew;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import ch.hsr.mixtape.Mixer;
import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.valuemapper.SpectralCentroidValueMaper;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistance;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistance;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistanceSpeedUp;

public class KolmogorovDistanceTest {

	public void testSameValues() {
		int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 7);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song = new Song();
		
		
		Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		f.setNFCAs(nfcas);
		f.addWindowValues(input);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		
		NormalizedInformationDistance dist = new NormalizedInformationDistance();
		double act = dist.distance(song, song);
		
		
		assertEquals(0, act, 0);
	}
	
	public void completeDifferentValuesTest() {
		
	}
	
	public void halfToFullCommonValuesTest() {
		int[] input2 = new int[] { 1, 2, 3, 4, 5, 1, 2, 3, 4, 5};
		int[] input = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5};

		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 9);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song = new Song();
		
		
		Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		f.setNFCAs(nfcas);
		f.addWindowValues(input);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		int[] suffixArray2 = new SkewInteger().buildSuffixArray(input2, 9);
		int[] lcp2 = new LCP().longestCommonPrefixes(input2, suffixArray);
		int[] nfcas2 = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song2 = new Song();
		
		
		Feature f2 = new Feature("", 12, new SpectralCentroidValueMaper());
		f2.setLcp(lcp2);
		f2.setSuffixArray(suffixArray2);
		f2.setNFCAs(nfcas2);
		f2.addWindowValues(input2);
		FeatureVector fv2 = new FeatureVector();
		fv2.addFeature(f2);
		song2.setFeatureVector(fv2);
		
		
		NormalizedInformationDistance dist = new NormalizedInformationDistance();
		double act = dist.distance(song, song2);
		
		assertEquals(0, act, 0);
	}
	
	public void halfToHalfCommonValuesTest() {
		int[] input2 = new int[] { 1, 2, 3, 4, 5, 8, 8, 8, 8, 8};
		int[] input = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5};
		
		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 9);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song = new Song();
		
		
		Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		f.setNFCAs(nfcas);
		f.addWindowValues(input);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		int[] suffixArray2 = new SkewInteger().buildSuffixArray(input2, 9);
		int[] lcp2 = new LCP().longestCommonPrefixes(input2, suffixArray);
		int[] nfcas2 = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song2 = new Song();
		
		
		Feature f2 = new Feature("", 12, new SpectralCentroidValueMaper());
		f2.setLcp(lcp2);
		f2.setSuffixArray(suffixArray2);
		f2.setNFCAs(nfcas2);
		f2.addWindowValues(input2);
		FeatureVector fv2 = new FeatureVector();
		fv2.addFeature(f2);
		song2.setFeatureVector(fv2);
		
		
		NormalizedInformationDistance dist = new NormalizedInformationDistance();
		double act = dist.distance(song, song2);
		
		assertEquals(0.5, act, 0);
	}
	
	@Test
	public void testRandomValues() {
//		int[] input2 = new int[] { 8, 8, 8, 9, 9, 8, 8, 8, 8, 8};
//		int[] input = new int[] { 1, 1, 2, 9, 9, 1, 2, 3, 4, 5};
		
		int[] input = new int[3000];
		int[] input2 = new int[3000];
		
		for (int i = 0; i < input.length; i++) {
			input2[i] = new Random().nextInt(1600) + 1;
			input[i] = new Random().nextInt(1600) + 1;
		}
		
		
		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 3000);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Song song = new Song();
		
		
		Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		f.setNFCAs(nfcas);
		f.addWindowValues(input);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		int[] suffixArray2 = new SkewInteger().buildSuffixArray(input2, 3000);
		int[] lcp2 = new LCP().longestCommonPrefixes(input2, suffixArray2);
		int[] nfcas2 = new NFCA().numberOfFirsCommontAncestors(lcp2);
		
		Song song2 = new Song();
		
		
		Feature f2 = new Feature("", 12, new SpectralCentroidValueMaper());
		f2.setLcp(lcp2);
		f2.setSuffixArray(suffixArray2);
		f2.setNFCAs(nfcas2);
		f2.addWindowValues(input2);
		FeatureVector fv2 = new FeatureVector();
		fv2.addFeature(f2);
		song2.setFeatureVector(fv2);
		
		
		NormalizedInformationDistance dist = new NormalizedInformationDistance();
		NormalizedInformationDistanceSpeedUp dist2 = new NormalizedInformationDistanceSpeedUp();
		
		double act = dist.distance(song, song2);
		double act2 = dist2.distance(song, song2);
		
		System.out.println("dist1: " + act + " dist2: " + act2);
	}
	

	
	public void testActualSong() {
		
		NormalizedInformationDistance distFunc = new NormalizedInformationDistance();
		NormalizedInformationDistanceSpeedUp distFunc2 = new NormalizedInformationDistanceSpeedUp();

		ArrayList<Song> extractAudioData = new Mixer().extractAudioData();
		
//		for (int i = 0; i < extractAudioData.size(); i++) {
//			for (int j = i; j < extractAudioData.size(); j++) {
//				Song song = extractAudioData.get(i);
//				Song song2 = extractAudioData.get(j);
//				
//			}
//		}
	}
}

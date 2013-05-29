package ch.hsr.mixtape.metrics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hsr.mixtape.data.valuemapper.FooMapper;
import ch.hsr.mixtape.distancefunction.DistanceFunction;
import ch.hsr.mixtape.distancefunction.LZ77V2;
import ch.hsr.mixtape.domain.Feature2;
import ch.hsr.mixtape.domain.FeatureVector;
import ch.hsr.mixtape.domain.Song;

public class KolmogorovDistanceTest {

	@Test
	public void testSameValues() {
		int[] input = new int[] { 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7, 2, 3, 4, 5, 7, 4, 5, 6, 7, 3, 2};
		Song song = createSong(input, 7);

		DistanceFunction dist = new LZ77V2();
		
		double expectedDistance = 0.0;
		double actualDistance = dist.distance(song, song);
		
		
		assertEquals(expectedDistance, actualDistance, 0);
	}
	
//	public void completeDifferentValuesTest() {
//		
//	}
//	
	@Test
	public void halfToFullCommonValuesTest() {
		int[] input = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5, 9, 3, 4};
		int[] input2 = new int[] { 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2};
		
		Song song = createSong(input, 9);
		Song song2 = createSong(input2, 5);
		
		DistanceFunction dist = new LZ77V2();
		
		double expectedDistance = 0.5;
		
		double actualDistance = dist.distance(song, song2);
		
		assertEquals(expectedDistance, actualDistance, 0);
	}
	
	@Test
	public void halfToHalfCommonValuesTest() {

		int[] input = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5};
		int[] input2 = new int[] { 1, 2, 3, 4, 5, 8, 8, 8, 8, 8};
		
		Song song = createSong(input, 9);
		Song song2 = createSong(input2, 8);
		
		DistanceFunction dist = new LZ77V2();
		
		double expectedDistance = 0.5;
		double actualDistance = dist.distance(song, song2);
		
		
		assertEquals(expectedDistance, actualDistance, 0);
	}
//	
//	
//	@Test
//	public void testCompareDistanceFunctions() {
//		
//		int maxValue = 1600;
//		
//		int[] input = new int[3000];
//		int[] input2 = new int[3000];
//		
//		for (int i = 0; i < input.length; i++) {
//			input2[i] = new Random().nextInt(maxValue) + 1;
//			input[i] = new Random().nextInt(maxValue) + 1;
//		}
//		
//		
//		Song song = createSong(input2, maxValue);
//		Song song2 = createSong(input2, maxValue);
//		
//		NormalizedInformationDistance dist = new NormalizedInformationDistance();
//		NormalizedInformationDistanceSpeedUp dist2 = new NormalizedInformationDistanceSpeedUp();
//		
//		double actualDistanceF1 = dist.distance(song, song2);
//		double actualDistanceF2 = dist2.distance(song, song2);
//		
//		assertEquals(actualDistanceF1, actualDistanceF2, 0);
//		
//	}
	

	public Song createSong(int[] input, int maxValue) {

		Song song = new Song();
		
		int[] suffixArray = new SuffixArrayBuilder().buildSuffixArray(input, maxValue);
		int[] lcp = new LcpBuilder().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Feature2 f = new Feature2("foo feature", input.length, new FooMapper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		f.setNFCAs(nfcas);
		f.addWindowValues(input);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		return song;
	}
}
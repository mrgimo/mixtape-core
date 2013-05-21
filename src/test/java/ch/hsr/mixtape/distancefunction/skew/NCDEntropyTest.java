package ch.hsr.mixtape.distancefunction.skew;

import org.junit.Test;
import static org.junit.Assert.*;

import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.valuemapper.FooMapper;
import ch.hsr.mixtape.distancefunction.NCDEntropy;

public class NCDEntropyTest {
	
	@Test
	public void testSameValues() {
		int[] input = new int[] {0, 5, 1, 3, 2, 4};
		
		Song song = createSong(input, 5);
		
		NCDEntropy entropy = new NCDEntropy();
		
		double distance = entropy.distance(song, song);
		double expectedDistance = 0.0;
		
		assertEquals(expectedDistance, distance, 0);
		
		
	}
	
	@Test
	public void testCompleteleDifferentValues() {
		
		int[] input = new int[] {0, 5, 1, 3, 2, 4};
		int[] input2 = new int[] {7, 8 ,9 , 10, 11, 12 , 13};
		
		Song song = createSong(input, 5);
		Song song2 = createSong(input2, 13);
		
		NCDEntropy entropy = new NCDEntropy();
		
		double distance = entropy.distance(song, song2);
		double expectedDistance = 1.0;
		
		assertEquals(expectedDistance, distance, 0);
		
	}
	
	public Song createSong(int[] input, int maxValue) {

		Song song = new Song();
		
		int[] suffixArray = new SuffixArrayBuilder().buildSuffixArray(input, maxValue);
		int[] lcp = new LcpBuilder().longestCommonPrefixes(input, suffixArray);
		int[] nfcas = new NFCA().numberOfFirsCommontAncestors(lcp);
		
		Feature f = new Feature("foo feature", input.length, new FooMapper());
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

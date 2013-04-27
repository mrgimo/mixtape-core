package ch.hsr.mixtape.distancefunction.skew;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

import ch.hsr.mixtape.MixTape;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.SpectralCentroidFeature;
import ch.hsr.mixtape.distancefunction.KolmogorovDistance;
import ch.hsr.mixtape.features.LPC;

public class KolmogorovDistanceTest {

	public void testSameValues() {
		int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 7);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		
		Song song = new Song();
		
		
		SpectralCentroidFeature f = new SpectralCentroidFeature("", 12);
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		
		KolmogorovDistance dist = new KolmogorovDistance();
		double act = dist.distance(song, song);
		
		
		assertEquals(1, act, 0);
	}
	
	
	@Test
	public void testActualSong() {
		
		KolmogorovDistance distFunc = new KolmogorovDistance();

		ArrayList<Song> extractAudioData = MixTape.extractAudioData();
		
		for (int i = 0; i < extractAudioData.size(); i++) {
			for (int j = i; j < extractAudioData.size(); j++) {
				Song song = extractAudioData.get(i);
				Song song2 = extractAudioData.get(j);
				
				double dist = distFunc.distance(song, song2);
				
				System.out.println("\ndistance: " + song.getName() + " to " + song2.getName() + " : " + dist);
				System.out.println("--------------------------------------- \n");
				
			}
		}
	}
}

package ch.hsr.mixtape.distancefunction.skew;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import ch.hsr.mixtape.Mixer;
import ch.hsr.mixtape.data.Feature;
import ch.hsr.mixtape.data.FeatureVector;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.valuemapper.SpectralCentroidValueMaper;
import ch.hsr.mixtape.distancefunction.NormalizedInformationDistance;

public class KolmogorovDistanceTest {

	@Test
	public void testSameValues() {
		int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		int[] suffixArray = new SkewInteger().buildSuffixArray(input, 7);
		int[] lcp = new LCP().longestCommonPrefixes(input, suffixArray);
		
		Song song = new Song();
		
		
		Feature f = new Feature("", 12, new SpectralCentroidValueMaper());
		f.setLcp(lcp);
		f.setSuffixArray(suffixArray);
		FeatureVector fv = new FeatureVector();
		fv.addFeature(f);
		song.setFeatureVector(fv);
		
		
		NormalizedInformationDistance dist = new NormalizedInformationDistance();
		double act = dist.distance(song, song);
		
		
		assertEquals(0, act, 0);
	}
	
	
	public void testActualSong() {
		
		NormalizedInformationDistance distFunc = new NormalizedInformationDistance();

		ArrayList<Song> extractAudioData = new Mixer().extractAudioData();
		
		for (int i = 0; i < extractAudioData.size(); i++) {
			for (int j = i; j < extractAudioData.size(); j++) {
				Song song = extractAudioData.get(i);
				Song song2 = extractAudioData.get(j);
				
				double dist = distFunc.distance(song, song2);
			}
		}
	}
}

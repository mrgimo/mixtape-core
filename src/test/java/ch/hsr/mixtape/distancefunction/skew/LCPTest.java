package ch.hsr.mixtape.distancefunction.skew;

import org.junit.Test;
import static org.junit.Assert.*;

public class LCPTest {

	@Test
	public void testMultipleLCP(){
		int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		int[] suffixArray = new int[]{0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11};
		
		int[] expectedLCP = new int[]{0, 5, 0, 4, 0, 3, 0, 2, 0, 1, 0, 0};
		
		LCP lcp = new LCP();
		
		int[] generatedLCP = lcp.longestCommonPrefixes(input, suffixArray);
		
		assertArrayEquals(expectedLCP, generatedLCP);
	}
	
	@Test
	public void testNoLCP() {
		int[] input = new int[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13, -14, -15};
		int[] suffixArray = new int[]{14, 13, 12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		
		int[] expectedLCP = new int[] {0, 0, 0, 0, 0 , 0, 0, 0, 0 ,0 ,0 ,0 ,0, 0,0};
		
		LCP lcp = new LCP();
		int[] generatedLCP = lcp.longestCommonPrefixes(input, suffixArray);
		
		assertArrayEquals(expectedLCP, generatedLCP);
	}
}

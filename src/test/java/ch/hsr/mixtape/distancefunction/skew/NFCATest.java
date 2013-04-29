package ch.hsr.mixtape.distancefunction.skew;

import org.junit.Test;

import cern.colt.Arrays;
import static org.junit.Assert.*;

public class NFCATest {

	
	@Test
	public void testNFCAMultipleNFCAs() {
		NFCA nfca = new NFCA();
		
		int[] lcp = new int[] {0,1,0,3,1,2,0,1,1};
		
		int[] expectedNFCAs = new int[] {1, 0, 3, 0, 0, 0, 2, 0, 0};
		int[] actualNFCAs = nfca.numberOfFirsCommontAncestors(lcp);
		
		System.out.println(Arrays.toString(actualNFCAs));
		
		assertArrayEquals(expectedNFCAs, actualNFCAs);

	}
	
}



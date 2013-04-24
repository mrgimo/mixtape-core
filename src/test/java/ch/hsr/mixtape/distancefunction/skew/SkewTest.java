package ch.hsr.mixtape.distancefunction.skew;

import org.junit.Test;
import static org.junit.Assert.*;

public class SkewTest {
	
	@Test
	public void testAllDifferentLengthMod0() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13, -14, -15};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{14, 13, 12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testAllDifferentLengthMod2() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13, -14};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{13, 12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
	}
	
	@Test
	public void testAllDifferentLengthMod1() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
	}
	
	@Test
	public void testEqualSuffixLengthMod0SameModPos() {
		
		double[] input = new double[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testEqualSuffixLengthMod1SameModPos() {
		
		double[] input = new double[] { 9, 1, 2, 3, 4, 5, 6, 1 , 2 , 3, 4, 5, 1};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{12, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 6, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testEqualSuffixLengthMod2SameModPos() {
		
		double[] input = new double[] { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 7, 8 ,9};
		Skew skew = new Skew();
		
		int[] expectedSuffixArray = new int[]{0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11, 12, 13};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	
	
	
	

}

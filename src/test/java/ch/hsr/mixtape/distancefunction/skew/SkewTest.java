package ch.hsr.mixtape.distancefunction.skew;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class SkewTest {
	
	@Test
	public void testAllDifferentLengthMod0() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13, -14, -15};
		SkewDouble skew = new SkewDouble();
		
		int[] expectedSuffixArray = new int[]{14, 13, 12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testAllDifferentLengthMod2() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13, -14};
		SkewDouble skew = new SkewDouble();
		
		int[] expectedSuffixArray = new int[]{13, 12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
	}
	
	@Test
	public void testAllDifferentLengthMod1() {
		double[] input = new double[] { -1, -2, - 3, -4, -5, -6 , -7, -8, -9, -10, -11, -12, -13};
		SkewDouble skew = new SkewDouble();
		
		int[] expectedSuffixArray = new int[]{12, 11, 10, 9 , 8 , 7 , 6 , 5, 4, 3, 2, 1, 0};
		int[] suffixArray = skew.buildSuffixArray(input);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
	}
	
	
	@Test
	public void testEqualSuffixLengthMod1SameModPos() {
		
		int[] input = new int[] { 9, 1, 2, 3, 4, 5, 6, 1 , 2 , 3, 4, 5, 1};
		SkewInteger skew = new SkewInteger();
		
		int[] expectedSuffixArray = new int[]{12, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 6, 0};
		int[] suffixArray = skew.buildSuffixArray(input, 9);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testEqualSuffixLengthMod2SameModPos() {
		
		int[] input = new int[] { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 7, 8 ,9};
		SkewInteger skew = new SkewInteger();
		
		int[] expectedSuffixArray = new int[]{0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11, 12, 13};
		int[] suffixArray = skew.buildSuffixArray(input, 13);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
	}
	
	@Test
	public void testEqualSuffixLengthMod0SameModPosInteger() {
		
		int[] input = new int[] { 1, 2, 3, 4, 5, 6 , 1, 2, 3, 4, 5, 7};
		SkewInteger skew = new SkewInteger();
		
		int[] expectedSuffixArray = new int[]{0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11};
		int[] suffixArray = skew.buildSuffixArray(input, 7);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
	}
	
	@Test
	public void testSameInputValuesInARow(){
		int[] input = new int[] { 3, 1, 2, 1, 1, 1, 3, 1, 4, 5, 5};
		int[] expectedSuffixArray = new int[] { 3, 4, 1, 5, 7, 2, 0, 6, 8, 10, 9};
		SkewInteger skew = new SkewInteger();
		
		int[] suffixArray = skew.buildSuffixArray(input, 5);
		
		assertArrayEquals(expectedSuffixArray, suffixArray);
		
		
	}
	
	
	
	

}

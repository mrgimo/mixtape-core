package ch.hsr.mixtape.metrics;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ch.hsr.mixtape.nid.SuffixArrayBuilder;

public class SkewTest {

	@Test
	public void testEqualSuffixLengthMod1SameModPos() {
		int[] input = new int[] { 9, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 1 };
		SuffixArrayBuilder skew = new SuffixArrayBuilder();

		int[] expectedSuffixArray = new int[] { 12, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 6, 0 };
		int[] suffixArray = skew.buildSuffixArray(input, 9);

		assertArrayEquals(expectedSuffixArray, suffixArray);

	}

	@Test
	public void testEqualSuffixLengthMod2SameModPos() {
		int[] input = new int[] { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 7, 8, 9 };
		SuffixArrayBuilder skew = new SuffixArrayBuilder();

		int[] expectedSuffixArray = new int[] { 0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11, 12, 13 };
		int[] suffixArray = skew.buildSuffixArray(input, 13);

		assertArrayEquals(expectedSuffixArray, suffixArray);

	}

	@Test
	public void testEqualSuffixLengthMod0SameModPosInteger() {
		int[] input = new int[] { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 7 };
		SuffixArrayBuilder skew = new SuffixArrayBuilder();

		int[] expectedSuffixArray = new int[] { 0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11 };
		int[] suffixArray = skew.buildSuffixArray(input, 7);

		assertArrayEquals(expectedSuffixArray, suffixArray);
	}

	@Test
	public void testSameInputValuesInARow() {
		int[] input = new int[] { 3, 1, 2, 1, 1, 1, 3, 1, 4, 5, 5 };
		SuffixArrayBuilder skew = new SuffixArrayBuilder();

		int[] expectedSuffixArray = new int[] { 3, 4, 1, 5, 7, 2, 0, 6, 8, 10, 9 };
		int[] suffixArray = skew.buildSuffixArray(input, 5);

		assertArrayEquals(expectedSuffixArray, suffixArray);

	}

}

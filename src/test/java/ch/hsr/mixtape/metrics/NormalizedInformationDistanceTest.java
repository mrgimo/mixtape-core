package ch.hsr.mixtape.metrics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hsr.mixtape.nid.NormalizedInformationDistance;

public class NormalizedInformationDistanceTest {

	// @Test
	public void testSameValues() {
		int[] x = new int[] { 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 7, 2, 3, 4, 5, 7, 4, 5, 6, 7, 3, 2 };

		NormalizedInformationDistance nid = new NormalizedInformationDistance();

		double expectedDistance = 0.0;
		double actualDistance = nid.distanceBetween(x, x);

		assertEquals(expectedDistance, actualDistance, 0);
	}

	// @Test
	public void halfToFullCommonValuesTest() {
		int[] x = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5, 9, 3, 4 };
		int[] y = new int[] { 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2 };

		NormalizedInformationDistance nid = new NormalizedInformationDistance();

		double expectedDistance = 0.5;
		double actualDistance = nid.distanceBetween(x, y);

		assertEquals(expectedDistance, actualDistance, 0);
	}

	@Test
	public void halfToHalfCommonValuesTest() {
		int[] x = new int[] { 9, 9, 9, 9, 9, 1, 2, 3, 4, 5 };
		int[] y = new int[] { 1, 2, 3, 4, 5, 8, 8, 8, 8, 8 };

		NormalizedInformationDistance nid = new NormalizedInformationDistance();

		double expectedDistance = 0.5;
		double actualDistance = nid.distanceBetween(x, y);

		assertEquals(expectedDistance, actualDistance, 0);
	}

}
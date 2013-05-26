package ch.hsr.mixtape.metrics;

import java.util.Collection;
import java.util.concurrent.Future;

public class NormalizedInformationDistance implements Metric<Integer[]> {

	public double distanceBetween(Collection<Future<Integer[]>> x, Collection<Future<Integer[]>> y) {
		return 0;
	}

}
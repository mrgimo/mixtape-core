package ch.hsr.mixtape.metrics;

import java.util.Collection;
import java.util.concurrent.Future;

public interface Metric<T> {

	double distanceBetween(Collection<Future<T>> x, Collection<Future<T>> y);

}
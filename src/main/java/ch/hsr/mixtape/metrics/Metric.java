package ch.hsr.mixtape.metrics;

import java.util.List;
import java.util.concurrent.Future;

public interface Metric<T> {

	double distanceBetween(List<Future<T>> x, List<Future<T>> y);

}
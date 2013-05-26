package ch.hsr.mixtape.features.harmonic;

import ch.hsr.mixtape.metrics.Metric;

public interface Feature<T> {

	T extract(double[] window);

	int getWindowSize();

	Metric<T> getMetric();

}
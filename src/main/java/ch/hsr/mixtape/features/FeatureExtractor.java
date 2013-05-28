package ch.hsr.mixtape.features;

import ch.hsr.mixtape.metrics.Metric;

public interface FeatureExtractor<T> {

	T extract(double[] window);

	Metric<T> getMetric();

	int getWindowSize();

	int getWindowOverlap();

}
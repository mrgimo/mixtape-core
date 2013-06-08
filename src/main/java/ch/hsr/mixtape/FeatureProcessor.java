package ch.hsr.mixtape;

import java.util.Iterator;
import java.util.concurrent.Callable;

import ch.hsr.mixtape.concurrency.EvaluatingIterator;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.model.Song;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class FeatureProcessor<FeaturesOfWindow, FeaturesOfSong> {

	private final FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor;

	private final ListeningExecutorService extractionExecutor;
	private final ListeningExecutorService postprocessingExecutor;

	private EvaluatingIterator<FeaturesOfWindow> featuresOfWindows;
	private ListenableFuture<FeaturesOfSong> featureOfSong;

	public FeatureProcessor(FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor,
			ListeningExecutorService extractionExecutor,
			ListeningExecutorService postprocessingExecutor) {
		this.featureExtractor = featureExtractor;

		this.extractionExecutor = extractionExecutor;
		this.postprocessingExecutor = postprocessingExecutor;

		featuresOfWindows = initExtraction(featureExtractor);
		featureOfSong = initPostprocessing(featuresOfWindows);
	}

	private EvaluatingIterator<FeaturesOfWindow> initExtraction(
			FeatureExtractor<FeaturesOfWindow, FeaturesOfSong> featureExtractor) {
		return new EvaluatingIterator<>(doExtract(new double[featureExtractor.getWindowSize()]));
	}

	private ListenableFuture<FeaturesOfSong> initPostprocessing(final Iterator<FeaturesOfWindow> featuresOfWindows) {
		return postprocessingExecutor.submit(new Callable<FeaturesOfSong>() {

			public FeaturesOfSong call() throws Exception {
				return featureExtractor.postprocess(featuresOfWindows);
			}

		});
	}

	public void process(double[] windowOfSamples) {
		featuresOfWindows.put(doExtract(windowOfSamples));
	}

	private ListenableFuture<FeaturesOfWindow> doExtract(final double[] windowOfSamples) {
		return extractionExecutor.submit(new Callable<FeaturesOfWindow>() {

			public FeaturesOfWindow call() throws Exception {
				return featureExtractor.extractFrom(windowOfSamples);
			}

		});
	}

	public void postprocess() {
		featuresOfWindows.finish();
	}

	public ListenableFuture<FeaturesOfSong> getFeaturesOfSong() {
		return featureOfSong;
	}

	public int getWindowSize() {
		return featureExtractor.getWindowSize();
	}

	public int getWindowOverlap() {
		return featureExtractor.getWindowOverlap();
	}

}

package ch.hsr.mixtape.features.temporal;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ch.hsr.mixtape.MathUtils;
import ch.hsr.mixtape.features.temporal.SpectralDescription.SpectralDescriptionType;

/**
 * Single task for multi-threaded tempo extraction.
 * 
 * @author Stefan Derungs
 */
public class TempoExtractionTask implements Callable<ExtractedTempo> {

	private static final double SILENCE = -90.0;

	private SpectralDescriptionType type;

	private ArrayList<double[]> dataSamples;

	private Tempo tempo;

	private double[] extractedBPMs;

	private double[] extractedConfidences;

	private PhaseVocoder phaseVocoder;

	public TempoExtractionTask(SpectralDescriptionType type,
			ArrayList<double[]> dataSamples, int windowSize, int hopSize, int sampleRateInHz) {
		this.type = type;
		this.dataSamples = dataSamples;
		// tempo = new Tempo(type, dataSamples.size(), hopSize, sampleRateInHz);
		// // TODO: remove if results ok.
		tempo = new Tempo(type, windowSize, hopSize, sampleRateInHz);
		extractedBPMs = new double[dataSamples.size()];
		extractedConfidences = new double[dataSamples.size()];
		phaseVocoder = new PhaseVocoder(windowSize, hopSize);
	}

	@Override
	public ExtractedTempo call() throws Exception {
		for (int i = 0; i < dataSamples.size(); i++) {
			double[] windowOfSamples = dataSamples.get(i);

			tempo.extractTempo(detectSilence(windowOfSamples), phaseVocoder.computeSpectralFrame(windowOfSamples));
			extractedBPMs[i] = tempo.getBPM();
			extractedConfidences[i] = tempo.getConfidence();
		}

		return new ExtractedTempo(type, extractedBPMs, extractedConfidences);
	}

	private boolean detectSilence(double[] windowOfSamples) {
		double energy = 0.;
		for (int j = 0; j < windowOfSamples.length; j++)
			energy += MathUtils.square(windowOfSamples[j]);

		return 10.0 * Math.log10(energy / (double) windowOfSamples.length) >= SILENCE;
	}

}

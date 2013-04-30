package ch.hsr.mixtape.features.aubio;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ch.hsr.mixtape.features.aubio.SpectralDescription.SpectralDescriptionType;

/**
 * Single task for multi-threaded tempo extraction.
 *  
 * @author Stefan Derungs
 */
public class TempoExtractionTask implements Callable<ExtractedTempo> {

	private SpectralDescriptionType type;

	private ArrayList<double[]> dataSamples;

	private Tempo tempo;

	private double[] extractedBPMs;

	private double[] extractedConfidences;

	public TempoExtractionTask(SpectralDescriptionType type,
			ArrayList<double[]> dataSamples, int windowSize, int hopSize, int sampleRateInHz) {
		this.type = type;
		this.dataSamples = dataSamples;
		//tempo = new Tempo(type, dataSamples.size(), hopSize, sampleRateInHz); // TODO: remove if results ok.
		tempo = new Tempo(type, windowSize, hopSize, sampleRateInHz);
		extractedBPMs = new double[dataSamples.size()];
		extractedConfidences = new double[dataSamples.size()];
	}

	@Override
	public ExtractedTempo call() throws Exception {
		for (int i = 0; i < dataSamples.size(); i++) {
			tempo.extractTempo(dataSamples.get(i));
			extractedBPMs[i] = tempo.getBPM();
			extractedConfidences[i] = tempo.getConfidence();
		}

		return new ExtractedTempo(type, extractedBPMs, extractedConfidences);
	}

}

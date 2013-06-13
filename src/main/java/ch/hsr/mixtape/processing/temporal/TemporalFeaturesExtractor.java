package ch.hsr.mixtape.processing.temporal;

import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.COMPLEX_DOMAIN;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.ENERGY;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.HIGH_FREQUENCY_CONTENT;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.KULLBACK_LIEBLER;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.MODIFIED_KULLBACK_LIEBLER;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.PHASE_FAST;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.SPECTRAL_DIFFERENCE;
import static ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType.SPECTRAL_FLUX;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.hsr.mixtape.nid.NormalizedInformationDistance;
import ch.hsr.mixtape.processing.FeatureExtractor;
import ch.hsr.mixtape.processing.temporal.SpectralDescription.SpectralDescriptionType;
import ch.hsr.mixtape.util.MathUtils;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

public class TemporalFeaturesExtractor implements FeatureExtractor<TemporalFeaturesOfWindow, TemporalFeaturesOfSong> {

	private static final SpectralDescriptionType[] SPECTRAL_DESCRIPTION_TYPES = {
			ENERGY,
			SPECTRAL_DIFFERENCE,
			HIGH_FREQUENCY_CONTENT,
			COMPLEX_DOMAIN,
			PHASE_FAST,
			KULLBACK_LIEBLER,
			MODIFIED_KULLBACK_LIEBLER,
			SPECTRAL_FLUX
	};

	private static final int WINDOW_SIZE = 4096;
	private static final int HOP_SIZE = 512;
	private static final int WINDOW_OVERLAP = WINDOW_SIZE - HOP_SIZE;

	private static final int SAMPLE_RATE = 44100;

	private static final double SILENCE = -90.0;

	private PhaseVocoder phaseVocoder = new PhaseVocoder(WINDOW_SIZE, WINDOW_SIZE - WINDOW_OVERLAP);
	private NormalizedInformationDistance nid = new NormalizedInformationDistance();

	public TemporalFeaturesOfWindow extractFrom(double[] windowOfSamples) {
		TemporalFeaturesOfWindow temporalFeaturesOfWindow = new TemporalFeaturesOfWindow();

		temporalFeaturesOfWindow.silent = detectSilence(windowOfSamples);
		temporalFeaturesOfWindow.fftgrain = phaseVocoder.computeSpectralFrame(windowOfSamples);

		return temporalFeaturesOfWindow;
	}

	private boolean detectSilence(double[] windowOfSamples) {
		double energy = 0.;
		for (int i = 0; i < windowOfSamples.length; i++)
			energy += MathUtils.square(windowOfSamples[i]);

		return 10.0 * Math.log10(energy / (double) windowOfSamples.length) >= SILENCE;
	}

	public TemporalFeaturesOfSong postprocess(
			Iterator<TemporalFeaturesOfWindow> featuresOfWindows) {
		Tempo[] tempos = initTempos();

		List<Double[]> bpms = Lists.newArrayList();
		List<Double[]> confidences = Lists.newArrayList();

		double[] maxConfidences = new double[tempos.length];
		while (featuresOfWindows.hasNext()) {
			TemporalFeaturesOfWindow featureOfWindow = featuresOfWindows.next();

			Double[] bpm = new Double[tempos.length];
			Double[] confidence = new Double[tempos.length];
			for (int i = 0; i < tempos.length; i++) {
				Tempo tempo = tempos[i];
				tempo.extractTempo(featureOfWindow.silent, featureOfWindow.fftgrain);

				bpm[i] = tempo.getBPM();
				confidence[i] = tempo.getConfidence();

				if (maxConfidences[i] < confidence[i])
					maxConfidences[i] = confidence[i];
			}

			bpms.add(bpm);
			confidences.add(confidence);
		}

		TemporalFeaturesOfSong featuresOfSong = new TemporalFeaturesOfSong();
		featuresOfSong.beats = quantize(bpms, confidences, maxConfidences);

		return featuresOfSong;
	}

	private int[] quantize(List<Double[]> bpms, List<Double[]> confidences, double[] maxConfidences) {
		int[] beats = new int[bpms.size()];
		int numberOfBeats = 0;

		for (int i = 0; i < bpms.size(); i++) {
			Double[] bpm = bpms.get(i);
			Double[] confidence = confidences.get(i);

			double beatSum = 0;
			double confidenceSum = 0;
			for (int j = 0; j < bpm.length; j++) {
				if (maxConfidences[j] == 0)
					continue;

				double factor = confidence[j] / maxConfidences[j];

				beatSum += bpm[j] * factor;
				confidenceSum += factor;
			}

			if (confidenceSum > 0)
				beats[numberOfBeats++] = DoubleMath.roundToInt(beatSum / confidenceSum * 0.2, RoundingMode.HALF_UP) + 1;
		}

		return Arrays.copyOf(beats, numberOfBeats);
	}

	private Tempo[] initTempos() {
		Tempo[] tempos = new Tempo[SPECTRAL_DESCRIPTION_TYPES.length];
		for (SpectralDescriptionType type : SPECTRAL_DESCRIPTION_TYPES)
			tempos[type.ordinal()] = new Tempo(type, WINDOW_SIZE, HOP_SIZE,
					SAMPLE_RATE);

		return tempos;
	}

	public double distanceBetween(TemporalFeaturesOfSong x, TemporalFeaturesOfSong y) {
		return nid.distanceBetween(x.beats, y.beats);
	}

	public int getWindowSize() {
		return WINDOW_SIZE;
	}

	public int getWindowOverlap() {
		return WINDOW_OVERLAP;
	}

}
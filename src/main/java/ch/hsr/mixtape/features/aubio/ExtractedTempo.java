package ch.hsr.mixtape.features.aubio;

import ch.hsr.mixtape.features.aubio.SpectralDescription.SpectralDescriptionType;

public class ExtractedTempo {

	private double[] bpms;

	private double[] confidences;

	private SpectralDescriptionType type;

	public ExtractedTempo(SpectralDescriptionType type, double[] bpms,
			double[] confidences) {
		this.type = type;
		this.bpms = bpms;
		this.confidences = confidences;
	}

	public double[] getBpms() {
		return bpms;
	}

	public double[] getConfidences() {
		return confidences;
	}

	public SpectralDescriptionType getType() {
		return type;
	}

}

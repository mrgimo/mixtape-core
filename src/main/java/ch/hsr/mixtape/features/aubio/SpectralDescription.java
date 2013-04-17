package ch.hsr.mixtape.features.aubio;

/**
 * This is the controller class for executing a spectral description function.
 * 
 * <p>
 * <b>This class was ported from aubio's `specdesc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class SpectralDescription {

	SpectralDescriptionType onsetType;

	/**
	 * Pointer to aubio_specdesc_<type> function.
	 */
	SpectralDescriptionFunction functionPointer;

	/**
	 * Minimum norm threshold for phase and specdiff.
	 */
	double threshold;

	/**
	 * Previous norm vector.
	 */
	double[] oldMagnitude;

	/**
	 * Current onset detection measure vector.
	 */
	double[] dev1;

	/**
	 * Previous phase vector, one frame behind.
	 */
	double[] theta1;

	/**
	 * Previous phase vector, two frames behind.
	 */
	double[] theta2;

	Histogram histogram;

	/**
	 * Onset detection types
	 */
	public enum SpectralDescriptionType {
		ENERGY, SPECTRAL_DIFFERENCE, HIGH_FREQUENCY_CONTENT, COMPLEX_DOMAIN, PHASE_FAST, KULLBACK_LIEBLER, MODIFIED_KULLBACK_LIEBLER, SPECTRAL_FLUX,
		// SPECTRAL_CENTROID,
		// SPECTRAL_SPREAD,
		// SPECTRAL_SKEWNESS,
		// SPECTRAL_KURTOSIS,
		// SPECTRAL_SLOPE,
		// SPECTRAL_DECREASE,
		// SPECTRAL_ROLLOFF
	}

	/**
	 * @param onset_mode
	 *            Default to use is aubio_onset_hfc.
	 * @param bufferSize
	 *            Length of the input spectrum frame
	 */
	public SpectralDescription(SpectralDescriptionType onsetType, int bufferSize) {
		// int rsize = bufferSize / 2 + 1; // TODO: still correct?!?!
		int rsize = MathUtils.ensureIsPowerOfTwo(bufferSize);

		switch (onsetType) {
		/* for both energy and hfc, only fftgrain.norm is required */
		case ENERGY:
			functionPointer = new SDF_Energy();
			break;
		case HIGH_FREQUENCY_CONTENT:
			functionPointer = new SDF_HighestFrequencyContent();
			break;
		/* the other approaches will need some more memory spaces */
		case COMPLEX_DOMAIN:
			oldMagnitude = new double[rsize];
			dev1 = new double[rsize];
			theta1 = new double[rsize];
			theta2 = new double[rsize];
			functionPointer = new SDF_ComplexDomain();
			break;
		case PHASE_FAST:
			dev1 = new double[rsize];
			theta1 = new double[rsize];
			theta2 = new double[rsize];
			histogram = new Histogram(0.0, Math.PI, 10);
			threshold = 0.1;
			functionPointer = new SDF_PhaseFast();
			break;
		case SPECTRAL_DIFFERENCE:
			oldMagnitude = new double[rsize];
			dev1 = new double[rsize];
			histogram = new Histogram(0.0, Math.PI, 10);
			threshold = 0.1;
			functionPointer = new SDF_SpectralDifference();
			break;
		case KULLBACK_LIEBLER:
			oldMagnitude = new double[rsize];
			functionPointer = new SDF_KullbackLiebler();
			break;
		case MODIFIED_KULLBACK_LIEBLER:
			oldMagnitude = new double[rsize];
			functionPointer = new SDF_ModifiedKullbackLiebler();
			break;
		case SPECTRAL_FLUX:
			oldMagnitude = new double[rsize];
			functionPointer = new SDF_SpectralFlux();
			break;
		default:
			break;
		}
	}

	/**
	 * Generic function pointing to the choosen spectral function descriptor
	 * object.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_specdesc_do
	 * </p>
	 */
	public double[] call(double[][] fftgrain) {
		// TODO: refactor to return just a double instead of a double[]
		return functionPointer.call(this, fftgrain);
	}

}
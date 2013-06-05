package ch.hsr.mixtape.features.temporal;

/**
 * Interface for spectral detection functions.
 * 
 * @author Stefan Derungs
 */
public interface SpectralDescriptionFunction {

	/**
	 * Execute spectral description function on a spectral frame.
	 * 
	 * Generic function to compute spectral detescription.
	 * 
	 * @param sd
	 *            Spectral description object.
	 * @param fftgrain
	 *            Input signal spectrum as computed by aubio_pvoc_do.
	 * @return Output vector (one sample long, to send to the peak picking).
	 */
	public double[] call(SpectralDescription sd, double[][] fftgrain);

}

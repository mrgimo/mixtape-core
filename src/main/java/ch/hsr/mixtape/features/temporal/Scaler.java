package ch.hsr.mixtape.features.temporal;

/**
 * This class provides the ability to scale double arrays.
 * 
 * <p>
 * <b>This class was ported from aubio's `scale.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class Scaler {

	double ilow;

	double ihig;

	double olow;

	double ohig;

	double scaler;

	double irange;

	/**
	 * Create a scale object.
	 * 
	 * @param flow
	 *            lower value of output function
	 * @param fhig
	 *            higher value of output function
	 * @param ilow
	 *            lower value of input function
	 * @param ihig
	 *            higher value of output function
	 */
	public Scaler(double ilow, double ihig, double olow, double ohig) {
		setLimits(ilow, ihig, olow, ohig);
	}

	/**
	 * Modify scale parameters after object creation.
	 * 
	 * @param s
	 *            scale object as returned by new_aubio_scale
	 * @param olow
	 *            lower value of output function
	 * @param ohig
	 *            higher value of output function
	 * @param ilow
	 *            lower value of input function
	 * @param ihig
	 *            higher value of output function
	 */
	public int setLimits(double ilow, double ihig, double olow, double ohig) {
		double inputrange = ihig - ilow;
		double outputrange = ohig - olow;
		this.ilow = ilow;
		this.ihig = ihig;
		this.olow = olow;
		this.ohig = ohig;
		if (inputrange == 0) {
			scaler = 0.0;
		} else {
			scaler = outputrange / inputrange;
			if (inputrange < 0)
				inputrange = inputrange * -1.0;
		}
		return 0;
	}

	/**
	 * Scale input vector.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_scale_do
	 * </p>
	 * 
	 * @param input
	 *            vector to scale
	 */
	public void scale(double[] input) {
		for (int i = 0; i < input.length; i++) {
			input[i] -= ilow;
			input[i] *= scaler;
			input[i] += olow;
		}
	}

}

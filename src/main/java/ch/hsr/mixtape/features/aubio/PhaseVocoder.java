package ch.hsr.mixtape.features.aubio;

/**
 * This class implements a phase vocoder. The spectral frames are computed using
 * a HanningZ window and a swapped version of the signal to simplify the phase
 * relationships across frames. The window sizes and overlap are specified at
 * creation time.
 * 
 * <p>
 * <b>This class was ported from aubio's `phasevoc.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class PhaseVocoder {

	/**
	 * Grain length.
	 */
	int windowSize;

	/**
	 * Overlap step.
	 */
	int hopSize;

	/**
	 * fft object
	 */
	FFTWrapper fft;

	/**
	 * Current output grain [win_s].
	 */
	double[] synth;

	/**
	 * Last input frame [win_s-hop_s].
	 */
	double[] synthold;

	/**
	 * Current input grain [win_s].
	 */
	double[] data;

	/**
	 * Last input frame [win_s-hop_s].
	 */
	double[] dataold;

	/**
	 * Grain window [win_s].
	 */
	double[] w;

	public PhaseVocoder(int windowSize, int hopSize) {
		if (hopSize < 1) {
			System.err.println("Hop size is smaller than 1!");
			System.err.println("Resetting hop size to half the window size.");
			hopSize = windowSize / 2;
		}

		fft = new FFTWrapper();

		/* remember old */
		data = new double[windowSize];
		synth = new double[windowSize];

		/* new input output */
		dataold = new double[windowSize - hopSize];
		synthold = new double[windowSize - hopSize];

		try {
			w = createWindow("hanningz", windowSize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.hopSize = hopSize;
		this.windowSize = windowSize;
	}

	/**
	 * This function accepts an input vector of size [hop_s]. The analysis
	 * buffer is rotated and filled with the new data. After windowing of this
	 * signal window, the Fourier transform is computed and returned in fftgrain
	 * as two vectors, magnitude and phase.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_pvoc_do
	 * </p>
	 * 
	 * @param in
	 *            New input signal (hopSize long).
	 * @return output spectral frame.
	 */
	public double[][] computeSpectralFrame(double[] in) {
		/* slide */
		swapBuffers(data, dataold, in, windowSize, hopSize);
		/* windowing */
		DoubleArrayUtils.weight(data, w);
		/* shift */
		DoubleArrayUtils.shift(data);
		/* calculate fft */
		return fft.forward(data);
	}

	/**
	 * <p>
	 * <b>Methodname in aubio:</b> new_aubio_window
	 * </p>
	 * 
	 * @throws Exception
	 */
	private double[] createWindow(String window_type, int size)
			throws Exception {
		double[] win = new double[size];

		if (window_type == null || window_type.isEmpty())
			throw new Exception("Window type must not be null.");

		switch (window_type) {
		case "rectangle":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.5;
			break;
		case "hamming":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (win.length));
			break;
		case "hanning":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.5 - (0.5 * Math.cos(2 * Math.PI * i / (win.length)));
			break;
		case "hanningz":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.5 * (1.0 - Math.cos(2 * Math.PI * i / (win.length)));

			break;
		case "blackman":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.42 - 0.50
						* Math.cos(2 * Math.PI * i / (win.length - 1.0)) + 0.08
						* Math.cos(2.0 * 2 * Math.PI * i / (win.length - 1.0));
			break;
		case "blackman_harris":
			for (int i = 0; i < win.length; i++)
				win[i] = 0.35875 - 0.48829
						* Math.cos(2 * Math.PI * i / (win.length - 1.0))
						+ 0.14128
						* Math.cos(2.0 * 2 * Math.PI * i / (win.length - 1.0))
						- 0.01168
						* Math.cos(3.0 * 2 * Math.PI * i / (win.length - 1.0));
			break;
		case "gaussian":
			double /* lsmp_t */a,
			b,
			c = 0.5;
			for (int n = 0; n < win.length; n++) {
				a = (n - c * (win.length - 1)) / (c * c * (win.length - 1));
				b = -c * a * a;
				win[n] = Math.exp(b);
			}
			break;
		case "welch":
			for (int i = 0; i < win.length; i++)
				win[i] = 1.0 - Math.pow((2. * i - win.length)
						/ (win.length + 1.0), 2);
			break;
		case "parzen":
			for (int i = 0; i < win.length; i++)
				win[i] = 1.0 - Math.abs((2. * i - win.length)
						/ (win.length + 1.0));
			break;
		case "default":
			break;
		default:
			throw new Exception("Unknown window type `" + window_type + "`.");
		}

		return win;
	}

	/**
	 * Returns data and dataold slided by hopSize.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_pvoc_swapbuffers
	 * </p>
	 */
	public static void swapBuffers(double[] data, double[] dataold,
			double[] datanew, int windowSize, int hopSize) {
		for (int i = 0; i < windowSize - hopSize; i++)
			data[i] = dataold[i];
		for (int i = 0; i < hopSize; i++)
			data[windowSize - hopSize + i] = datanew[i];
		for (int i = 0; i < windowSize - hopSize; i++)
			dataold[i] = data[i + hopSize];
	}

}

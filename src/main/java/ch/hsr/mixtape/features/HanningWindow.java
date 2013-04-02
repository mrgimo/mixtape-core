package ch.hsr.mixtape.features;

import static java.lang.Math.*;

public class HanningWindow {

	public double[] hanningWindow(double[] values) {
		double[] window = new double[values.length];
		for (int i = 0; i < window.length; i++)
			window[i] = 0.5 * (1 - cos((2 * PI * i) / (window.length - 1)));

		return window;
	}

}

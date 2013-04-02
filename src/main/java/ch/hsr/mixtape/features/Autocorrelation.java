package ch.hsr.mixtape.features;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

public class Autocorrelation {

	public Complex[] autocorrelate(double[] values) {
		FastFourierTransformer transformer = new FastFourierTransformer();
		HanningWindow window = new HanningWindow();

		Complex[] coefficients = transformer.transform(window.hanningWindow(values));
		Complex[] autocorrelation = transformer.inversetransform(multiply(coefficients, conjugate(coefficients)));

		return autocorrelation;
	}

	private static Complex[] conjugate(Complex[] a) {
		Complex[] conjugations = new Complex[a.length];
		for (int i = 0; i < a.length; i++)
			conjugations[i] = a[i].conjugate();

		return conjugations;
	}

	private static Complex[] multiply(Complex[] a, Complex[] b) {
		Complex[] products = new Complex[a.length];
		for (int i = 0; i < a.length; i++)
			products[i] = a[i].multiply(b[i]);

		return products;
	}

}
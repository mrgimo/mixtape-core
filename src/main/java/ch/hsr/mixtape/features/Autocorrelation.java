package ch.hsr.mixtape.features;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Autocorrelation {

	public Complex[] autocorrelate(double[] values) {
		FastFourierTransformer transformer = new FastFourierTransformer(
				DftNormalization.STANDARD);
		HanningWindow window = new HanningWindow();

		Complex[] coefficients = transformer.transform(
				window.hanningWindow(values), TransformType.FORWARD);
		Complex[] autocorrelation = transformer.transform(
				multiply(coefficients, conjugate(coefficients)),
				TransformType.INVERSE);

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
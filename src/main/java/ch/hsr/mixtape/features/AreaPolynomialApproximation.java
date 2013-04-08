package ch.hsr.mixtape.features;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class AreaPolynomialApproximation {

	private static final int WINDOW_LENGTH = 50;
	private static final int FEATURE_LENGTH = 512;

	private int k = 5;
	private int l = 20;

	private DenseDoubleMatrix2D terms;
	private DenseDoubleMatrix2D z;

	public AreaPolynomialApproximation() {
		terms = calcTerms();
		z = new DenseDoubleMatrix2D(1, FEATURE_LENGTH * WINDOW_LENGTH);
	}

	public double[] extractFeature(double[] samples, double sampling_rate, double[][] magnitudeSpectra) {
		if ((FEATURE_LENGTH != magnitudeSpectra[0].length) || (WINDOW_LENGTH != magnitudeSpectra.length)) {
			terms = new DenseDoubleMatrix2D(k * l, WINDOW_LENGTH * FEATURE_LENGTH);
			z = new DenseDoubleMatrix2D(1, FEATURE_LENGTH * WINDOW_LENGTH);
			calcTerms();
		}

		for (int i = 0; i < WINDOW_LENGTH; ++i) {
			for (int j = 0; j < FEATURE_LENGTH; ++j) {
				z.set(0, WINDOW_LENGTH * i + j, magnitudeSpectra[i][j]);
			}
		}

		return new Algebra().solve(terms, z).viewRow(0).toArray();
	}

	private DenseDoubleMatrix2D calcTerms() {
		DenseDoubleMatrix2D terms = new DenseDoubleMatrix2D(k * l, WINDOW_LENGTH * FEATURE_LENGTH);

		terms.assign(0.0);
		for (int x = 0; x < WINDOW_LENGTH; ++x) {
			for (int y = 0; y < FEATURE_LENGTH; ++y) {
				for (int i = 0; i < k; ++i) {
					for (int j = 0; j < l; ++j) {
						terms.set(l * i + j, FEATURE_LENGTH * x + y, Math.pow(x, i) * Math.pow(y, j));
					}
				}
			}
		}

		return terms;
	}

}
package ch.hsr.mixtape.features.aubio;

/**
 * <p>
 * <b>This class contains some helper methods from aubio's `mathutils.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class MathUtils {
	
	/**
	 * Finds exact peak index by quadratic interpolation.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> quadint
	 * </p>
	 */
	public static double quadraticInterpolation(double[] x, int pos) {
		int span = 2;
		double step = 1. / 200.;
		/* hack : init resold to - something (in case x[pos+-span]<0)) */
		double res, frac, s0, s1, s2, exactpos = (double) pos, resold = -1000.;
		if ((pos > span) && (pos < x.length - span)) {
			s0 = x[pos - span];
			s1 = x[pos];
			s2 = x[pos + span];
			/* increase frac */
			for (frac = 0.; frac < 2.; frac = frac + step) {
				res = quadraticFraction(s0, s1, s2, frac);
				if (res > resold)
					resold = res;
				else {
					exactpos += (frac - step) * 2. - 1.;
					break;
				}
			}
		}
		return exactpos;
	}

	/**
	 * Quadratic interpolation using Lagrange polynomial.
	 * 
	 * Inspired from ``Comparison of interpolation algorithms in real-time sound
	 * processing'', Vladimir Arnost.
	 * 
	 * <p>
	 * <b>Methodname in aubio:</b> aubio_quadfrac
	 * </p>
	 * 
	 * @param s0
	 *            known point on the curve
	 * @param s1
	 *            another known point on the curve
	 * @param s2
	 *            another known point on the curve
	 * @param pf
	 *            is the floating point index [0;2]
	 * @return
	 */
	public static double quadraticFraction(double s0, double s1, double s2,
			double pf) {
		return s0 + (pf / 2.)
				* (pf * (s0 - 2. * s1 + s2) - 3. * s0 + 4. * s1 - s2);
	}

	public static int ensureIsPowerOfTwo(int value) {
		double log_value = logBaseN(value, 2);
		int log_int = (int) log_value;
		int result = pow(2, log_int);
		if (result != value)
			result = pow(2, log_int + 1);
		return result;
	}

	private static double logBaseN(double x, double n) {
		return (Math.log10(x) / Math.log10(n));
	}

	private static int pow(int a, int b) {
		int result = a;
		for (int i = 1; i < b; i++)
			result *= a;
		return result;
	}

}

package ch.hsr.mixtape.features.aubio;

import java.util.ArrayList;

/**
 * Beat tracking using a context dependant model. <br>
 * This file implements the causal beat tracking algorithm designed by Matthew
 * Davies and described in the following articles:
 * <ul>
 * <li>Matthew E. P. Davies and Mark D. Plumbley. Causal tempo tracking of
 * audio. In Proceedings of the International Symposium on Music Information
 * Retrieval (ISMIR), pages 164­169, Barcelona, Spain, 2004.</li>
 * <li>Matthew E. P. Davies, Paul Brossier, and Mark D. Plumbley. Beat tracking
 * towards automatic musical accompaniment. In Proceedings of the Audio
 * Engeeniring Society 118th Convention, Barcelona, Spain, May 2005.</li>
 * </ul>
 * 
 * <p>
 * <b>This class was ported from aubio's `beattracking.c`.</b>
 * </p>
 * 
 * @see http://git.aubio.org/
 * @author Stefan Derungs
 */
public class BeatTracking {

	/**
	 * Define to 1 to print out tracking difficulties.
	 */
	private static final boolean AUBIO_BEAT_WARNINGS = false;

	/**
	 * Rayleigh weighting for beat period in general modelfvec_t.
	 */
	double[] rwv;

	/**
	 * Exponential weighting for beat alignment in general model.
	 */
	double[] dfwv;

	/**
	 * Gaussian weighting for beat period in context dependant model.
	 */
	double[] gwv;

	/**
	 * Gaussian weighting for beat alignment in context dependant model.
	 */
	double[] phwv;

	/**
	 * Reversed onset detection function.
	 */
	double[] dfrev;

	/**
	 * Vector for autocorrelation function (of current detection function
	 * frame).
	 */
	double[] acf;

	/**
	 * Store result of passing acf through s.i.c.f.b.
	 */
	double[] acfout;

	double[] phout;

	/**
	 * Time signature of input, set to zero until context dependent model
	 * activated.
	 */
	int timesig = 0;

	int step;

	/**
	 * Rayleigh parameter.
	 */
	int rayparam;

	double lastbeat = 0;

	int counter = 0;

	int flagstep = 0;

	double g_var = 3.901; // constthresh empirically derived!

	double gp = 0;

	double beatPeriod;

	double rp = 1;

	double rp1;

	double rp2;

	/**
	 * Create beat tracking object.
	 * 
	 * @param hopSize
	 *            Number of onset detection samples [512].
	 */
	public BeatTracking(int hopSize) {
		/*
		 * parameter for rayleigh weight vector - sets preferred tempo to 120bpm
		 * [43]
		 */
		double rayparam = 48. / 512. * hopSize;

		double dfwvnorm = Math.exp((Math.log(2.0) / rayparam) * (hopSize + 2));

		/* length over which beat period is found [128] */
		int laglen = hopSize / 4;

		this.rayparam = (int) rayparam;
		/*
		 * step increment - both in detection function samples -i.e. 11.6ms or 1
		 * onset frame [128]
		 */
		step = hopSize / 4;/* 1.5 seconds */
		rwv = new double[laglen];
		gwv = new double[laglen];
		dfwv = new double[hopSize];
		dfrev = new double[hopSize];
		acf = new double[hopSize];
		acfout = new double[laglen];
		phwv = new double[2 * laglen];
		phout = new double[hopSize];

		/* exponential weighting, dfwv = 0.5 when i = 43 */
		for (int i = 0; i < hopSize; i++)
			dfwv[i] = (Math.exp((Math.log(2.0) / rayparam) * (i + 1)))
					/ dfwvnorm;

		for (int i = 0; i < (laglen); i++)
			rwv[i] = ((double) (i + 1.) / Math.pow((double) rayparam, 2))
					* Math.exp((-Math.pow((double) (i + 1.), 2) / (2. * Math
							.pow((double) rayparam, 2))));
	}

	/**
	 * Track the beat.
	 * 
	 * @param dfframes
	 *            Current input detection function frame, smoothed by adaptive
	 *            median threshold.
	 * @return Detected beat locations.
	 */
	public double[] trackBeat(double[] dfframe) {
		// number of harmonics in shift invariant comb filterbank
		int numelem = 4;
		// number of elements used to find beat phase
		int kmax;

		/* copy dfframe, apply detection function weighting, and revert */
		dfrev = dfframe.clone();
		DoubleArrayUtils.weight(dfrev, dfwv);
		DoubleArrayUtils.revert(dfrev);

		/* compute autocorrelation function */
		acf = getAutocorrelation(dfframe);

		/* if timesig is unknown, use metrically unbiased version of filterbank */
		if (timesig == 0)
			numelem = 4;
		else
			numelem = timesig;

		/* first and last output values are left intentionally as zero */
		acfout = new double[acfout.length];

		/* compute shift invariant comb filterbank */
		for (int i = 1; i < rwv.length - 1; i++)
			for (int a = 1; a <= numelem; a++)
				// used to build shift invariant comb filterbank
				for (int b = (1 - a); b < a; b++)
					acfout[i] += acf[a * (i + 1) + b - 1] * 1. / (2. * a - 1.);

		/* apply Rayleigh weight */
		DoubleArrayUtils.weight(acfout, rwv);

		/* find non-zero Rayleigh period */
		int maxindex = DoubleArrayUtils.getMaxElementPosition(acfout);
		rp = maxindex != 0 ? MathUtils.quadraticInterpolation(acfout, maxindex)
				: 1;
		// rp = (maxindex==127) ? 43 : maxindex; //rayparam
		rp = (maxindex == acfout.length - 1) ? rayparam : maxindex; // rayparam

		/* activate biased filterbank */
		checkState();
		// #if 0 // debug metronome mode
		// this.bp = 36.9142;
		// #endif
		/* end of biased filterbank */

		/* deliberate integer operation, could be set to 3 max eventually */
		kmax = (int) Math.floor(dfwv.length / beatPeriod);

		/* initialize output */
		phout = new double[phout.length];
		for (int i = 0; i < beatPeriod; i++) {
			for (int k = 0; k < kmax; k++)
				phout[i] += dfrev[i + (int) Math.round(beatPeriod * k)];
		}
		DoubleArrayUtils.weight(phout, phwv);

		double phase; // beat alignment (step - lastbeat)
		/* find Rayleigh period */
		maxindex = DoubleArrayUtils.getMaxElementPosition(phout);
		if (maxindex >= dfwv.length - 1) {
			if (AUBIO_BEAT_WARNINGS)
				System.err.println("no idea what this groove's phase is\n");
			phase = step - lastbeat;
		} else {
			phase = MathUtils.quadraticInterpolation(phout, maxindex);
		}
		/* take back one frame delay */
		phase += 1.;
		// #if 0 // debug metronome mode
		// phase = step - lastbeat;
		// #endif

		int beatCounter = 0;
		double beatPosition = beatPeriod - phase;

		if (AUBIO_BEAT_WARNINGS)
			System.err.println("beatPeriod: " + beatPeriod + ", phase: "
					+ phase + ", lastbeat: " + lastbeat + ", step: " + step
					+ ", windowLength: " + dfwv.length);

		/*
		 * the next beat will be earlier than 60% of the tempo period skip this
		 * one
		 */
		if ((step - lastbeat - phase) < -0.40 * beatPeriod) {
			if (AUBIO_BEAT_WARNINGS)
				System.err.println("back off-beat error, skipping this beat\n");
			beatPosition += beatPeriod;
		}

		/* start counting the beats */
		while (beatPosition + beatPeriod < 0)
			beatPosition += beatPeriod;

		ArrayList<Double> output = new ArrayList<Double>();
		if (beatPosition >= 0) {
			output.add(beatPosition);
			beatCounter++;
		}

		while (beatPosition + beatPeriod <= step) {
			beatPosition += beatPeriod;
			output.add(beatPosition);
			beatCounter++;
		}

		lastbeat = beatPosition;
		/* store the number of beats in this frame as the first element */
		if (beatCounter > 0)
			output.add(0, (double) beatCounter);
		else
			output.add((double) beatCounter);
		// Note: the IF-Check is to prevent IndexOutOfBoundException in
		// output.add in case no beats were detected.

		double[] result = new double[output.size()];
		for (int i = 0; i < output.size(); i++)
			result[i] = output.get(i);
		return result;
	}

	private int getTimeSignature(double[] acf, int acflen, double gp) {
		double three_energy = 0.;
		double four_energy = 0.;
		if (acflen > 6 * gp + 2) {
			for (int k = -2; k < 2; k++) {
				three_energy += acf[(int) (3 * gp + k)];
				four_energy += acf[(int) (4 * gp + k)];
			}
		} else {
			/* Expanded to be more accurate in time sig estimation */
			for (int k = -2; k < 2; k++) {
				three_energy += acf[(int) (3 * gp + k)]
						+ acf[(int) (6 * gp + k)];
				four_energy += acf[(int) (4 * gp + k)]
						+ acf[(int) (2 * gp + k)];
			}
		}
		return (three_energy > four_energy) ? 3 : 4;
	}

	private void checkState() {
		if (gp > 0) {
			// doshiftfbank again only if context dependent model is in
			// operation
			// acfout = doshiftfbank(acf,gwv,timesig,laglen,acfout);
			// don't need acfout now, so can reuse vector
			// gwv is, in first loop, definitely all zeros, but will have
			// proper values when context dependent model is activated
			acfout = new double[acfout.length];
			for (int i = 1; i < rwv.length - 1; i++)
				for (int a = 1; a <= timesig; a++)
					for (int b = (1 - a); b < a; b++)
						acfout[i] += acf[a * (i + 1) + b - 1];

			DoubleArrayUtils.weight(acfout, gwv);
			gp = MathUtils.quadraticInterpolation(acfout,
					DoubleArrayUtils.getMaxElementPosition(acfout));
		} else {
			// still only using general model
			gp = 0;
		}

		// now look for step change - i.e. a difference between gp and rp that
		// is greater than 2*constthresh - always true in first case, since gp =
		// 0
		if (counter == 0) {
			if (Math.abs(gp - rp) > 2. * g_var) {
				flagstep = 1; // have observed step change.
				counter = 3; // setup 3 frame counter
			} else {
				flagstep = 0;
			}
		}

		int flagconst = 0;
		// i.e. 3rd frame after flagstep initially set
		if (counter == 1 && flagstep == 1) {
			// check for consistency between previous beatperiod values
			if (Math.abs(2. * rp - rp1 - rp2) < g_var) {
				// if true, can activate context dependent model
				flagconst = 1;
				counter = 0; // reset counter and flagstep
			} else {
				// if not consistent, then don't flag consistency!
				flagconst = 0;
				counter = 2; // let it look next time
			}
		} else if (counter > 0) {
			// if counter doesn't = 1,
			counter = counter - 1;
		}

		rp2 = rp1;
		rp1 = rp;

		if (flagconst != 0) {
			/* first run of new hypothesis */
			gp = rp;
			timesig = getTimeSignature(acf, acf.length, gp);
			for (int i = 0; i < rwv.length; i++)
				gwv[i] = Math.exp(-.5 * Math.pow((double) (i + 1. - gp), 2)
						/ Math.pow(g_var, 2));

			flagconst = 0;
			beatPeriod = gp;
			/* flat phase weighting */
			DoubleArrayUtils.resetWithOnes(phwv);
		} else if (timesig != 0) {
			/* context dependant model */
			beatPeriod = gp;
			/* gaussian phase weighting */
			if (step > lastbeat) {
				for (int i = 0; i < 2 * rwv.length; i++)
					phwv[i] = Math.exp(-.5
							* Math.pow((double) (1. + i - step + lastbeat), 2)
							/ (beatPeriod / 8.));

			} else {
				DoubleArrayUtils.resetWithOnes(phwv);
			}
		} else {
			/* initial state */
			beatPeriod = rp;
			/* flat phase weighting */
			DoubleArrayUtils.resetWithOnes(phwv);
		}

		/* do some further checks on the final bp value */

		/* if tempo is > 206 bpm, half it */
		while (beatPeriod < 25) {
			if (AUBIO_BEAT_WARNINGS)
				System.err.println("Doubling from " + beatPeriod + " ("
						+ (60. * 44100. / 512. / beatPeriod) + " bpm) to "
						+ (beatPeriod / 2.) + " ("
						+ (60. * 44100. / 512. / beatPeriod / 2.) + " bpm).");
			beatPeriod = beatPeriod * 2;
		}
	}

	/**
	 * Get current tempo in BPM.
	 * 
	 * @param bt
	 *            beat tracking object
	 * @return Returns the currently observed tempo, in beats per minutes, or 0
	 *         if no consistent value is found.
	 */
	public double getBPM() {
		if (timesig != 0 && counter == 0 && flagstep == 0)
			return 5168. / MathUtils.quadraticInterpolation(acfout,
					(int) beatPeriod);
		else
			return 0.;
	}

	/**
	 * Get current tempo confidence.
	 * 
	 * @param bt
	 *            beat tracking object
	 * @return Returns the confidence with which the tempo has been observed, 0
	 *         if no consistent value is found.
	 */
	public double getConfidence() {
		return gp != 0 ? DoubleArrayUtils.getMaxElement(acfout) : 0.;
	}

	/**
	 * Compute normalised autocorrelation function.
	 * 
	 * @param input
	 *            vector to compute autocorrelation from
	 */
	private double[] getAutocorrelation(double[] input) {
		double[] acf = new double[input.length];
		double tmp = 0.;
		for (int i = 0; i < input.length; i++) {
			for (int j = i; j < input.length; j++) {
				tmp += input[j - i] * input[j];
			}
			acf[i] = tmp / (double) (input.length - i);
			tmp = 0.0;
		}

		return acf;
	}

}

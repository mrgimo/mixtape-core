package ch.hsr.mixtape.features;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class was ported from MATLAB-Files from the MPEG7-Project.
 * 
 * <p>
 * See:<br>
 * <a href="http://mpeg7.doc.gold.ac.uk/mirror/v1/Matlab-XM/h_Common/h_energy.m"
 * >h_energy.m</a> <br>
 * Usage example:<br>
 * <a href=
 * "http://mpeg7.doc.gold.ac.uk/mirror/v1/Matlab-XM/InstrumentTimbreDS/InstrumentTimbreDS.m"
 * >InstrumentTimbreDS.m</a>
 * 
 * TODO: review for correctness
 */
public class EnergyEnvelope {

	/**
	 * @param signal
	 *            vector containing the data of the soundfile
	 * @param samplingRate
	 *            sampling rate in Hz of the soundfile
	 * @param cutFrequency
	 *            cutting frequency in Hz for low-pass filtering of the energy
	 * @param dsfact
	 *            down-sampling factor for the energy [integer] (1=Fe, 2=Fe/2,
	 *            3=Fe/3, ...)
	 * @return
	 */
	public double[][] getEnergyEnvelope(double[] signal, int samplingRate,
			int cutFrequency, int dsfact) {
		int L_n = Math.round(samplingRate / (2 * cutFrequency));
		L_n = L_n + (L_n % 2 == 0 ? 1 : 0); // L_n + ~rem(L_n,2) => ensures that
											// number is always odd.
		int LD_n = (L_n - 1) / 2;
		ArrayList<Integer> mark_n_v = getMarkArray(LD_n + 1, dsfact,
				signal.length - LD_n);

		double[] time_sec_v = new double[mark_n_v.size()];
		for (int i = 0; i < mark_n_v.size(); i++) {
			time_sec_v[i] = mark_n_v.get(i).doubleValue() / samplingRate;
		}

		double[] energy_v = new double[mark_n_v.size()];
		for (int index = 0; index < mark_n_v.size(); index++) {
			Integer n = mark_n_v.get(index);
			double[] window = Arrays
					.copyOfRange(signal, n - LD_n - 1, n + LD_n);
			energy_v[index] = Math
					.sqrt(getSquaredSignalSum(getSignalDC(window)) / L_n);
		}

		return mergeTimeAndEnergy(time_sec_v, energy_v);
	}

	private double[][] mergeTimeAndEnergy(double[] time_sec_v, double[] energy_v) {
		double[][] result = new double[time_sec_v.length][2];

		for (int i = 0; i < time_sec_v.length; i++) {
			result[i][0] = time_sec_v[i];
			result[i][1] = energy_v[i];
		}
		return result;
	}

	private ArrayList<Integer> getMarkArray(int startValue, int stepSize,
			int maximumValue) {
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = startValue; i <= maximumValue; i += stepSize) {
			result.add(i);
		}

		return result;
	}

	private double[] getSignalDC(double[] signal) {
		double sum = 0.0;
		for (int i = 0; i < signal.length; i++) {
			sum += signal[i];
		}
		double mean = sum / signal.length;

		for (int i = 0; i < signal.length; i++) {
			signal[i] = signal[i] - mean;
		}
		return signal;
	}

	private double getSquaredSignalSum(double[] signal) {
		double sum = 0.0;
		for (int i = 0; i < signal.length; i++) {
			sum += signal[i] * signal[i];
		}
		return sum;
	}
}

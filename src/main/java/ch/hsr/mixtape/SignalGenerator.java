package ch.hsr.mixtape;

public class SignalGenerator {

	private static final int MULTIPLY = 4;

	public static final double C = 261.63 * MULTIPLY;
	public static final double CIS = 277.18 * MULTIPLY;
	public static final double D = 293.66 * MULTIPLY;
	public static final double DIS = 311.13 * MULTIPLY;
	public static final double E = 329.63 * MULTIPLY;
	public static final double F = 349.23 * MULTIPLY;
	public static final double FIS = 369.99 * MULTIPLY;
	public static final double G = 392.00 * MULTIPLY;
	public static final double GIS = 415.30 * MULTIPLY;
	public static final double A = 440.00 * MULTIPLY;
	public static final double AIS = 466.16 * MULTIPLY;
	public static final double B = 493.88 * MULTIPLY;

	private int sampleRate;
	
	public SignalGenerator(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public byte[] generate(double hertz, long timeInMillis) {
		byte[] samples = new byte[(int) ((sampleRate * timeInMillis) / 1000)];
		for (int i = 0; i < samples.length; i++)
			samples[i] = (byte) (Math.sin(i * ((2 * Math.PI * hertz) / sampleRate)) * Byte.MAX_VALUE);

		return samples;
	}

}

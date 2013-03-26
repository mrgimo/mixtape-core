package ch.hsr.mixtape.features;

public class LogConstantQ {

	public double[] extractFeature(double[] samples, double sampling_rate, double[] constantQ) throws Exception {
		double[] logConstantQ = new double[constantQ.length];
		for (int i = 0; i < constantQ.length; ++i) {
			logConstantQ[i] = Math.log(constantQ[i]);
			if (logConstantQ[i] < -50.0)
				logConstantQ[i] = -50.0;
		}

		return logConstantQ;
	}

}
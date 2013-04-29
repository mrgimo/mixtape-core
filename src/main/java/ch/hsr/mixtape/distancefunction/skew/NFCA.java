package ch.hsr.mixtape.distancefunction.skew;

public class NFCA {

	public int[] numberOfFirsCommontAncestors(int[] lcp) {

		int[] nfcas = new int[lcp.length];

		for (int pos = 0; pos < lcp.length;) {

			int nextPos = pos + 1;

			while (nextPos < lcp.length && lcp[nextPos++] > 0)
				nfcas[pos]++;

			pos += nfcas[pos] + 1;
		}

		return nfcas;
	}

}

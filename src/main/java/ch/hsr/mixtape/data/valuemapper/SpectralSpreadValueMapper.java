package ch.hsr.mixtape.data.valuemapper;




public class SpectralSpreadValueMapper implements ValueMapper {
	
	/*
	 * spectral spread: 0.19695643523647455
	 * spectral spread: 0.2329136822022278
	 */

	@Override
	public int mapValueToInt(double value) {
		return (int) (value * 100 + 1);
	}

}

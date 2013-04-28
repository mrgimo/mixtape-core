package ch.hsr.mixtape.data.valuemapper;


public class SpectralKurtosisValueMapper implements ValueMapper {
	
	/*
	 * spec kurtosis: 36057.254808918005
	 * spec kurtosis: 44587.96651042575
	 */

	@Override
	public int mapValueToInt(double value) {
		return (int) (value / 10 + 1);
	}

}

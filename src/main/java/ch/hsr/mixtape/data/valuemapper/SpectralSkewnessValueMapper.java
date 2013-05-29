package ch.hsr.mixtape.data.valuemapper;



public class SpectralSkewnessValueMapper implements ValueMapper {

	@Override
	public int mapValueToInt(double value) {
		return (int) (value + 10000);
	}

}

package ch.hsr.mixtape.data.valuemapper;

public class MFCCValueMapper implements ValueMapper {

	@Override
	public int mapValueToInt(double value) {
		return (int) ((value + 100) * 100);
	}

}

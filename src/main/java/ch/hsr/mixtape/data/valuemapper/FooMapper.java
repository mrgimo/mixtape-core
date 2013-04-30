package ch.hsr.mixtape.data.valuemapper;

public class FooMapper implements ValueMapper {

	@Override
	public int mapValueToInt(double value) {
		return (int) value;
	}

}

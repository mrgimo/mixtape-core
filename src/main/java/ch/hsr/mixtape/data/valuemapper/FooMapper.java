package ch.hsr.mixtape.data.valuemapper;

public class FooMapper implements ValueMapper {

	@Override
	public int mapValueToInt(double value) {
//		System.out.println("foo maped " + value + " to " + ((int) value + 1));
		return (int) value + 1;
	}

}

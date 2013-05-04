package ch.hsr.mixtape.data.valuemapper;

public class MFCCValueMapper implements ValueMapper {

	@Override
	public int mapValueToInt(double value) {
//		System.out.println("raw value" + value +"\nintVal: " + (int)(value + 100));
		return (int) ((value + 100) * 10);
	}

}

package ch.hsr.mixtape.data;

public class Feature <T extends Number> {
	
	private String name;
	private T value;

	public Feature(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	public double doubleValue() {
		return (double) value.doubleValue();
	}
	
	public String getName() {
		return name;
	}

}

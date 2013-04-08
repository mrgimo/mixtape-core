package ch.hsr.mixtape.data;

/*
 * worschinlich besser als interface und implementationa mit definiarta datatypa und 
 * eigeni implementation zum nach double z convertiara( doubleValue() ) ?!
 */

public class Feature<T extends Number> {

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

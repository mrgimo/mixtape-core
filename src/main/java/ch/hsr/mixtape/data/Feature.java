package ch.hsr.mixtape.data;

import ch.hsr.mixtape.data.valuemapper.ValueMapper;

public class Feature {

	private ValueMapper valueMapper;
	
	private String name;
	private int[] values = new int[0];

	private int[] suffixArray = new int[0];
	private int[] lcp = new int[0];

	private int addedWindows = 0;

	private int[] nfcas;

	public Feature(String name, int windowCount, ValueMapper valueMapper) {
		this.name = name;
		values = new int[windowCount];
		this.valueMapper = valueMapper;
		
	}

	public String getName() {
		return name;
	}

	public void addWindowValue(double windowValue) {
		values[addedWindows++] = valueMapper.mapValueToInt(windowValue);
	}

	public int maxValue() {
		
		int maxValue = 0;
		for (int i = 0; i < values.length; i++) {
			if(values[i] > maxValue)
				maxValue = values[i];
		}
		return maxValue;
	}

	public int[] getSuffixArray() {
		return suffixArray ;
	}

	public int[] getLcp() {
		return lcp ;
	}

	public void setLcp(int[] lcpValues) {
		lcp = lcpValues;
	}

	public void setSuffixArray(int[] suffixArray) {
		this.suffixArray = suffixArray;
	}

	public int[] windowValues() {
		return values;
	}

	public void addWindowValues(int[] values) {
		this.values = values;
	}

	public void setNFCAs(int[] nfcas) {
		this.nfcas = nfcas;
		
	}
	
	public int[] getNFCAs() {
		return nfcas;
	}

	public int windowCount() {
		return values.length;
	}
}

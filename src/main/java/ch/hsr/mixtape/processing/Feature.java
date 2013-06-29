package ch.hsr.mixtape.processing;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import ch.hsr.mixtape.nid.LongestCommonPrefixBuilder;
import ch.hsr.mixtape.nid.SuffixArrayBuilder;

import com.google.common.primitives.Ints;

@Entity
public class Feature implements Serializable {
	
	private static final long serialVersionUID = -8556725113974715410L;

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	public int[] values = {};
	
	@Transient
	private int[] suffixArray;
	@Transient
	private int[] lcpOfSuffixArray;
	
	public Feature() {
		
	}
	
	public synchronized int[] getSuffixArray() {

		if (!isComputed(suffixArray))
			suffixArray = new SuffixArrayBuilder()
					.buildSuffixArray(values, Ints.max(values));

		return suffixArray;
	}
	
	
	public synchronized int[] getLcpOfSuffixArray() {
		
		if(!isComputed(lcpOfSuffixArray))
			lcpOfSuffixArray = new LongestCommonPrefixBuilder().longestCommonPrefixes(values, getSuffixArray());
		
		return lcpOfSuffixArray;
		
	}
	
	private boolean isComputed(int[] array) {
		return array != null;
	}

}

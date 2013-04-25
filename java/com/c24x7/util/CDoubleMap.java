/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;



		/**
		 * <p>Hash map for double floating point values.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date July 7, 2012 12:11:29 PM
		 */
public final class CDoubleMap extends HashMap<String, Double> {
	private static final long serialVersionUID = -521163626930974445L;

		/**
		 * <p>Comparator for hash table for double that excludes equal terms. 
		 * Contrary to the default comparator used in TreeSet and TreeMap this comparator
		 * return 1 for greater or equals and -1 for less operators
		 * @author Patrick Nicolas
		 * @date June 23, 2012
		 */
	protected class NDecreasingComparator implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			return (get(o1).doubleValue() < get(o2).doubleValue()) ? 1 : -1;
		}
	}
	
		/**
		 * <p>Comparator for hash table for double that excludes equal terms. 
		 * Contrary to the default comparator used in TreeSet and TreeMap this comparator
		 * return 1 for greater or equals and -1 for less operators
		 * @author Patrick Nicolas
		 * @date June 23, 2012
		 */
	protected class NIncreasingComparator implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			return (get(o1).doubleValue() >= get(o2).doubleValue()) ? 1 : -1;
		}
	}
	
	/**
	 * <p>Increment the value associated to a key on this integer map by an arbitrary value</p>
	 * @param key key of the key-value pair {'key",value}
	 * @param value value to be incremented
	 */
	public Double put(String key, double value) {
		Double oldValue = get(key);
		
		if ( oldValue != null ) {
			value += oldValue.intValue();
		}
		return super.put(key, new Double(value));
	}
	
	
	/**
	 * <p>Extracted the ordered set of keys for this hash map
	 * of <key,float point value> pairs in decreasing order of values</p>
	 * @return Set of keys ordered by decreasing values.
	 */
	public Set<String> order() {
		return order(true);
	}
	

	/**
	 * <p>Extracted the ordered set of keys for this hash map
	 * of <key,float point value> pairs.</p>
	 * @param decreasingOrder  The key, value pairs are ordered in decreasing value if true, increasing values if false.
	 * @return Set of keys ordered by decreasing values or increasing values.
	 */
	public Set<String> order(boolean decreasingOrder) {
		Comparator<String> cmp = decreasingOrder ? new NDecreasingComparator() : new NIncreasingComparator();
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TreeMap<String, Double> treeMap = new TreeMap(cmp);
		for(String key : keySet()) {
			treeMap.put(key, get(key));
		}	
		return treeMap.keySet();
	}
}

// -------------------------  EOF ------------------------------
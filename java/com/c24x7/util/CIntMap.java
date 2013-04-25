// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;



	/**
	 * <p>
	 * Convenient class to override the standard put/insert method
	 * for non-synchronized hash tables (Map)</p>
	 * @author Patrick Nicolas
	 * @date 02/14/2012
	 */
public class CIntMap extends HashMap<String, Integer> {
	static final long  serialVersionUID = 931433L;

		/**
		 * <p>Comparator for Integer that excludes equal terms. Contrary to the
		 * default comparator used in TreeSet and TreeMap this comparator
		 * return 1 for greater or equals and -1 for less operators
		 * @author Patrick Nicolas
		 * @date 01/15/2012
		 */
	protected class NComparator implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			return (get(o1).intValue() >= get(o2).intValue()) ? 1 : -1;
		}
	}
	
	
	public CIntMap() {
		super();
	}
	
	
	public void putAll(CIntMap map) {
		for( String key : map.keySet() ) {
			this.put(key);
		}
	}
	
	public void putAll(CIntMap map, int n) {
		for( String key : map.keySet() ) {
			this.put(key, n);
		}
	}
	
	
	public final int getInt(final String key) {
		Integer value = get(key);
		return (value == null) ? -1 : value.intValue();
	}
	
			/**
			 * <p>Increment the value associated to a key on this integer map, by 1</p>
			 * @param key key of the key-value pair {'key", +1}
			 */
	public void put(final String key) {
		Integer oldValue = get(key);
		int newValue = 1;
		
		if ( oldValue != null ) {
			newValue += oldValue.intValue();
		}
		super.put(key, new Integer(newValue));
	}
	
	/**
	 * <p>Increment the value associated to a key on this integer map by an arbitrary value</p>
	 * @param key key of the key-value pair {'key",value}
	 * @param value value to be incremented
	 */
	public Integer put(final String key, final Integer value) {
		return super.put(key, value);
	}
	
	
	/**
	 * <p>Increment the value associated to a key on this integer map by an arbitrary value</p>
	 * @param key key of the key-value pair {'key",value}
	 * @param value value to be incremented
	 */
	public Integer put(String key, int value) {
		Integer oldValue = get(key);
		
		if ( oldValue != null ) {
			value += oldValue.intValue();
		}
		return super.put(key, new Integer(value));
	}
	
	
	public final Set<String> order() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TreeMap<String, Integer> treeMap = new TreeMap(new NComparator());
		for(String key : keySet()) {
			treeMap.put(key, get(key));
		}
	
		return treeMap.keySet();
	}
	
		
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for( String key : keySet() ) {
			buf.append("\n");
			buf.append(key);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(get(key));
		}
		
		return buf.toString();
	}

}
// ----------------------  EOF ------------------------
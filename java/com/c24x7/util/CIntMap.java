// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.util;

import java.util.HashMap;


/**
 * <p>
 * Convenient class to override the standard put/insert method
 * for non-synchronized hash tables (Map)</p>
 * @author Patrick Nicolas
 */
public final class CIntMap extends HashMap<String, Integer> {
	static final long  serialVersionUID = 931433L;
	
	public static final int NUM_INTEGER = 32;
	public static final Integer[] values = new Integer[NUM_INTEGER];
	static {
		for( int j = 0; j < NUM_INTEGER; j++) {
			values[j] = new Integer(j);
		}
	}
	
	public static final Integer getInteger(int j) {
		return (j >= NUM_INTEGER) ? null : values[j];
	}
	
	
	public CIntMap() {
		super();
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
		return put(key, value.intValue());
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

}
// ----------------------  EOF ------------------------
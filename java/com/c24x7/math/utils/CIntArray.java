/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.math.utils;

import java.util.Arrays;





		/**
		 * <p>Class that implements a dynamic array of integers or indices. The array
		 * can only expand but not shrink.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 02, 2012 9:01:40 PM
		 */
public final class CIntArray extends AArray {	
	protected final static int EXPANSION_SIZE = 6;

	protected int[] _values = null;
	
	
		/**
		 * <p>Create a dynamic array of integers with a single integer.</p>
		 * @param value first value in the dynamic array
		 */
	public CIntArray() {	
		super();
		_values = new int[EXPANSION_SIZE];
	}
	
		/**
		 * <p>Create a dynamic array of integers with a single integer.</p>
		 * @param value first value in the dynamic array
		 */
	public CIntArray(int value) {	
		super();
		_values = new int[EXPANSION_SIZE];
		_values[++_index] = value;
	}
	
	/**
	 * <p>Create a dynamic array of integers from an existing array of integers.</p>
	 * @param value first value in the dynamic array
	 */
	public CIntArray(int[] values) {
		super( values.length-1);
		initialize(values);
	}
	
	
	
		/**
		 * <p>Clone this dynamic array of integers or indices.</p>
		 * @return deep copy of 'this' object
		 */
	@Override
	public CIntArray clone() {
		return new CIntArray(_values, _index);
	}
	
	
	
	/**
	 * <p>Retrieve the capacity of the dynamic array as the actual size of 
	 * the allocated memory not the actual number of integers in the array.</p>
	 * @return current capacity of the array.
	 */
	public int capacity() {
		return _values.length;
	}
	
	
	public int value(int index) {
		if( index < 0 || index > _index) {
			throw new ArrayIndexOutOfBoundsException("Incorrect access to CIntArray content");
		}
		
		return _values[index];
	}
	
	public int[] values() {
		return _values;
	}
	
	public int[] getValues() {
		return Arrays.copyOf(_values, _index+1);
	}
		
		/**
		 * <p>Add a new value to this array of integer.
		 * @param value value added to the array of integers.
		 */
	public void add(int value) {
		if( _index >= _values.length-1) {
			int[] oldArray = Arrays.copyOf(_values, _index + (EXPANSION_SIZE>>1));
			_values = oldArray;
		}
		_values[++_index] = value;			
	}
	
	
	
		/**
		 * <p>Extends this dynamic array with a new one.</p>
		 * @param intArray dynamic array to be added.
		 */
	public void add(final CIntArray intArray) {
		if( intArray == null) {
			throw new IllegalArgumentException("Cannot add an empty array");
		}
		
		int[] values = intArray.values();
		final int newIndex = _index + intArray.index();
			/*
			 * Re-allocate (expand) the current array
			 */
		if( newIndex >= _values.length-1) {
			int[] oldArray = Arrays.copyOf(_values, newIndex + EXPANSION_SIZE);
			_values = oldArray;
		}
			
			/*
			 * Initialize the content of the newly resized array
			 */
		for( int k = 0; k <= intArray.index(); k++) {

				/*
				if( !hasValue(values[k])) {
					_values[++_index] = values[k];
				}
				*/
			_values[++_index] = values[k];
		}
	}
	

	/**
	 * <p>Update the sum of the values with the current value of index k.</p>
	 * @param k index of the new value
	 * @return return the double floating point value of the kth+1 observations.
	 */
	@Override
	protected double getValue(int k) {
		return _values[k];
	}
	
	/**
	 * <p>Test if the dynamic array has been allocated and contains values.</p>
	 * @return true if dynamic array is not initialized, false othewise
	 */
	@Override
	protected boolean isEmpty() {
		return (_values == null);
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int k = 0; k <= _index; k++) {
			buf.append(_values[k]);
			buf.append(" ");
		}		
		return buf.toString();
	}



							// ----------------
							// Private Methods
							// -----------------
	
	/*
	private boolean hasValue(int value) {
		boolean found = false;
		
		for(int k = 0; k <= _index; k++) {
			if(_values[k] == value) {
				found = true;
				break;
			}
		}
		
		return found;
	}
	*/
	
	
	private CIntArray(int[] values, int index){ 
		super(index);
		initialize(values);
	} 
	
	
	private void initialize(int[] values) {
		_values = new int[values.length + (EXPANSION_SIZE>>1)];
		for( int k = 0; k < values.length; k++) {
			_values[k] = values[k];
		}
	}
}

// -----------------------------------------------  EOF -----------------------------------------------
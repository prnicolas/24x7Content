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
public final class CFreqArray extends AArray {	
	protected final static int EXPANSION_SIZE = 6;

	protected int[] _values 	 = null;
	protected int[] _frequencies = null;
	
		/**
		 * <p>Default constructor for the array of frequencies which
		 * allocated array of integers for values and frequencies without
		 * initialization.</p>
		 */
	public CFreqArray() {
		super();
		_values = new int[EXPANSION_SIZE];
		_frequencies = new int[EXPANSION_SIZE];
	}

		/**
		 * <p>Create a dynamic array of integers with a single integer.</p>
		 * @param value first value in the dynamic array
		 */
	public CFreqArray(int value) {	
		super();
		_values = new int[EXPANSION_SIZE];
		_frequencies = new int[EXPANSION_SIZE];
		_values[0] = value;
		_frequencies[0] = 1;
		_index = 0;
	}
	
	/**
	 * <p>Create a dynamic array of integers from an existing array of integers.</p>
	 * @param values array of integers used to initialize the dynamic array.
	 */
	public CFreqArray(int[] values) {
		final int newSize = values.length + (EXPANSION_SIZE>>1);
		_values = new int[newSize];
		_frequencies = new int[newSize];
		
		for( int k = 0; k < values.length; k++) {
			add(values[k]);
		}
	}
	
	
	
		/**
		 * <p>Clone this dynamic array of integers or indices.</p>
		 * @return deep copy of 'this' object
		 */
	@Override
	public CFreqArray clone() {
		return new CFreqArray(_values, _index);
	}
	
	
	
	/**
	 * <p>Retrieve the capacity of the dynamic array as the actual size of 
	 * the allocated memory not the actual number of integers in the array.</p>
	 * @return current capacity of the array.
	 */
	public int capacity() {
		return _values.length;
	}
	
	
	public int getFrequency(int index) {
		if( index < 0 || index > _index) {
			throw new ArrayIndexOutOfBoundsException("Incorrect index for frequency in dynamic array");
		}
		
		return _frequencies[index];
	}
	
	
	public int[] getValues() {
		return Arrays.copyOf(_values, _index+1);
	}
	
	public int[] values() {
		return _values;
	}
	
	public int[] frequencies() {
		return _frequencies;
	}
	
	public int[] getFrequencies() {
		return Arrays.copyOf(_frequencies, _index+1);
	}
	
	public void add(int value) {
		this.add(value, 1);
	}
	
		/**
		 * <p>Shift the values of the dynamic array.</p>
		 * @param shiftValue value of the shift to be applied to all the values of the frequency table.
		 */
	public void shiftValueBy(int shiftValue) {
		for( int k = 0; k <= _index; k++) {
			_values[k] += shiftValue;
		}
	}
	
		
		/**
		 * <p>Add a new value to this array of integer.
		 * @param value value added to the array of integers.
		 */
	public void add(int value, int frequency) {
		if( !hasValue(value)) {
			if( _index >= _values.length-1) {
				final int newSize = _index + (EXPANSION_SIZE>>1);
				_values = Arrays.copyOf(_values, newSize);
				_frequencies = Arrays.copyOf(_frequencies, newSize);
			}
			_index++;
			_values[_index] = value;
			_frequencies[_index] += frequency;
		}
	}
	
	
	
		/**
		 * <p>Extends this dynamic array with a new one.</p>
		 * @param intArray dynamic array to be added.
		 */
	public void add(final CFreqArray intArray) {
		if( intArray == null) {
			throw new IllegalArgumentException("Cannot add an empty array");
		}
		
		int[] values = intArray.values();
		int[] frequencies = intArray.frequencies();
		final int newIndex = _index + intArray.index();
			/*
			 * Re-allocate (expand) the current array, by being
			 * conservative in our assumption.
			 */
		if( newIndex >= _values.length-1) {
			_values = Arrays.copyOf(_values, newIndex + 1);
			_frequencies = Arrays.copyOf(_frequencies, newIndex + 1);
		}
			/*
			 * Initialize the content of the newly resized array
			 */
		for( int k = 0; k <= intArray.index(); k++) {
			this.add(values[k], frequencies[k]);
		}
	}
	
	/**
	 * <p>Computes the mean and variance of values of a dynamic array of integers
	 * or floating point values and returns the array of mean and variance.</p>
	 * @return two floating point array of {mean, variance} of the values in the dynamic array
	 * @throws IllegalArgumentException  if the dynamic array is null
	 */
	
	@Override
	public double[] computeStats() {
		if( isEmpty()) {
			throw new IllegalArgumentException("Cannot compute mean of undefined data");
		}

		double sum 			= 0.0,
		       squareSum 	= 0.0,
		       minValue 	= Double.MAX_VALUE;
			 
		/*
		 * Compute the minimal value 
		 */
		for( int k = 0; k <= _index; k++) {
			if(minValue > getValue(k)) {
				minValue = getValue(k);
			 }
		}
		
		double diff = 0.0;
		int numData = 0;
		for( int k = 0; k <= _index; k++) {
			diff = (getValue(k) - minValue)*getFrequency(k);
			sum += diff;
			squareSum += diff*diff;
			numData += getFrequency(k);
		}
		double mean = sum/numData;
		
		return new double[] { mean, (squareSum - mean*mean)/numData };
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
			buf.append(",");
			buf.append(_frequencies[k]);
			buf.append(" ");
		}		
		return buf.toString();
	}



							// ----------------
							// Private Methods
							// -----------------
	
	private boolean hasValue(int value) {
		boolean found = false;
		
		for(int k = 0; k <= _index; k++) {
			if(_values[k] == value) {
				_frequencies[k]++;
				found = true;
				break;
			}
		}
		
		return found;
	}
	
	
	private CFreqArray(int[] values, int index){
		super(index);

		final int arraySize = values.length + (EXPANSION_SIZE>>1);
		_values = Arrays.copyOf(values, arraySize);
		_frequencies = new int[arraySize];
		
		for( int k = 0; k < arraySize; k++) {
			_frequencies[k] = 1;
		}
	}
}

// -----------------------------------------------  EOF -----------------------------------------------
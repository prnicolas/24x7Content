/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.math.utils;


		/**
		 * <p>Generic class that defined dynamic arrays of built-in
		 * types such as integer, floating point values. The dynamic
		 * arrays can only expands but not shrink.
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 11, 2012 6:49:36 PM
		 */
public abstract class AArray {
	protected final static int EXPANSION_SIZE = 6;

	protected int   _index 		= -1;
	protected int	_expandRate = EXPANSION_SIZE;
	
	
	public AArray() { }
	
	public AArray(int index) {
		_index = index;
	}
	
	
	
	/**
	 * <p>Compute the variance of values of a dynamic array of integers
	 * or floating point values. This method requires the pre-computation
	 * of the mean value, defined as one of the parameters.</p>
	 * @param mean value of the dynamic array
	 * @return variance of values of values in the dynamic array
	 * @throws IllegalArgumentException  if the dynamic array is null
	 */
	public double variance(double mean) {
		double sum = 0.0;
		double diff = 0.0;
		
		for( int k = 0; k <= _index; k++) {
			diff = getValue(k) -mean;
			sum += diff*diff;
		}
		
		return sum/(_index+1);
	}
	
	
	/**
	 * <p>Computes the mean and variance of values of a dynamic array of integers
	 * or floating point values and returns the array of mean and variance.</p>
	 * @return two floating point array of {mean, variance} of the values in the dynamic array
	 * @throws IllegalArgumentException  if the dynamic array is null
	 */
	public double[] computeStats() {
		if( isEmpty()) {
			throw new IllegalArgumentException("Cannot compute mean of undefined data");
		}

		double sum 			= 0.0,
		       squareSum 	= 0.0,
		       minValue 	= Double.MAX_VALUE;
			 
		for( int k = 0; k <= _index; k++) {
			if(minValue > getValue(k)) {
				minValue = getValue(k);
			 }
		}
		
		double diff = 0.0;
		for( int k = 0; k <= _index; k++) {
			diff = getValue(k) - minValue;
			sum += diff;
			squareSum += diff*diff;
		}
		double mean = sum/(_index+1);
		
		return new double[] { mean, (squareSum - mean*mean)/(_index+1) };
	}
	
	
	

	/**
	 * <p>Update the sum of the values with the current value of index k.</p>
	 * @param k index of the new value
	 * @return return the double floating point value of the kth+1 observations.
	 */
	abstract protected double getValue(int k);
	
	/**
	 * <p>Test if the dynamic array has been allocated and contains values.</p>
	 * @return true if dynamic array is not initialized, false othewise
	 */
	abstract protected boolean isEmpty();
	
		
	
	public void setExpandRate(int expandRate) {
		_expandRate = expandRate;
	}
		/**
		 * <p>Retrieve the size of the dynamic array as the number of elements
		 * in the array, not the actual size of the allocated memory.</p>
		 * @return number of integers in the array.
		 */
	public int size() {
		return _index+1;
	}

	
	public int index() {
		return _index;
	}
}

// ---------------------  EOF -------------------------------------------
/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.math.lsregression;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.c24x7.exception.ClassifierException;
import com.c24x7.math.utils.CDoubleArray;



			/**
			 * <p>Class that implements Least Square Regression for multiple
			 * variables, from Apache commons math3 packages. This class uses 
			 * dynamic array for storing observed data for maximum computation efficiency. 
			 * The user can specify the transform function F to be applied to 
			 * the original observations (log, power,...)<br>
			 * y = F(x).A + b (i.e  y[3] = a[0].log(x[3][0]) + a[1].log(x[3][1]) + a[2]) 
			 * 
			 * @author Patrick Nicolas         24x7c 
			 * @date June 17, 2012 10:38:38 AM
			 */
public final class CLSRegression {
	private CDoubleArray _values 			= null;
	private double[]  	_parameters 		= null;
	private int		 	_numVariables 		= -1;
	private int		 	_numObservations 	= -1;
	private double		_rSquareStats		= 0.0;
	
	
		/**
		 * <p>Create a default instance of the Multivariate least
		 * square regression using the linear data transform.</p>
		 */
	public CLSRegression() {}
	

	
			/**
			 * <p>Add a new array of observations values to the current dynamic
			 * array that contains the existing observations. The length of the
			 * array should be consistent with the actual number of variables used
			 * in the least square regression..</p>
			 * 
			 * @param data new batch of observed data
			 * @throws IllegalArgumentException if the new batch of observation is undefined.
			 */
	public void addData(double[] data) {
		if( data == null || data.length ==0) {
			throw new IllegalArgumentException("Cannot add undefined data as input to LS Regression");
		}
		
			/*
			 * Format the dynamic array to hold observed data
			 */
		if( _numVariables == -1) {
			_numVariables = data.length-1;
			_values = new CDoubleArray();
		}
		
		for(int k = 0; k < data.length; k++) {
			_values.add(data[k]);
		}
	}
	
	
	public double getRSquareStats() {
		return _rSquareStats;
	}
	
			/**
			 * <p>Add a new array of observations values to the current dynamic
			 * array that contains the existing observations. The length of the
			 * array should be consistent with the actual number of variables used
			 * in the least square regression..</p>
			 * 
			 * @param data new batch of observed data
			 * @throws IllegalArgumentException if the new batch of observation is undefined.
			 */
	public void addData(double[] data, int numVariables) {
		if( data == null || data.length ==0) {
			throw new IllegalArgumentException("Cannot add undefined data as input to LS Regression");
		}
		
			/*
			 * Format the dynamic array to hold observed data
			 */
		if( _numVariables == -1) {
			_numVariables = numVariables;
			_values = new CDoubleArray(data);
		}
	}

	
	
		/**
		 * <p>Compute the parameters of the Least Square Regression.</p>
		 * @return R statistics as sqrt(1 - Sum of Square Residuals/Sum of squares);
		 * @throws ClassifierException if no observations was defined or the number of observations is too small (undefitted).
		 */
	public int compute() throws ClassifierException {
			
		/*
		 * Computes the number of observations
		 */
		double[] values =  _values.currentValues();
		if(values == null) {
			throw new ClassifierException("Cannot compute LS Regression on undefined observations");
		}

		
		_numObservations = values.length/(_numVariables +1);
		if(_numObservations < _numVariables<<2) {
			throw new ClassifierException("Not enough observations to compute LS Regression");
		}
	
		/*
		 * Calls the Apache Math3 default Least square class.
		 */
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		try {
			regression.newSampleData(values, _numObservations, _numVariables);
			_parameters = regression.estimateRegressionParameters();
			_rSquareStats = regression.calculateRSquared();

		}
		catch( IllegalArgumentException e) {
			throw new ClassifierException("Incorrect arguments for LS regression: " + e.toString());
		}
		
		return _numObservations;
	}
	
	
		/**
		 * <p>Retrieve the parameters for this LS Regression.</p>
		 * @return array of parameters computed by the regression
		 */
	public double[] getParameters() {
		return _parameters;
	}
	
	
		/**
		 * <p>Display the parameters of the LS regression.</p>
		 * @return textual representation of the LS regression parameters.
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if( _parameters != null) {
			for( int k = 0; k < _parameters.length; k++) {
				buf.append("b[");
				buf.append(k);
				buf.append("]=");
				buf.append(_parameters[k]);
				buf.append(" ");
			}
		}
		
		return buf.toString();
	}
}

// -------------------------  EOF -----------------------------

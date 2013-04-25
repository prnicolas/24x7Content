/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.math.bayes;

import com.c24x7.exception.ClassifierException;
import com.c24x7.math.utils.CDoubleArray;


		/**
		 * <p>Generic class that implements the Multinomial Naive
		 * Bayes classifier. The number of model features and classes
		 * is arbitrary. The run-time classification can use different
		 * discrimants (log, logistic) </p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 29, 2012 12:02:20 AM
		 */
public final class CMNNaiveBayes {
	private final static double EPS = 0.001;

		/**
		 * <p>Class that implement a classifier class which is defined
		 * by its features (computed as mean value), variance on the features
		 * value and its probability.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 29, 2012 12:08:56 AM
		 */
	public final class NClass {
		private double[] _parameters		= null;
		private double[] _paramsVariance 	= null;
		private double	 _classProb 		= 0.0;
		
		public NClass(int numParameters) {
			_parameters = new double[numParameters];
		}
		
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			for( int k = 0; k < _parameters.length; k++) {
				buf.append(_parameters[k]);
				buf.append(" ");
			}
			
			for( int k = 0; k < _parameters.length; k++) {
				buf.append("; ");
				buf.append(Math.sqrt(_paramsVariance[k]));
				buf.append(" ");
			}
			buf.append("\n");
			buf.append(_classProb);
			
			return buf.toString();
		}

		private void add(double[] data) {
			int numObservations = 0;
			_paramsVariance = new double[_parameters.length];
			for(int j = 0; j < data.length; ) {
				j++;
				for( int k = 0; k < _parameters.length; k++, j++) {
					_parameters[k] += data[j];
					_paramsVariance[k] += data[j]*data[j];
				}
				numObservations++;
			}
			_classProb = numObservations;
		}
		
	
		
		private void computeStats() {
			double 	invClassProb = 1.0/_classProb;
			double  invClassProbCube = invClassProb*invClassProb*invClassProb;
			
			for( int k = 0; k < _parameters.length; k++) {
				_parameters[k] /= _classProb;
				_paramsVariance[k] = _paramsVariance[k]*invClassProb - _parameters[k]*_parameters[k]*invClassProbCube;
			}
			_classProb /= _numObservations;
		}		
	}
	
	public interface NKernel {
		public double estimate(double value);
	}
	
	public static class NLinearKernel implements NKernel {
		public double estimate(double value) {
			return value;
		}
	}
	
	private CDoubleArray[] 	_valuesArray 		= null;
	private NClass[]  		_classes 			= null;
	private int		 		_numObservations 	= 0;
	private	int				_step				= 1;
	private NKernel			_kF					= null;
	
	
	public CMNNaiveBayes() { }
	

	public CMNNaiveBayes(int numParameters, int numClasses) {
		this(numParameters, numClasses, new NLinearKernel());
	}
		
	
	public CMNNaiveBayes(int numParameters, int numClasses, final NKernel kf) {
		_classes = new NClass[numClasses];
		_valuesArray = new CDoubleArray[numClasses];
		
		for( int k = 0; k < numClasses; k++) {
			_classes[k] = new NClass(numParameters);
			_valuesArray[k] = new CDoubleArray();
		}
		
		_kF = kf;
		discretize(0,numClasses);
	}
	
	
	public void setClassParameter(int classid, int paramid, double paramValue) {
		_classes[classid]._parameters[paramid] = paramValue;
	}
	
	
	public double getClassParameter(int classid, int paramid) {
		return _classes[classid]._parameters[paramid];
	}
	
	public double getClassParameterVar(int classid, int paramid) {
		return _classes[classid]._paramsVariance[paramid];
	}

		/**
		 * <p>Retrieve the parameters for this LS Regression.</p>
		 * @return array of parameters computed by the regression
		 */
	public double[] getClassParameters(int classId) {
		if( classId >= _classes.length) {
			throw new IllegalArgumentException("Incorrect class id " + classId + " for Naive Bayes Classifier");
		}
		
		return _classes[classId]._parameters;
	}
	
	
	public final int getNumClasses() {
		return _classes.length;
	}
	
	public final int getNumClassParameters() {
		return _classes[0]._parameters.length;
	}
	
	
	public void setClassProb(int classId, double value) {
		_classes[classId]._classProb = value;
	}
	
	
	public double getClassProb(int classId) {
		if( classId >= _classes.length) {
			throw new IllegalArgumentException("Incorrect class id " + classId + " for Naive Bayes Classifier");
		}
		
		return _classes[classId]._classProb;
	}
	
	public void discretize(double min, double max) {
		_step = (int)((max-min+EPS)/_classes.length);
	}
	
	
	
	public void add(double[] data) {
		if( data == null || data.length ==0) {
			throw new IllegalArgumentException("Cannot add undefined data as input to Naive Bayes");
		}
		
			/*
			 * Format the dynamic array to hold observed data
			 */

		int classId = (int)(data[0]/_step);
		for(int k = 0; k < data.length; k++) {
			_valuesArray[classId].add(data[k]);
		}
		_numObservations++;
	}
	
	
	/**
	 * <p>Train the multinomial Naive Bayesian model.
	 * @return R mean square error
	 * @throws ClassifierException if data is insufficient.
	 */
	public int train() throws ClassifierException {
		/*
		 * Computes the number of observations
		 */
		double[] values =  null;
		
		for( int j = 0; j < _valuesArray.length; j++) {
			values = _valuesArray[j].currentValues();
			if(values == null) {
				throw new ClassifierException("Cannot compute LS Regression on undefined observations");
			}

			/*
			 * Computes the mean value for the different model parameters.
			 */
			_classes[j].add(values);
		}
		
		for( int j = 0; j < _classes.length; j++) {
			_classes[j].computeStats();
		}
				
		return values.length;
	}
	
	

	
			/**
			 * <p>Generate the class id for a set of values or observations. This
			 * run-time classification method relies on the Multinomial Bayesian Network.</p> 
			 * @param values array of observations to classify against the model
			 * @return class id (or rank) for this set of observations.
			 */
	public int classify(double[] values) {
		if( values == null || values.length != getNumClassParameters()) {
			throw new IllegalArgumentException("Multinomial Bayes cannot classify improperly formatted data");
		}
		
			/*
			 * Compute the normalizing denominator value
			 */
		double[] normalizedPriorProb = new double[values.length];
		double prob = 0.0;
		
		for( int valueIndex = 0; valueIndex < values.length; valueIndex++) {
			for(int classid = 0; classid < _classes.length; classid++) { 
				prob = Math.abs(values[valueIndex] - _classes[classid]._parameters[valueIndex]);
				if( prob > normalizedPriorProb[valueIndex]) {
					normalizedPriorProb[valueIndex] = prob;
				}
			}
		}
		
		return maximumLikelihood(values, normalizedPriorProb);
	}
	


	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int k = 0; k < _classes.length; k++) {
			buf.append("; Class id=");
			buf.append(k);
			buf.append("\n");
			buf.append(_classes[k].toString());
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	
	
								// ----------------------------
								//  Private Supporting Methods
								// ----------------------------
	
	private int maximumLikelihood(double[] values, double[] denominator) {
		double 	score 			= 0.0,
				adjustedValue 	= 0.0,
				priorProb 		= 0.0,
				bestScore		= -Double.MAX_VALUE;
		int bestMatchedClass = -1;
		
		/*
		 * Walks through all the class defined in the model
		 */
		for(int classid = 0; classid < _classes.length; classid++) { 
			double[] classParameters = _classes[classid]._parameters;
			score = 0.0;
			
			/*
			 * Compute the likelihood for each value and use the
			 * Naive Bayes formula for log of prior probability and log
			 * of class probability
			 */
			for( int k = 0; k < values.length; k++) {
				adjustedValue = _kF.estimate(values[k]);
				priorProb = Math.abs(adjustedValue -classParameters[k])/denominator[k];
				score += Math.log(1.0 - priorProb);
			}
			
			score += Math.log(_classes[classid]._classProb);
			
			if(score > bestScore) {
				bestScore = score;
				bestMatchedClass = classid;
			}
		}
		
		return bestMatchedClass;
	}
	

}

// -----------------------------  EOF --------------------------------------

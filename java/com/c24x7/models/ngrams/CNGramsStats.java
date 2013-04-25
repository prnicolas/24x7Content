// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.ngrams;

import com.c24x7.util.CEnv;


		/**
		 * <p>Class used to collect and process statistics for the N-Grams
		 * Frequency classifier.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/11/20102
		 */
public final class CNGramsStats {
	private String	_label 					= null;
	private boolean	_isNNP 					= false;
	private int		_nGramRank 				= CEnv.UNINITIALIZED_INT;
	private	double	_numNGramOccurrences 	= CEnv.UNINITIALIZED_INT;
	private double	_maxNumTermOccurrences 	= CEnv.UNINITIALIZED_INT;
	private	float	_idf 					= CEnv.UNINITIALIZED_FLOAT;

			/**
			 * <p>Create a N-Gram frequency statistics for a labeled N-Gram
			 * @param label  labeled N-Gram used for supervised training purpose
			 */
	public CNGramsStats(final String label) {
		_label = label;
	}

	
			/**
			 * <p>Initialize the number of N-Gram occurrences and the maximum number
			 * of occurrences of any term of the N-Gram within a document
			 * @param numNGramOccurrences number of N-Gram occurrences in a document
			 * @param maxNumTermOccurrences maximum number of occurrences of any term of the N-Gram within a document
			 */
	public void setNumOccurrences(double numNGramOccurrences, double maxNumTermOccurrences) {
		_numNGramOccurrences = numNGramOccurrences;
		_maxNumTermOccurrences = maxNumTermOccurrences;
	}
	
	
	public final String getLabel() {
		return _label;
	}
	
	public void setIdf(float idf) {
		_idf = idf;
	}
	
	public void setNGramRank(int nGramRank) {
		_nGramRank = nGramRank;
	}
	
	public int getNGramRank() {
		return _nGramRank;
	}
	
	public void setIsNNP(boolean isNNP) {
		_isNNP = isNNP;
	}
	
	public boolean isCompound() {
		return (_label.split(" ").length > 1);
	}
	

	
	public boolean isLabel(final String label) {
		if( label == null) {
			throw new IllegalArgumentException("Undefined label in NGrams order statistics");
		}
		
		return (_label.compareTo(label) ==0);
	}
	
	public float getIdf() {
		return _idf;
	}
	

	
	public double getNumNGramOccurrences() {
		return _numNGramOccurrences;
	}
	
	public double getMaxNumTermOccurrences() {
		return _maxNumTermOccurrences;
	}
	
	public boolean isNNP() {
		return _isNNP;
	}
}

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.ngrams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;



			/**
			 * <p>Defines the model for selecting the most semantically relevant
			 * N-GRAM from a document. The model features are<br>
			 * - Tag estimator: probability a semantically relevant N-GRAM to contains a proper noun (tag = NNP(s))<br>
			 * - Maximum Term Frequency estimator: probability of a semantically relevant N-GRAM with the maximum relative frequency of any of its term to have a specific value.<br>
			 * - Minimum NGram Frequency: Minimum relative frequency the least semantically relevant N-GRAM may have<br>
			 * - Maximum NGram Frequency: Maximum relative frequency the most semantically relevant N-GRAM may have.<br>
			 * The model is created through the N-Grams Frequency classifier. This class is implemented
			 * as a Singleton.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/13/2012
			 */

public final class CNGramsModel {
	private final static String TAG_ESTIMATOR 		 	= "tagEstimator";
	private final static String MAX_TERM_FREQ_ESTIMATOR = "maxTermFreqEstimator";
	private final static String MIN_NGRAM_FREQ 			= "minNGramFreq";
	private final static String MAX_NGRAM_FREQ 			= "maxNGramFreq";
	private final static String MODEL_FILE 				= CEnv.modelsDir + "semantic/ngrams_freq_model";
	
	private float _tagEstimator 		= CEnv.UNINITIALIZED_FLOAT;
	private float _maxTermFreqEstimator = CEnv.UNINITIALIZED_FLOAT;
	private float _minNGramFreq 		= CEnv.UNINITIALIZED_FLOAT;
	private float _maxNGramFreq 		= CEnv.UNINITIALIZED_FLOAT;
	
	private static CNGramsModel instance = null;
	
	
		/**
		 * <p>Method to initialize the N-Grams frequency model, loaded as launch time.
		 * This method is called by the initialization routine, CEnv.init()</p>
		 *  
		 * @see com.c24x7.util.CEnv.init()
		 * @return true if initialize of the model succeeds, false otherwise
		 */
	
	public static boolean init() {
		instance = new CNGramsModel();
		try {
			if( !instance.loadModel() ) {
				throw new IOException("Improper NGrams Frequency Model Format");
			}
		}
		catch (NumberFormatException e) {
			CLogger.error("Cannot initialize N-Grams frequency model " + e.toString());
			instance = null;
		}
		catch(IOException e) {
			CLogger.error("Cannot initialize N-Grams frequency model " + e.toString());
			instance = null;
		}
		
		return (instance != null);
	}
	
	
		/**
		 * <p>Method to retrieve the N-Grams Frequency Model singleton</p>
		 * @return singleton instance if initialization was successful, null otherwise
		 */
	public static CNGramsModel getInstance() {
		return instance;
	}
	

		/**
		 * <p>Initialize the tag estimator for the  N-Grams Frequency Model. This method is
		 * called by the N-Grams Frequency Model classifier.</p>
		 * @param tagEstimator new value for the tagEstimator parameter
		 */
	protected void setTagEstimator(float tagEstimator) {
		_tagEstimator = tagEstimator;
	}
	
		/**
		 * <p>Retrieve tag estimator for this N-Grams Frequency Model.</p>
		 * @return  tag estimator
		 */
	public float getTagEstimator() {
		return _tagEstimator;
	}
	
		/**
		 * <p>Initialize the estimation of the maximum relative frequency for any of 
		 * the terms frequency contained in the N-Gram.</p>. 
		 * This method is called by the N-Grams Frequency Model classifier.</p>
		 * @param maxTermFreqEstimator new value for the maximum terms frequency estimator.
		 */
	protected void setMaxTermFreqEstimator(float maxTermFreqEstimator) {
		_maxTermFreqEstimator = maxTermFreqEstimator;
	}
	
		/**
		 * <p>Retrieve the estimated value (or probability) of the maximum relative frequency 
		 * for any of the terms frequency contained in the N-Gram.</p>
		 * @return estimation of the maximum relative frequency  for any of the terms frequency contained in the N-Gram
		 */
	public float getMaxTermFreqEstimator() {
		return _maxTermFreqEstimator;
	}
	
		/**
		 * <p>Initialize the minimum allowed NGram frequency for this  
		 * N-Grams Frequency Model.  This method is called by the N-Grams 
		 * Frequency Model classifier.</p>
		 * @param minNGramFreq new value for the minimum allowed NGram frequency
		 */
	protected void setMinNGramFreq(float minNGramFreq) {
		_minNGramFreq = minNGramFreq;
	}
	
		/**
		 * <p>Retrieve the minimum allowed NGram frequency for this  
		 * N-Grams Frequency Model.</p>
		 * @return  minimum allowed NGram frequency
		 */
	public float getMinNGramFreq() {
		return _minNGramFreq;
	}
	
		/**
		 * <p>Initialize the maximum allowed NGram frequency for this  
		 * N-Grams Frequency Model.  This method is called by the N-Grams 
		 * Frequency Model classifier.</p>
		 * @param maxNGramFreq new value for the maximum allowed NGram frequency
		 */
	protected void setMaxNGramFreq(float maxNGramFreq) {
		_maxNGramFreq = maxNGramFreq;
	}
	
		/**
		 * <p>Retrieve the maximum allowed NGram frequency for this  
		 * N-Grams Frequency Model.</p>
		 * @return  maximum allowed NGram frequency
		 */
	public float getMaxNGramFreq() {
		return _maxNGramFreq;
	}
	
	

						// --------------------------
						//  Private Supporting Methods
						// ---------------------------
	
	private CNGramsModel() {	}
	
	
	private boolean loadModel() throws IOException, NumberFormatException {
		Map<String, String> map = new HashMap<String, String>();
		CFileUtil.readKeysValues(MODEL_FILE, CEnv.KEY_VALUE_DELIM, map);
		String value = map.get(TAG_ESTIMATOR);
		if( value == null) {
			return false;
		}
		_tagEstimator = Float.parseFloat(value);
		
		value = map.get(MAX_TERM_FREQ_ESTIMATOR);
		if( value == null) {
			return false;
		}
		_maxTermFreqEstimator = Float.parseFloat(value);
		
		value = map.get(MIN_NGRAM_FREQ);
		if( value == null) {
			return false;
		}
		_minNGramFreq = Float.parseFloat(value);
		
		value = map.get(MAX_NGRAM_FREQ);
		if( value == null) {
			return false;
		}
		_maxNGramFreq = Float.parseFloat(value);
		
		return true;
	}
}

// ----------------------  EOF ---------------------------------------
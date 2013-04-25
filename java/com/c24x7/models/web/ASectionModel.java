// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.c24x7.webanalyzer.CWebDocument;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.CIntMap;
import com.c24x7.util.logs.CLogger;



			/**
			 * <p>Base class to detect and classify paragraph using a Naive
			 * Bayesian model. The subclasses implement a model dedicated to 
			 * the extraction of copyright, references or other specialized section in
			 * a published document.<br>
			 * The model parameters are <ul>
			 * <li>Relative number of special patterns characters</li>
			 * <li>Size of the paragraph</li>
			 * <li>Threshold for the Bayes probability</li>
			 * <li>The most frequent keywords used in this specialized paragraph.</li>
			 * </ul>
			 * </p>
			 * @author Patrick Nicolas
			 * @date 11/17/2011
			 */
public abstract class ASectionModel {
	
	protected float _numRefPatterns	= 0.0F;
	protected float _threshold 		= 0.75F;
	protected float	_size			= 0.0F;
	protected float _numKeywords    = 0.0F;
	protected int   _totalWeight    = 0;
	
				/**
				 * <p>Generic method to train a Naive Bayes model and generate
				 * the parameters for the model.</p>
				 * @throws IOException if training files are not available.
				 */
	protected int train() throws IOException {
		BufferedReader reader = null;
		int trainingSetCounter = 0;
		
		try {
			initialize();
			int numRefPatterns = 0;
			FileInputStream fis = new FileInputStream(getTrainingFile());
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			int recordSize = 0;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 8) {
					numRefPatterns = extractStats(line, numRefPatterns);
					trainingSetCounter++;
					recordSize += line.length();
				}
			}
					/*
					 * Update Bayesian Model parameters
					 */
			float fNumRefPatterns = numRefPatterns/(float)trainingSetCounter;
			StringBuilder results = new StringBuilder(String.valueOf(fNumRefPatterns));
			results.append("\n");
			results.append((float)recordSize/trainingSetCounter);
			results.append("\n");
			
			int totalWeight = 0;
			for(String keyword : getKeywordsMap().keySet()) {
				totalWeight += getKeywordsMap().get(keyword).intValue();
			}
			results.append(totalWeight);
			results.append("\n");
			
					/*
					 * Orders the keywords
					 */
			for(String keyword : getKeywordsMap().keySet()) {
				results.append(keyword);
				results.append(CEnv.KEY_VALUE_DELIM);
				results.append(getKeywordsMap().get(keyword).intValue());
				results.append("\n");
			}
			
			CFileUtil.write(getModelFile(), results.toString());
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		return trainingSetCounter;
	}
	
	
	
	protected int validate() throws IOException {
		return 0;
	}
	
				/**
				 * <p>Implements the Bayes/Bernoulli classifier for any 
				 * paragraph of a document.</p>
				 * @param list of tokens extracted from this specific section of the web page
				 * @param contentLen length of the document.
				 * @return true if the paragraph match the class, false otherwise
				 */
	
	protected boolean classify(final List<String> tokens, int contentLen) {
		int numRefPatterns = 0;
		int keywordsWeight = 0;
		
		for( String token : tokens) {
			token = token.trim();
			if(token.length() > 0) {
				if(matchRegEx(token)) {
					numRefPatterns++;
				}
				token = removePunctuation(token);
				if(getKeywordsMap().containsKey(token)) {
					keywordsWeight += getKeywordsMap().get(token).intValue();
				}
			}
		}
			/*
			 * Use Manhattan distance abs(x-X)/X
			 */
		float prob = (numRefPatterns +1)/(_numRefPatterns+1.0F);
		prob *= Math.abs(contentLen - _size)/_size;
		
		prob *= ((float)keywordsWeight +1.0F)/(_totalWeight +1.0F);
		if( prob > 1.0F) {
			prob = 1.0F;
		}
		
		return (prob > _threshold);
	}
	
	/**
	 * <p>Update a document extraction statistics with a new section content</p>
	 * @param doc document extractor used for this analysis
	 * @param content sentence or paragraph of a section of a document
	 */
	protected void update(CWebDocument doc, final String content) { }
	
					// --------------------
					// Polymorphic methods
					// --------------------
	
	/**
	 * <p>Matches a token using this class regular expression.</p>
	 * @param token or term
	 * @return true if match succeeds, false otherwise.
	 */
	abstract protected boolean matchRegEx(final String token);
				
	/**
	 * <p>Retrieve the name of the file containing this model parameters.</p>
	 * @return the model file name.
	 */
	abstract protected String getModelFile();
	
	/**
	 * <p>Retrieve the name of the file containing the initial training set.</p>
	 * @return the name of the file containing the training set.
	 */
	abstract protected String getTrainingFile();
	
	/**
	 * <p>Retrieve the map of most frequent keywords used for this type of paragraph.</p>
	 * @return map of keywords commonly used in this type of paragraph.
	 */
	abstract protected CIntMap getKeywordsMap();
	
			/**
			 * <p>Initialize the terms table for this paragraph classifier.</p>
			 */
	abstract protected void initialize();
	
	protected ASectionModel() {
		try {
			load();
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
	}

				// ------------------------------
				// Private Supporting Methods
				// ---------------------------
	
	protected boolean load() throws IOException {
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(getModelFile());
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = reader.readLine();
			
			if( line == null) {
				return false;
			}
			line = line.trim();
			if( line.length() > 0) {
				_numRefPatterns = Float.parseFloat(line);
			}
		
			line = reader.readLine();
			if( line == null) {
				return false;
			}
			line = line.trim();
			if( line.length() > 0) {
				_size = Float.parseFloat(line);
			}
			line = reader.readLine();
			if( line == null) {
				return false;
			}
			line = line.trim();
			if( line.length() > 0) {
				_totalWeight = Integer.parseInt(line);
			}
			
			int indexDelim = -1;
			int value = 0;
			while ((line = reader.readLine()) != null) {
				indexDelim = line.trim().indexOf(CEnv.KEY_VALUE_DELIM);
				if( indexDelim != -1) {
					value = Integer.parseInt(line.substring(indexDelim+1));
					getKeywordsMap().put(line.substring(0, indexDelim), value);
				}
			}
			return (getKeywordsMap().size() > 0);
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
	}
	
	protected int extractStats(final String line, int numRefPatterns) {
		
		String[] tokens = line.split(" ");
		int localNumRefPatterns = numRefPatterns;
		
		for( String token : tokens) {
			token = token.trim();
			if(token.length() > 0) {
				if(matchRegEx(token)) {
					localNumRefPatterns++;
				}
					/*
					 * Make sure we do not extract a string which include
					 * a punctuation as last character..
					 */
				token = removePunctuation(token);
				if(getKeywordsMap().containsKey(token)) {
					getKeywordsMap().put(token);
				}
			}
		}
		return localNumRefPatterns;
	}

	
	protected static String removePunctuation(final String token) {
		String cleanedToken = token;
		if(token.length() > 2) {
			int lastChar = token.charAt(token.length()-1);
			if(lastChar == '.' || lastChar == ',' || lastChar == ':') {
				cleanedToken = token.substring(0, token.length()-1);
			}
		}
			
		return cleanedToken;
	}
}

// ----------------------------------  EOF ---------------------------------
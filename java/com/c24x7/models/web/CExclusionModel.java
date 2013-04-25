// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.web;

import com.c24x7.util.CEnv;
import com.c24x7.util.CIntMap;




		/**
		 * <p>Bayesian model to extract information or content non relevant to a
		 * document. This is a singleton pattern. The model support training and real-time classification of content.<br>
		 * The model parameters are <ul>
		 * <li>Relative number of special patterns characters</li>
		 * <li>Size of the paragraph</li>
		 * <li>Threshold for the Bayes probability</li>
		 * <li>The most frequent keywords used in this specialized paragraph.</li>
		 * </ul></p>
		 * @author Patrick Nicolas
		 * @date 11/18/2011
		 * @see CParagraphClassifier
		 */

public final class CExclusionModel extends ASectionModel {
	protected static final String TRAINING_FILE = CEnv.trainingDir + "sets/exclusion_train";
	protected static final String MODEL_FILE = CEnv.modelsDir + "semantic/exclusion_model";
	protected static final String regEx = "^([a-zA-Z].|:|-|;|,|]){1,10}$|[+^|{}#<>?(&amp;)]";
	
	protected static CIntMap keywordsMap = new CIntMap();

	
	public CExclusionModel() { 
		super();
	}
	
			/**
			 * <p>Initialize the terms table for this paragraph classifier.</p>
			 */
	@Override
	protected void initialize() {
		keywordsMap.put("Terms", 0);
		keywordsMap.put("Help", 0);
		keywordsMap.put("Contact", 0);
		keywordsMap.put("contact", 0);
		keywordsMap.put("Site", 0);
		keywordsMap.put("Privacy", 0);
		keywordsMap.put("Advertise", 0);
		keywordsMap.put("customer", 0);
		keywordsMap.put("Payment", 0);
		keywordsMap.put("Sitemap", 0);
		keywordsMap.put("Rights", 0);
		keywordsMap.put("Acceptable", 0);
		keywordsMap.put("Fair", 0);
		keywordsMap.put("Good", 0);
		keywordsMap.put("Questions", 0);
		keywordsMap.put("Paperback", 0);
		keywordsMap.put("Registration", 0);
		keywordsMap.put("Register", 0);
		keywordsMap.put("Toll", 0);
		keywordsMap.put("TDD/TTY", 0);
		keywordsMap.put("Deals", 0);
		keywordsMap.put("deals", 0);
		keywordsMap.put("scams", 0);
		keywordsMap.put("Free", 0);
		keywordsMap.put("FREE", 0);
		keywordsMap.put("browsers", 0);
		keywordsMap.put("javascript", 0);
		keywordsMap.put("Javascript", 0);
		keywordsMap.put("news", 0);
		keywordsMap.put("Reply", 0);
		keywordsMap.put("Amazon", 0);
		keywordsMap.put("[...]", 0);
	}

			/**
			 * <p>Matches a token using this class regular expression.</p>
			 * @param token or term
			 * @return true if match succeeds, false otherwise.
			 */
	@Override
	protected boolean matchRegEx(String token) {
		return token.matches(regEx);
	}

	/**
	 * <p>Retrieve the name of the file containing this model parameters.</p>
	 * @return the model file name.
	 */
	@Override
	protected String getModelFile() {
		return MODEL_FILE;
	}

	/**
	 * <p>Retrieve the name of the file containing the initial training set.</p>
	 * @return the name of the file containing the training set.
	 */
	@Override
	protected String getTrainingFile() {
		return TRAINING_FILE;
	}

	/**
	 * <p>Retrieve the map of most frequent keywords used for this type of paragraph.</p>
	 * @return map of keywords commonly used in this type of paragraph.
	 */
	@Override
	protected CIntMap getKeywordsMap() {
		return keywordsMap;
	}
}

// ------------------------------  EOF ---------------------------------------
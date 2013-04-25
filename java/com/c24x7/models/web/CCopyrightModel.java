// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.web;

import com.c24x7.util.CEnv;
import com.c24x7.util.CIntMap;
import com.c24x7.webanalyzer.CWebDocument;


			/**
			 * <p>Bayesian model to extract copyright information from a
			 * document. Implemented as a Singleton pattern. The model support training and real-time classification of content.
			 * <br>
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

public final class CCopyrightModel extends ASectionModel {
	protected static final String TRAINING_FILE = CEnv.trainingDir + "training/sets/copyright_train";
	protected static final String MODEL_FILE = CEnv.modelsDir + "semantic/copyright_model";
	protected static final String regEx = "^((19|20)\\d\\d)$|^(\\(19|20)\\d\\d\\)$|^([a-zA-Z].|:|-|;|,|]){1,10}$|[+^|©]|^((©20)\\d\\d)";
	
	protected static CIntMap keywordsMap = new CIntMap();

	public CCopyrightModel() {
		super();
	}

	
	/**
	 * <p>Update a document extraction statistics with the copyright information content</p>
	 * @param doc document extractor used for this analysis
	 * @param content sentence or paragraph of the copyright section of a document
	 */
	@Override
	protected void update(CWebDocument doc, final String content) {
		doc.setCopyright(content);
	}
	
			/**
			 * <p>Initialize the terms table for this paragraph classifier.</p>
			 */
	@Override
	protected void initialize() {
		keywordsMap.put("Copyright", 0);
		keywordsMap.put("Reserved", 0);
		keywordsMap.put("Rights", 0);
		keywordsMap.put("rights", 0);
		keywordsMap.put("reserved", 0);
		keywordsMap.put("All", 0);
		keywordsMap.put("Inc", 0);
		keywordsMap.put("prohibited", 0);
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

// -----------------------------  EOF ----------------------------------
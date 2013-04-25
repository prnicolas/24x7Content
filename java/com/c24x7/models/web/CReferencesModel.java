package com.c24x7.models.web;


import com.c24x7.util.CEnv;
import com.c24x7.util.CIntMap;
import com.c24x7.webanalyzer.CWebDocument;



			/**
			 * <p>Bayesian model to extract references and links from a published
			 * document. This is a Singleton pattern. The model support training and real-time classification of content.
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

public final class CReferencesModel extends ASectionModel {
	protected static final String TRAINING_FILE = CEnv.trainingDir + "set/references_train";
	protected static final String MODEL_FILE = CEnv.modelsDir + "semantic/references_model";
	protected static final String regEx = "^((19|20)\\d\\d)$|^(\\(19|20)\\d\\d\\)$|^([a-zA-Z].|:|-|;|,){1,10}$|^(http)|[+^|]";
	protected static CIntMap keywordsMap = new CIntMap();

	
	public CReferencesModel() {
		super();
	}
	
	/**
	 * <p>Update a document extraction statistics with a new reference content</p>
	 * @param doc document extractor used for this analysis
	 * @param content sentence or paragraph of a reference section of a document
	 */
	@Override
	protected void update(CWebDocument doc, final String content) {
		doc.addReferences(content);
	}
	
	
	
			/**
			 * <p>Initialize the terms table for this paragraph classifier.</p>
			 */
	@Override
	protected void initialize() {
		keywordsMap.put("ISBN", 0);
		keywordsMap.put("Vol", 0);
		keywordsMap.put("Journal", 0);
		keywordsMap.put("Wiley", 0);
		keywordsMap.put("Press", 0);
		keywordsMap.put("Addison-Wesley", 0);
		keywordsMap.put("Publishing", 0);
		keywordsMap.put("Retrieved", 0);
		keywordsMap.put("Harper", 0);
	}
	
	/**
	 * <p>Matches a token using this class regular expression.</p>
	 * @param token or term
	 * @return true if match succeeds, false otherwise.
	 */
	@Override
	protected boolean matchRegEx(final String token) {
		return token.matches(regEx);
	}
	
	/**
	 * <p>Retrieve the map of most frequent keywords used for this type of paragraph.</p>
	 * @return map of keywords commonly used in this type of paragraph.
	 */
	@Override
	protected CIntMap getKeywordsMap() {
		return keywordsMap;
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
}

// ----------------------- EOF ------------------------------------------
package com.c24x7.models.web;

import java.io.IOException;
import java.util.List;


import com.c24x7.textanalyzer.tokenizers.SCRegExpTokenizer;


			/**
			 * <p>Generic classifier for web content. This classifier is composed
			 * of several distinct and dedicated models to extract components of a web page.</p>
			 * @author Patrick Nicolas
			 * @date 12/02/2011
			 */
public final class CWebContentClassifier {
	protected ASectionModel[] _paragraphModels = null;
	
	
		/**
		 * <p>Create a classifier object that manage a component model such
		 * as copyright notice, reference section,.. on a web page.</p>
		 * @param model model or classifier specialized in a specific section of a web page
		 */
	public CWebContentClassifier(ASectionModel model) {
		_paragraphModels = new ASectionModel[] { model };
	}
	
	
			/**
			 * <p>Create a generic classifier object that manage all the components of
			 * a web page such as copyright notice, reference section,...</p>
			 * @param model model or classifier specialized in a specific section of a web page
			 */
	public CWebContentClassifier() {
		_paragraphModels = new ASectionModel[3];
		
		_paragraphModels[0] = new CExclusionModel();
		_paragraphModels[1] = new CReferencesModel();
		_paragraphModels[2] = new CCopyrightModel();
	}
	
	

	
		/**
		 * <p>Train a model from a sequence of files.</p>
		 * @return number of records used in the training (return 0 if the training phase failed).
		 * @throws IOException if training files are not available or corrupted.
		 */
	public int train()throws IOException {
		int trainingSize = 0;
		
		if( _paragraphModels.length > 0) {
			for(ASectionModel model :  _paragraphModels) {
				trainingSize += model.train();
			}
		}
		return trainingSize;
	}
	
		/**
		 * <p>Validate a model from a sequence of files.</p>
		 * @return number of records used in the validation process
		 * @throws IOException if training files are not available or corrupted.
		 */
	public int validate() throws IOException {
		int validationSize = 0;
		
		if( _paragraphModels.length > 0) {
			for(ASectionModel model :  _paragraphModels) {
				validationSize += model.validate();
			}
		}
		return validationSize;
	}
	
		/**
		 * <p>Classify a document.</p>
		 * @return number of documents classified.
		 */
	public boolean classify(final String content) {
		boolean completed = false;
		
		List<String> tokensList = SCRegExpTokenizer.getInstance().tokenize(content);

		if( _paragraphModels.length > 0) {
			for(ASectionModel model :  _paragraphModels) {
				completed = model.classify(tokensList, content.length());
				if( !completed) {
					break;
				}
			}
		}
	
		return completed;
	}
}

// ------------------------- EOF --------------------------------

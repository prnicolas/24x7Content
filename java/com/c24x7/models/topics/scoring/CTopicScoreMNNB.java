/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics.scoring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.ClassifierException;
import com.c24x7.math.bayes.CMNNaiveBayes;
import com.c24x7.models.topics.CTopicClassifier;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;


		/**
		 * <p>Topic scoring model that relies on the Multinomial Naive Bayes Classifier.</p>
		 * 
		 * @see com.c24x7.math.bayes.CMNNaiveBayes
		 * @author Patrick Nicolas         24x7c 
		 * @date June 29, 2012 9:58:36 AM
		 */
public final class CTopicScoreMNNB extends ATopicScore {
	public static final int NUM_CLASSES  = 3;
	
	private static final String CLASS_PROBABILITY	= "ClassProbabilityP";
	private static final String TOPICS_MODEL_FILE 	= CEnv.modelsDir + "semantic/topic_model_mmnaivebayes";
	public static final String LABEL 				= "Multinomial Naive Bayes";
	
	private CMNNaiveBayes _classifier = null;
	
		/**
		 * <p>Create a topic scoring instance using the Multinomial Naive Bayes
		 * classifier for NUM_CLASSES classes and NUM_FEATURES parameters.
		 */
	public CTopicScoreMNNB() {
		_classifier = new CMNNaiveBayes(CTopicClassifier.SIZE_LABELED_OBSERVATION-1, NUM_CLASSES);
	}
	
	@Override
	public String getType() {
		return LABEL;
	}

	@Override
	public void addData(double[] data) {
		_classifier.add(data);
	}

	/**
	 * <p>Train this model using Multinomial Naive Bayes.</p>
	 * @throws ClassifierException if the model computation fails or the model cannot be stored in file.
	 */
	@Override
	public int train() throws ClassifierException {
		int numTrainingSamples = _classifier.train();
		try {
			writeModel();
		}
		catch( IOException e) {
			throw new ClassifierException("Cannot train Bayesian Topic classifier. " + e.toString());
		}
		
		return numTrainingSamples;
	}
	
	
	@Override
	public void loadModel() throws IOException {
		Map<String, String> parametersMap = new HashMap<String, String>();
		CFileUtil.readKeysValues(TOPICS_MODEL_FILE, parametersMap);
			
		for( int k = 0; k < CTopicClassifier.SIZE_LABELED_OBSERVATION-1; k++) {
			if( parametersMap.containsKey(LABELS[k+1]) ) {
				loadModelFeature(k, parametersMap.get(LABELS[k+1]));
			}
			else {
				throw new IOException("Cannot load NB topic scoring features");
			}
		}
		loadClassProb(parametersMap.get(CLASS_PROBABILITY));
	}
	

	/**
	 * <p>Compute the score of the set of values against the model
	 * features defined in the classifier.</p>
	 * @param set of values or observations
	 * @return score of for this set of observations.
	 */
	@Override
	public double score(double[] values) {
		return (double)_classifier.classify(values);
	}
	
	@Override
	public String toString() {
		return _classifier.toString();
	}

							// ----------------------------
							//  Private Supporting Methods
							// ----------------------------

	private void loadModelFeature(int paramIndex, final String classValuesStr) throws IOException {
		String[] classValues = classValuesStr.split(CEnv.FIELD_DELIM);
		
		if( classValues.length != NUM_CLASSES) {
			throw new IOException("Incorrect format for Multinomial Bayesian Topic Score Model");
		}
		for(int k = 0; k < classValues.length; k++) {
			_classifier.setClassParameter(k, paramIndex, Float.parseFloat(classValues[k]));
		}
	}
	
	
	private void loadClassProb(final String classValuesStr) throws IOException { 
		String[] classValues = classValuesStr.split(CEnv.FIELD_DELIM);
		for(int k = 0; k < classValues.length; k++) {
			_classifier.setClassProb(k, Double.parseDouble(classValues[k]));
		}
	}

		/**
		 * <p>Write the model features into a file.</p>
		 */
	private void writeModel() throws IOException {
		StringBuilder buf = new StringBuilder("; -----------");
		buf.append(LABEL);
		buf.append("\n;");
		
		int lastClassIndex = _classifier.getNumClasses()-1;
		
		/*
		 * Write the model features prior probabilities into the model file
		 */
		for(int j = 1; j < CTopicClassifier.SIZE_LABELED_OBSERVATION; j++) {
			buf.append("\n");
			buf.append(LABELS[j]);
			buf.append(CEnv.KEY_VALUE_DELIM);
			
			for( int k = 0; k < lastClassIndex; k++) {
				buf.append(_classifier.getClassParameter(k, j-1));
				buf.append(CEnv.FIELD_DELIM);
			}
			buf.append(_classifier.getClassParameter(lastClassIndex, j-1));
			
			buf.append("\n;StdDev:");
			for( int k = 0; k < lastClassIndex; k++) {
				buf.append(_classifier.getClassParameterVar(k, j-1));
				buf.append(CEnv.FIELD_DELIM);
			}
			buf.append(_classifier.getClassParameterVar(lastClassIndex, j-1));
			buf.append("\n;");
		}
		
			/*
			 * Write the class probability into the model file
			 */
		buf.append("\n");
		buf.append(CLASS_PROBABILITY);
		buf.append(CEnv.KEY_VALUE_DELIM);
		for( int k = 0; k < lastClassIndex; k++) {
			buf.append(_classifier.getClassProb(k));
			buf.append(CEnv.FIELD_DELIM);
		}
		buf.append(_classifier.getClassProb(lastClassIndex));
		
		CFileUtil.write(TOPICS_MODEL_FILE, buf.toString());
	}
}

// --------------------------  EOF ---------------------------------------------

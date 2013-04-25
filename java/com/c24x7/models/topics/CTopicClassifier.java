/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.c24x7.models.topics.scoring.ATopicScore;
import com.c24x7.models.topics.scoring.CTopicScoreCluster;
import com.c24x7.exception.ClassifierException;
import com.c24x7.exception.InitException;
import com.c24x7.topics.CTopicsExtractor.NFeaturesSet;
import com.c24x7.util.CDoubleMap;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;


		/**
		 * <p>Classifier for topics extracted from taxonomy lineages (or
		 * classes hierarchy) extracted from semantically valid N-Grams. The
		 * classifier relies on Least Square Regression to model its features.
		 * The features for the topic classifier are:<br>
		 * Weight of the taxonomy class candidate as key topic of the document<br>
		 * Variance of the distribution of sentences or paragraphs per taxonomy class candidate<br>
		 * Residual value</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 05/23/2012
		 */
public final class CTopicClassifier {
	private static final String TOPICS_TRAINING_SETS_FILE 		= CEnv.trainingDir + "sets/topics_training_labels";
	private static final String TOPICS_VALIDATION_RESULTS_FILE 	= CEnv.trainingDir + "validation/topics_validation_results";
	
	private static final double WORST_SCORE				= 0.0;
	private static final double BEST_SCORE				= 2.0;
	public static final int SIZE_LABELED_OBSERVATION	= 4;
	public static final int MIN_NUM_TRAINING_SAMPLES 	= 260;

	private static CTopicClassifier instance = null;
	
	
		/**
		 * <p>Initialize the parameters for the topic classifier.</p>
		 * @throws InitException if the model file cannot be loaded or the model parameters are improperly formatted.
		 */
	public static void init() throws InitException {
		init(new CTopicScoreCluster());
	}
	
		/**
		 * <p>Initialize the parameters for the topic classifier.</p>
		 * @throws InitException if the model file cannot be loaded or the model parameters are improperly formatted.
		 */
	public static void init(ATopicScore topicScore) throws InitException {
		try {
			instance = new CTopicClassifier();
			instance.setScoreMethod(topicScore);
			topicScore.loadModel();
		}
		catch( IOException e) {
			throw new InitException("Cannot initialize Topic Classifier " + e.toString());
		}
	}
	
		/**
		 * <p>Create a topic classifier to train a model using the training set file.</p>
		 */
	public static CTopicClassifier getInstance() {
		if( instance == null ) {
			instance = new CTopicClassifier();
		}
		return instance;
	}
	
	
		/**
		 * <p>Initialize the scoring classifier for selecting topic
		 * among taxonomy classes.</p>
		 * @param topicScore topic score model used in the classification of topics.
		 */
	public void setScoreMethod(ATopicScore topicScore) {
		_topicScore = topicScore;
	}
	

		/**
		 * <p>Train the topic model/classifier using the training sets
		 * generated by the CTopicLabelsGeneration class.</p>
		 * 
		 * @return number of training samples processed during the training phase
		 * @throws ClassifierException if training fails or model file was unwritable.
		 */
	public int train() throws ClassifierException {
		if( _topicScore == null) {
			throw new ClassifierException("Topic scoring method is undefined");
		}
		
		int numRecords = 0;
		try {
			/*
			 * Extract the different records from the training set file
			 */
			List<String> fields = new ArrayList<String>();
			CFileUtil.readEntries(TOPICS_TRAINING_SETS_FILE, null, fields);
			
			double[] data = new double[SIZE_LABELED_OBSERVATION];			
			for( String field : fields) {
				/*
				 * Extract the score value for this field
				 */
				StringBuilder fieldBuf = new StringBuilder(field);
				double scoreValue = extractLabelScore(fieldBuf);
					
				/*
				 * Extract the parameters values from
				 * each training record or observation.. 
				 */
				String[] parameters = fieldBuf.toString().split(CEnv.FIELD_DELIM);
									
				try {
					data[0]= scoreValue;						// Score value
					for(int k = 0; k < SIZE_LABELED_OBSERVATION-1; k++) {
						data[k+1] = Double.valueOf(parameters[k]);	
					}
					
					_topicScore.addData(data);
					numRecords++;
				}
				catch( NumberFormatException e) {
					throw new ClassifierException("Cannot extract Topics training data " + e.toString());
				}
			}
			
			/*
			 * We need a minimum set of training records for building
			 * the generative model.
			 */
			if( numRecords < MIN_NUM_TRAINING_SAMPLES) {
				throw new ClassifierException("Training set for topics classifier is too small");
			}
			
			_topicScore.train();
		}
		catch( FileNotFoundException e) {
			throw new ClassifierException("Training sets not found " + e.toString());
		}
		catch( IOException e) {
			throw new ClassifierException("Improper tagged training set " + e.toString());
		}
		catch( Exception e) {
			e.printStackTrace();
			throw new ClassifierException("Improper format " + e.toString());
		}
			
		return numRecords;
	}
	
		
	
	/**
	 * <p>Validate the Topics classifier against a set of labeled
	 * topics records extracted from Wikipedia Reference database. The results
	 * of the validation is dumped into a results file 'topics_validation_results'</p>
	 * @return number of records used in the validation of the topics classifier
	 * @throws ClassifierException thrown if the classifier is not initialized or the validation files are not accessible.
	 */
	public int validate() throws ClassifierException {
		return validate(TOPICS_TRAINING_SETS_FILE, TOPICS_VALIDATION_RESULTS_FILE);
	}
	
	
			
	/**
	 * <p>Validate the Topics classifier against a set of labeled
	 * topics records extracted from Wikipedia Reference database. The results
	 * of the validation is dumped into a results file 'topics_validation_results'</p>
	 * @param trainingFile name of the file that contains the training set.
	 * @param resultFile name of the file that contains the results of the validation run.
	 * @return number of records used in the validation of the topics classifier
	 * @throws ClassifierException thrown if the classifier is not initialized or the validation files are not accessible.
	*/
	public int validate(final String trainingFile, 
						final String resultFile) throws ClassifierException {

		int counter 	 = 0,
			errorCounter = 0;
		
		try {
			List<String> fields = new ArrayList<String>();
			CFileUtil.readEntries(trainingFile, null, fields);
			
			StringBuilder buf = new StringBuilder();
			double leastSquareError = 0.0;

			String[] parameters = null;
			double[] values = null;
			
			for( String field : fields) {
				/*
				 * Extract the score value for this field
				 */
				StringBuilder fieldBuf = new StringBuilder(field);
				double labeledScore = extractLabelScore(fieldBuf);
				parameters = fieldBuf.toString().split(CEnv.FIELD_DELIM);
				
				/*
				 * collect the score for each training sets.
				 */
				try {
					values = new double[SIZE_LABELED_OBSERVATION-1];
					for( int k = 0; k < values.length; k++) {
						values[k] = Float.parseFloat(parameters[k]);
					}
					double score = _topicScore.score(values);
					
					buf.append("Score:");
					buf.append(score);
					buf.append(" Expected Score:");
					buf.append(labeledScore);
					buf.append(" Status:");
					
					/*
					 * Compare the actual score with the label.
					 */
					if( compareScoreWithLabel(score, labeledScore)) {
						buf.append("OK\n");
					}
					else {
						errorCounter++;
						buf.append("error\n");
					}						
					counter++;
				}
				catch( NumberFormatException e) {
					throw new ClassifierException("Cannot extract Topics training data " + e.toString());
				}
			}
			leastSquareError /= counter;
			
			StringBuilder resultsBuf = new StringBuilder("; Topic Classifier Validation Results with ");
			resultsBuf.append(_topicScore.getType());
			resultsBuf.append("\n% Failures :     ");
			
			String floatingPointDisplay = String.valueOf((double)errorCounter*100/counter);
			if(floatingPointDisplay.length() > 4) {
				floatingPointDisplay = floatingPointDisplay.substring(0,4);
			}
			resultsBuf.append(floatingPointDisplay);
			resultsBuf.append("\n -------------------------------------- \n");
			resultsBuf.append(buf.toString());
			
			CFileUtil.write(resultFile, resultsBuf.toString());
		}
		catch( IOException e) {
			throw new ClassifierException("Improper tagged training set");
		}
			
		return counter;
	}
	
	
		/**
		 * <p>Classify the map of features previously extracted from a document.</p>
		 * @param topicFeaturesMap map of features relevant to a topic.
		 * @return a set of taxonomy class names
		 */
	public Set<String> classify(final Map<String, NFeaturesSet> topicFeaturesMap) {
		CDoubleMap doubleMap = new CDoubleMap();
		double score = 0.0;
		
		/*
		 * rank the list of topic by scoring the values
		 * of its model features.
		 */
		NFeaturesSet featuresSet = null;
		for( String topicLabel : topicFeaturesMap.keySet()) {
			featuresSet = topicFeaturesMap.get(topicLabel);
			score = _topicScore.score(featuresSet.values());
			if(score == BEST_SCORE) {
				doubleMap.put(topicLabel, score);
			}
		}
		
		Set<String> orderedTopics = doubleMap.order();
		
		return orderedTopics;
	}
	
	
	
						// --------------------------
						//  Private Supporting Methods
						// ----------------------------
	
	private ATopicScore _topicScore = null;

	private CTopicClassifier() { }
		
	private static boolean compareScoreWithLabel(final double score, final double labeledScore) {
		return ((score == BEST_SCORE && labeledScore == BEST_SCORE) || (score < BEST_SCORE && labeledScore < BEST_SCORE));
	}

	
	private double extractLabelScore(StringBuilder field) {
		double scoreValue = WORST_SCORE;
		
		/*
		 *  Extract the overall score for the labeled data.
		 */
		int indexMarker = field.indexOf("|");
		if(indexMarker != -1) {
			String scoreStr = field.substring(indexMarker+1);
			scoreValue = Double.valueOf(String.valueOf(scoreStr.trim()));
			field.delete(indexMarker, field.length());
		}
		
		return scoreValue;
	}

}

// --------------------------  EOF --------------------------------
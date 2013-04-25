// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;



import com.c24x7.exception.ClassifierException;
import com.c24x7.exception.InitException;
import com.c24x7.models.topics.CTopicClassifier;
import com.c24x7.models.topics.scoring.CTopicScoreCluster;
import com.c24x7.models.topics.scoring.CTopicScoreMNNB;
import com.c24x7.models.topics.CTopicLabelsGeneration;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Command line application to extract a training set for topics model
		 * using Wikipedia reference database and use it to train the model.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 05/21/12
		 */
public final class CTopicTrainApp {

	
		/**
		 * <p>Main method to either extract a training set for the topics
		 * model or train the topics model. The argument '-create' should 
		 * be used to extract a training set from the Wikipedia reference
		 * database. The argument '-train' is used to train the topics
		 * model and compute the model parameters.</p>
		 * @see com.c24x7.models.topics.CTopicModel
		 * @param args argument to specify the operation to be executed
		 */
	public static void main(String[] args) {
		
		if(args.length > 0) {
			if( args[0].compareTo("-createLabels")==0) {
				createLabels();
			}
			else if(args[0].compareTo("-train")==0) {
				train();
			}
			else if(args[0].compareTo("-validate")==0) {
				validate();
			}
			else if(args[0].compareTo("-cluster")==0) {
				cluster();
			}
			else {
				incorrectArguments();
			}
		}
		else {
			incorrectArguments();
		}	
	}
	
	
	
	private static void createLabels() {
		try {
			CEnv.init();
			CLogger.setLoggerInfo(CLogger.TOPIC_TRAIN_TRACE);
			CTopicLabelsGeneration trainingGenerator = new CTopicLabelsGeneration();
			int numSamples = trainingGenerator.createRawLabels(396000, 619500);
			CLogger.info("Completed raw training set with " + numSamples + " samples",CLogger.TOPIC_TRAIN_TRACE);
		
			numSamples = trainingGenerator.createLabels();
			System.out.println("Completed labeled training set with " + numSamples + " samples");
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
		catch( ClassifierException e) {
			CLogger.error(e.toString());
		}
	}
	
	
	private static void cluster() {
		try {
			CTopicClassifier.getInstance().setScoreMethod(new CTopicScoreCluster());
			CTopicClassifier.getInstance().train();
		}
		catch( ClassifierException e) {
			CLogger.error(e.toString());
		}
	}
	
	
	private static void train() {
		try {
			CLogger.setLoggerInfo(CLogger.TOPIC_TRAIN_TRACE);
			CTopicClassifier.getInstance().setScoreMethod(new CTopicScoreMNNB());
			int numTrainingRecords = CTopicClassifier.getInstance().train();
			CLogger.info("\nTopic training completed after " + numTrainingRecords + " samples", CLogger.TOPIC_TRAIN_TRACE);
		}
		catch( ClassifierException e) {
			CLogger.error(e.toString());
		}
	}
	
	


	
	private static void validate() {	
		try {
			CTopicClassifier.init(new CTopicScoreCluster());
			int numValidationRecords = CTopicClassifier.getInstance().validate();
			System.out.println("Topic model validation completed after " + numValidationRecords + " samples");
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
		catch( ClassifierException e) {
			CLogger.error(e.toString());
		}
	}
	
	private static void incorrectArguments() {
		CLogger.error("CTopicTrainApp [-createTrainingSet/train/validate]\n -createTrainingSet: Generate training sample\n -train    : Train the topics model\n -validate    :Validate the topics model");
	}

}

// ------------------------------------  EIF ---------------------------

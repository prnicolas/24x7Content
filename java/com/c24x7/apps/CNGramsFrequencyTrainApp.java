// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;


import java.sql.SQLException;
import java.util.List;

import com.c24x7.exception.InitException;
import com.c24x7.models.ngrams.CNGramsClassifier;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.textanalyzer.tfidf.CTfIdfScore;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;

		/**
		 * <p>Application to train a model to extract NGrams from a documents.</p>
		 * @author Patrick Nicolas
		 * @date 01/12/2012 
		 */
public final class CNGramsFrequencyTrainApp {
	public static int startIndex = 41000;
	public static int endIndex 	=  42000;
	
	public static void main(String[] args) {
		
		if( args.length > 0 && args[0] != null) {
			if( args[0].compareTo("-train")==0) {
				if(args.length > 2) {
					startIndex = Integer.parseInt(args[1]);
					endIndex = Integer.parseInt(args[2]);
				}
				train();
			}
			else if( args[0].compareTo("-regress")==0) {
				if(args.length > 2) {
					startIndex = Integer.parseInt(args[1]);
					endIndex = Integer.parseInt(args[2]);
				}
				regressTfDataTransform(8);
			}
			
			else if( args[0].compareTo("-clusters")==0) {
				if(args.length > 2) {
					startIndex = Integer.parseInt(args[1]);
					endIndex = Integer.parseInt(args[2]);
				}
				clusters(4);
			}
		}
		else {
			CLogger.error("Incorrect command line for NGramsTag Training Application");
		}
	}	
	
	
	
							// --------------------------
							//  Private Supporting Methods
							// ---------------------------
	
	private static void clusters(int numClusters) {
		try {
			CEnv.init();
			float[] tagEstimatorValues = new float[10];
			float[] maxTermFreqEstimatorValues = new float[10];
					
			generateEstimatorValues(tagEstimatorValues, maxTermFreqEstimatorValues);
			
			//INFO
			System.out.println("Start clustering with linear discriminant");
			CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfLinearDiscriminant());
			executeClustering(tagEstimatorValues, maxTermFreqEstimatorValues, numClusters);
			
			//INFO
			System.out.println("Start clustering with square root discriminant");
			CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfSqrtDiscriminant());
			executeClustering(tagEstimatorValues, maxTermFreqEstimatorValues, numClusters);
			
			//INFO
			System.out.println("Start clustering with logarithmic discriminant");
			CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfLogDiscriminant());
			executeClustering(tagEstimatorValues, maxTermFreqEstimatorValues, numClusters);
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
	}
	
	
	/*
	private static void clusters(int[] numClusters) {
		if( CEnv.init() ) {
			float[] tagEstimatorValues = new float[10];
			float[] maxTermFreqEstimatorValues = new float[10];
					
			generateEstimatorValues(tagEstimatorValues, maxTermFreqEstimatorValues);
			for( int numCluster : numClusters) {
				executeClustering(tagEstimatorValues, maxTermFreqEstimatorValues, numCluster);
			}
		}
	}
	*/
	
		
	private static void regressTfDataTransform(int numTests) {
		try {
			CEnv.init();
			float[] tagEstimatorValues = new float[numTests];
			float[] maxTermFreqEstimatorValues = new float[numTests];
					
			generateEstimatorValues(tagEstimatorValues, maxTermFreqEstimatorValues);

			try {
				CNGramsClassifier nGramClassifier = new CNGramsClassifier(startIndex, endIndex);
				CDbpediaSql query = CDbpediaSql.getInstance();
				List<String[]> fieldsList = query.execute(startIndex, endIndex - startIndex);

				
				System.out.println("Start regression with Linear discriminant");
				CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfLinearDiscriminant());
				nGramClassifier.regress(tagEstimatorValues, maxTermFreqEstimatorValues, fieldsList);
				
				System.out.println("Start regression with Sqrt discriminant");
				CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfSqrtDiscriminant());
				nGramClassifier.regress(tagEstimatorValues,maxTermFreqEstimatorValues, fieldsList);
				
				System.out.println("Start regression with logarithmic discriminant");
				CTfIdfScore.getInstance().setDiscriminant(new CTfIdfScore.NTfLogDiscriminant());
				nGramClassifier.regress(tagEstimatorValues,maxTermFreqEstimatorValues, fieldsList);
			}
			catch (SQLException e) {
				CLogger.error("Cannot access Wikipedia to validate N-Gram frequency model " + e.toString());
			}
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}

	}
	
	
	
	
	private static void train() {
		try {
			CEnv.init();
			int fileIndex = 0;
				/*
				 * Execute test of experiment for estimator of N-Gram containing a proper
				 * noun (term with tag NNP(S))
				 */
			final float[] tagEstimatorValues = {1.1F,0.9F, 0.7F, 0.5F };
			final float maxNumTermFreqEstimatorValue = 0.8F;					
			for( float tagEstimatorValue : tagEstimatorValues) {
				executeTrainingRun(tagEstimatorValue, maxNumTermFreqEstimatorValue, fileIndex++);
			}
			
				/*
				 * Execute test of experiment for the estimator of the maximum 
				 * relative frequency of any terms of the N-Gram
				 */
			final float[] maxNumTermFreqEstimatorValues = {0.5F, 0.8F, 1.1F, 1.5F };
			final float tagEstimatorValue = 0.6F;
			for( float maxNumTermFreqEstimatorVal : maxNumTermFreqEstimatorValues) {
				executeTrainingRun(tagEstimatorValue, maxNumTermFreqEstimatorVal, fileIndex++);
			}
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}

	}
		
	
	
	private static void executeClustering(float[] tagEstimatorValues, float[] maxTermFreqEstimatorValues, int numClusters) {
		try {
			CNGramsClassifier nGramClassifier = new CNGramsClassifier(startIndex,endIndex);
			nGramClassifier.cluster(tagEstimatorValues, maxTermFreqEstimatorValues, numClusters);
		}
		catch (SQLException e) {
			CLogger.error("Cannot access Wikipedia to validate N-Gram frequency model " + e.toString());
		}
	}
	
	
	
	private static void generateEstimatorValues(float[] tagEstimatorValues, float[] maxTermFreqEstimatorValues) {
		float value = 0.3F;
		for( int k = 0; k < tagEstimatorValues.length; k++) {
			tagEstimatorValues[k] = value;
			value += 0.25F;
		}

		value = 0.3F;
		for( int k = 0; k < maxTermFreqEstimatorValues.length; k++) {
			maxTermFreqEstimatorValues[k] = value;
			value += 0.25F;
		}
	}
		
	
	
	
	private static void executeTrainingRun(float tagEstimatorValue, float maxNumTermFreqEstimatorValue, int fileIndex) { 
		try {
			StringBuilder buf = new StringBuilder("Execute run for ");
			buf.append(tagEstimatorValue);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(maxNumTermFreqEstimatorValue);
			buf.append(" [");
			buf.append(startIndex);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(endIndex);
			buf.append("]");
			System.out.println(buf.toString());
			
			CNGramsClassifier nGramClassifier = new CNGramsClassifier(startIndex, endIndex);
			nGramClassifier.setFileIndex(fileIndex);
			int numRecords = nGramClassifier.train();
			CLogger.info(numRecords + " records used in training");
		}
		catch( SQLException e) {
			CLogger.error(e.toString());
		}
	}

}
// -----------------------  eof ----------------------------------
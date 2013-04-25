// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;

import com.c24x7.mapred.AMapReduce;
import com.c24x7.mapred.CTestMapReduce;
import com.c24x7.mapred.CTrainingMapReduce;
import com.c24x7.mapred.CValidationMapReduce;
import com.c24x7.mapred.IModelStats;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Singleton class that implements the classifier for generating the features 
			 * of the taxonomy model.The model features are:<br>
			 * - Normalized distribution of weight of most important parts of speech<br>
			 * - Relative rank of the part of the speech in the document (i.e. rank=1 for the part 
			 * of the speech which has the higher relative term frequency (TF-IDF)<br>
			 * - Length of the taxonomy (or ontology path)<br>
			 * - Normalized distribution of the relative weight of each edge of the ontology path.<p>
			 * 
			 * @author Patrick Nicolas
			 * @date 02/02/2012
			 * @see com.c24x7.models.learners.taxonomy.CTaxonomyModel
			 */
public final class CTaxonomyClassifier implements Runnable  {
	
	
	private AMapReduce		_mapReducer = null;
	private int				_numThreads = 1;
	private static boolean	completionFlag = false;

	/**
	 * <p>Create a classifier to generate a model for extracting the
	 * taxonomy from content.</p>
	 */
	public CTaxonomyClassifier(int numThreads) { 
		_numThreads = numThreads;
	}
	

	
	public int getNumSamples() {
		return ATaxonomyDataSets.getCounter();
	}
	
	
	
	
	public void train() {
		int maxid = (int)CDbpediaSql.getInstance().getNumEntries();
		train(new int[] { 1, maxid-1 });
	}
	
		/**
		 * <p>Create a taxonomy model through supervised training using a list of Wikipedia records</p>
		 * @param indexStart index of the first Wikipedia record used in the training
		 * @param indexEnd index of the last Wikipedia record used during training
		 */
	public void train(int[] range) {
		if( range == null || range.length < 2 || range[0] >= range[1]) {
			throw new IllegalArgumentException("Ending index < Start index!");
		}
		
		loggingSetup();
	
		/*
		 * Select the appropriate map-reduce tasks.
		 */
		IModelStats modelStats = new CTaxonomyModelStats();
		_mapReducer = new CTrainingMapReduce(modelStats, _numThreads);

		
		CTaxonomyTrainingSets dataSets = new CTaxonomyTrainingSets(range[0], range[1]);
		_mapReducer.map(range, dataSets);
		/*
		 * start the controlling thread
		 */
		if( _numThreads > 1 ) {
			new Thread(this).start();
		}
		else {
			_mapReducer.reduce();
		}
	}
		

		/**
		 * <p>Validate the taxonomy model using a list of Wikipedia records</p>
		 * @param indexStart index of the first Wikipedia record used in the validation process.
		 * @param indexEnd index of the last Wikipedia record used during validation.
		 */
	public void validate(int[] range) {
		if( range == null || range.length < 2 || range[0] >= range[1]) {
			throw new IllegalArgumentException("Ending index < Start index!");
		}
		loggingSetup();
		
		_mapReducer = new CValidationMapReduce(_numThreads);
		CTaxonomyValidationSets dataSets = new CTaxonomyValidationSets(range[0], range[1]);
		
		_mapReducer.map(range, dataSets);
		
		if( _numThreads > 1) {
			new Thread(this).start();
			
			while( !completionFlag ) {
				try {
					Thread.sleep(10000);
				}
				catch( InterruptedException e) {
					CLogger.error(e.toString());
				}
			}
			
			completionFlag = false;
		}
		else {
			_mapReducer.reduce();
		}
	}
	
	

		/**
		 * <p>Validate the taxonomy model using a list of Wikipedia records</p>
		 * @param indexStart index of the first Wikipedia record used in the validation process.
		 * @param indexEnd index of the last Wikipedia record used during validation.
		 */
	public void test(int[] range) {
		if( range == null || range.length < 2 || range[0] >= range[1]) {
			throw new IllegalArgumentException("Ending index < Start index!");
		}
		
		_mapReducer = new CTestMapReduce(_numThreads);
		CTaxonomyTestSets dataSets = new CTaxonomyTestSets(range[0], range[1]);
		
		_mapReducer.map(range, dataSets);
		_mapReducer.reduce();
	}


		
	
	/**
	 * <p>Main thread for executing the reduce task for training, clustering
	 * and validation of the taxonomy model.</p>
	 */
	public void run() {
		_mapReducer.reduce();
		completionFlag = true;
	}
	
	
	private void loggingSetup() {
		CLogger.setLoggerInfo(CLogger.TAXONOMY_TRAIN_TRACE);
	}
}

// ------------------------  EOF -------------------------

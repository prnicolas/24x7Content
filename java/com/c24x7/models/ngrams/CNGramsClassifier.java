// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.ngrams;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.CText;
import com.c24x7.math.clustering.CDataPoint;
import com.c24x7.math.clustering.CKMeansClustering;
import com.c24x7.semantics.CTaxonomyConnectionsPool;
import com.c24x7.semantics.CTaxonomyConnectionsPool.NTaxonomiesConn;
import com.c24x7.semantics.CTaxonomyExtractor;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.textanalyzer.tfidf.CTfIdfScore;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;



			/**
			 * <p>Classifier that generates a model for evaluate semantically viable
			 * N-Grams according to their relative frequencies, tags and number of 
			 * terms in the N-Gram. The classifier generates the NGrams Frequency Model
			 * used to predict the most relevant semantically valid N-Gram for any 
			 * document. The training phase leverages the Wikipedia reference database.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/13/2012
			 */
public final class CNGramsClassifier {
	private static final String TRAINING_FILES		= CEnv.trainingDir + "sets/ngrams_freq_training";
	private static final String CLUSTER_FILES		= CEnv.trainingDir + "sets/ngrams_freq_clustering";
	private static final String REGRESSION_FILES 	= CEnv.trainingDir + "sets/ngrams_freq_regression";
	
	private static final int 	MIN_CONTENT_LENGTH 	= 700;

	
			/**
			 * <p>Statistics related to labeled 1-Grams used for training
			 * of the N-Gram frequency classifier. The statistics includes rank of
			 * the labeled 1-GRAM (according to TF-IDF value), relative frequency 
			 * of the 1-GRAM in the document, the percentage of proper nouns and
			 * the probability of the 1-GRAM to have a specific order.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/11/2012
			 */
	private class NLabeled1GramsStats {
		protected static final int MAX_SAMPLING_SIZE = 2000;
		
		protected int		_nGramRank 					= CEnv.UNINITIALIZED_INT;
		protected float[]	_numGramOccurrences 		= null;
		protected float 	_ratioNNP 					= CEnv.UNINITIALIZED_FLOAT;
		protected float		_probNGramRank				= CEnv.UNINITIALIZED_FLOAT;
		
		protected int		_lastEntryIndex 			= CEnv.UNINITIALIZED_INT;
		protected float 	_meanNumGramOccurrences 	= CEnv.UNINITIALIZED_FLOAT;
		protected float 	_stdDevNumGramOccurrences 	= CEnv.UNINITIALIZED_FLOAT;
	
		private  NLabeled1GramsStats(int nGramRank) {
			_nGramRank = nGramRank;
			_numGramOccurrences = new float[MAX_SAMPLING_SIZE];
		}
		
		
			/**
			 * <p>Add a new observation for a labeled 1-Gram with relative frequency and
			 * tag type as parameters.</p>
			 * @param numGramOccurrences relative number of occurrences in the labeled document
			 * @param containsNNPTag specify if the 1-Gram contains a proper noun  (tag = NNP(S))
			 * @param maxNumTermOccurrences  maximum relative number of occurrences of any terms contained in the 1-Gram 
			 * @return true if observation is properly added, false otherwise
			 */
		protected boolean add(double numGramOccurrences, boolean containsNNPTag, double maxNumTermOccurrences) {
			boolean entryAdded = _lastEntryIndex < MAX_SAMPLING_SIZE-1;
			if(entryAdded) {
				_lastEntryIndex++;
				if(containsNNPTag) {
					_ratioNNP++;
				}
				_numGramOccurrences[_lastEntryIndex] = (float)numGramOccurrences;
			}
			return entryAdded;
		}
		
			
		/**
		 * <p>Compute the mean values and standard deviation of key statistics
		 * collected for this labeled 1-GRAM.</p>
		 * @param totalNumEntries  total number of observations used in the training of the classifier
		 * @return true if compute succeeds, false otherwise
		 */
		protected boolean compute(int totalNumberEntries) {
			if( _lastEntryIndex > 0) {
					/*
					 * Compute the probability of a semantically
					 * valid N-Gram to have a specific rank.
					 */
				final float count = (float)(_lastEntryIndex+1);
				_probNGramRank = ((float)count)/totalNumberEntries;
				
					/*
					 * Computes the mean value of the number of
					 * occurrences of a semantically valid N-Gram
					 * in a document.
					 */
				_meanNumGramOccurrences = 0.0F;
				for( int k = 0; k <= _lastEntryIndex; k++) {
					_meanNumGramOccurrences += _numGramOccurrences[k];
				}
				_meanNumGramOccurrences /= count;
				
					/*
					 * Computes the standard deviation value of the number of
					 * occurrences of a semantically valid N-Gram
					 * in a document.
					 */
				float diff = 0.0F;
				for( int k = 0; k <= _lastEntryIndex; k++) {
					diff = _numGramOccurrences[k] - _meanNumGramOccurrences;
					_stdDevNumGramOccurrences += diff*diff;
				}
				_stdDevNumGramOccurrences /= count;
				
					/*
					 * Computes the ratio of semantic valid
					 * N-Gram with this rank to be a proper noun.
					 */
				_ratioNNP /= count;
			}

			return (_lastEntryIndex > 0);
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(_nGramRank);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_probNGramRank);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_meanNumGramOccurrences);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_stdDevNumGramOccurrences);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_ratioNNP);
			
			return buf.toString();
		}
	}
	
	
			/**
			 * <p>Statistics related to labeled 1-Grams used for training
			 * of the N-Gram frequency classifier. The statistics includes rank of
			 * the labeled N-GRAM (according to TF-IDF value), relative frequency 
			 * of the 1-GRAM in the document, the percentage of N-GRAM with this
			 * specific order to contain a proper noun and the probability of the 
			 * N-GRAM to have a specific order.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/11/2012
			 */
	private final class NLabeledNGramsStats extends NLabeled1GramsStats {
		private float[] _maxNumTermOccurrences 		= null;
		private float 	_meanMaxNumTermOccurrences 	= CEnv.UNINITIALIZED_FLOAT;
		private float 	_stdMaxNumTermOccurrences 	= CEnv.UNINITIALIZED_FLOAT;
		
		private NLabeledNGramsStats(int nGramRank) {
			super(nGramRank);
			_maxNumTermOccurrences = new float[MAX_SAMPLING_SIZE];
		}
		
		
		/**
		 * <p>Add a new observation for a labeled N-Gram with relative frequency and
		 * tag type as parameters.</p>
		 * @param numGramOccurrences relative number of occurrences in the labeled document
		 * @param isNNP specify if the N-Gram contains a proper noun
		 * @param maxNumTermOccurrences  maximum relative number of occurrences of any terms contained in the N-Gram 
		 * @return true if observation is properly added, false otherwise
		 */
		@Override
		protected boolean add(double numGramOccurrences, boolean containsNNP, double maxNumTermOccurrences) {
			boolean entryAdded = super.add(numGramOccurrences, containsNNP, CEnv.UNINITIALIZED_FLOAT);
			if( entryAdded ) {
				_maxNumTermOccurrences[_lastEntryIndex]=  (float)maxNumTermOccurrences;
			}
			return entryAdded;
		}
		
		/**
		 * <p>Compute the mean values and standard deviation of key statistics
		 * collected for this labeled N-GRAM.</p>
		 * @param totalNumEntries  total number of observations used in the training of the classifier
		 * @return true if compute succeeds, false otherwise
		 */
		@Override
		protected boolean compute(int totalNumEntries) {
			boolean success = super.compute(totalNumEntries);
			if( success ) {
				final float count = (float)(_lastEntryIndex+1);
				
					/*
					 * Compute the mean value for the maximum number of 
					 * N-Gram terms occurrences.
					 */
				for( int k = 0; k <= _lastEntryIndex; k++) {
					_meanMaxNumTermOccurrences += _maxNumTermOccurrences[k];
				}
				_meanMaxNumTermOccurrences /= count;
				
				/*
				 * Compute the standard deviation value for the maximum number of 
				 * N-Gram terms occurrences.
				 */
				float diff = 0.0F;
				for( int k = 0; k <= _lastEntryIndex; k++) {
					diff = _maxNumTermOccurrences[k] - _meanMaxNumTermOccurrences;
					_stdMaxNumTermOccurrences += diff*diff;
				}
				_stdMaxNumTermOccurrences /= count;
				_stdMaxNumTermOccurrences = (float)Math.sqrt(_stdMaxNumTermOccurrences);
			}
			
			return success;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(super.toString());
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_meanMaxNumTermOccurrences);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_stdMaxNumTermOccurrences);
			
			return buf.toString();
		}
	}
	
			/**
			 * <p>Class that implements the regression (classification according to the
			 * TF-IDF ranking) on 1-GRAM and N-GRAM order statistics.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/12/2012
			 */
	private final class NNGramsFrequencyRegression {
		private static final int MAX_ORDER_NGRAM_STATS = 7;

		private List<CNGramsStats> _nGramsFrequencyStatsList = null;

		private NNGramsFrequencyRegression() {
			_nGramsFrequencyStatsList = new LinkedList<CNGramsStats>();
		}

			/**
			 * <p>Add a new array of N-Grams order statistics to the regression.</p>
			 * @param nGramsOrderStat array of N-Gram order statistics.
			 */
		private void add(CNGramsStats nGramsFrequencyStat) {
			_nGramsFrequencyStatsList.add(nGramsFrequencyStat);
		}
			
		
			/**
			 * <p>Compute the key parameters of the NGramsOrder model.</p>
			 */
		private void compute() {
						
			int oneGramsCounter = 0;
			int nGramsCounter = 0;
			
			/*
			 * initialize the statistics for NGram
			 */
			NLabeledNGramsStats[] nGramsStat = new NLabeledNGramsStats[MAX_ORDER_NGRAM_STATS];
			NLabeled1GramsStats[] oneGramsStat = new NLabeled1GramsStats[MAX_ORDER_NGRAM_STATS];
			for(int k = 0; k < MAX_ORDER_NGRAM_STATS; k++) {
				nGramsStat[k] = new NLabeledNGramsStats(k);
				oneGramsStat[k] = new NLabeled1GramsStats(k);
			}
			
			int nGramRank = 0;
			for( CNGramsStats nGramsFrequencyStat : _nGramsFrequencyStatsList) {
				nGramRank = nGramsFrequencyStat.getNGramRank();
				
					/*
					 * Collect only lower order NGram labels
					 */
				if( nGramRank < MAX_ORDER_NGRAM_STATS) {
					if(nGramsFrequencyStat.isCompound()) {
						nGramsStat[nGramRank].add(	nGramsFrequencyStat.getNumNGramOccurrences(), 
												nGramsFrequencyStat.isNNP(),
												nGramsFrequencyStat.getMaxNumTermOccurrences() );
						nGramsCounter++;
					}
					else {
						oneGramsStat[nGramRank].add(nGramsFrequencyStat.getNumNGramOccurrences(), 
									  			nGramsFrequencyStat.isNNP() , 
									  			-1);
						oneGramsCounter++;
					}
				}
			}
			
			CNGramsModel instance = CNGramsModel.getInstance();
			
				/*
				 * Normalize to compute the average values..
				 */
			StringBuilder buf = new StringBuilder();
			buf.append(CFileUtil.COMMENTS_FIRST_CHAR);
			buf.append(" nnpFudge:");
			buf.append(instance.getTagEstimator());
			buf.append(" compoundFudge:");
			buf.append(instance.getMaxTermFreqEstimator());
			buf.append("\n");
			
			buf.append(CFileUtil.COMMENTS_FIRST_CHAR);
			buf.append( "1-Gram frequency model parameters\n");
			for(int k = 0; k < MAX_ORDER_NGRAM_STATS; k++) {
				if( oneGramsStat[k].compute(oneGramsCounter) ) {
					buf.append(oneGramsStat[k].toString());
					buf.append("\n");
				}
			}
			buf.append(CEnv.ENTRIES_DELIM);
			buf.append("\n");
			buf.append(CFileUtil.COMMENTS_FIRST_CHAR);
			buf.append( "N-Gram frequency model parameters\n");
			for(int k = 0; k < MAX_ORDER_NGRAM_STATS; k++) {
				if(nGramsStat[k].compute(nGramsCounter)) {
					buf.append(nGramsStat[k].toString());
					buf.append("\n");
				}
			}
			buf.append(CEnv.ENTRIES_DELIM);
				
			try {
				final String fileName = TRAINING_FILES + "_" + _fileIndex;
				CFileUtil.write(fileName, buf.toString());
			}
			catch( IOException e) {
				CLogger.error(e.toString());
			}
		}
	}
	
	private interface ISets {
		public void add(CNGramsStats nGramsFreqStats);
		public void compute();
		public boolean isValid(String[] fields);
	}
	
	private class NTrainingSets implements ISets {
		private NNGramsFrequencyRegression _nGramsFrequencyRegression = null;
		
		private NTrainingSets() {
			_nGramsFrequencyRegression = new NNGramsFrequencyRegression();
		}
		
		public void add(CNGramsStats nGramsFreqStats) {
			_nGramsFrequencyRegression.add(nGramsFreqStats);
		}
		
		public void compute() {
			_nGramsFrequencyRegression.compute();
		}
		
		public boolean isValid(String[] fields) {
			return false;
		}
	}
	
	private class NValidationSets implements ISets {
		private List<Integer>	_validation1GramRankList = null;
		private List<Boolean>	_validation1GramNNPList = null;
		private List<Integer>	_validationNGramRankList = null;
		private List<Boolean>	_validationNGramNNPList = null;

		private StringBuilder   _buf = null;
		
		private NValidationSets() {
			_validation1GramRankList = new LinkedList<Integer>();
			_validation1GramNNPList =  new LinkedList<Boolean>();
			_validationNGramRankList = new LinkedList<Integer>();
			_validationNGramNNPList = new LinkedList<Boolean>();
			_buf = new StringBuilder();
		}
		
		public void add(CNGramsStats nGramsFreqStats) {
			String label = nGramsFreqStats.getLabel();
		
			if( label.split(" ").length > 1) {
				_validationNGramRankList.add(Integer.valueOf(nGramsFreqStats.getNGramRank()));
				_validationNGramNNPList.add(Boolean.valueOf(nGramsFreqStats.isNNP()));
			}
			else {
				_validation1GramRankList.add(Integer.valueOf(nGramsFreqStats.getNGramRank()));
				_validation1GramNNPList.add(Boolean.valueOf(nGramsFreqStats.isNNP()));
			}
		}
		
		@Override
		public String toString() {
			return _buf.toString();
		}
		
		public void compute() {
			int meanNNPGramRank = 0;
			int nNNPCounter = 0;
			int meanNotNNPGramRank = 0;
			int nNotNNPCounter = 0;
			
			int listIndex = 0;
			Boolean isNNPBool = null;
			for(Integer nGramRankInt : _validation1GramRankList) {
				isNNPBool = _validation1GramNNPList.get(listIndex);
				if(isNNPBool.booleanValue()) {
					meanNNPGramRank += nGramRankInt.intValue();
					nNNPCounter++;
				}
				else {
					meanNotNNPGramRank += nGramRankInt.intValue();
					nNotNNPCounter++;
				}
				listIndex++;
			}
			
			float tagEstimator = CNGramsModel.getInstance().getTagEstimator();
			float maxTermFreqEstimator = CNGramsModel.getInstance().getMaxTermFreqEstimator();
			
			_buf.append(tagEstimator);
			_buf.append(CEnv.FIELD_DELIM);
			_buf.append(maxTermFreqEstimator);
			_buf.append(CEnv.FIELD_DELIM);
			_buf.append(((float)meanNNPGramRank)/nNNPCounter);
			_buf.append(CEnv.FIELD_DELIM);
			_buf.append(((float)meanNotNNPGramRank)/nNotNNPCounter);
			_buf.append(CEnv.FIELD_DELIM);
			_buf.append(((float)(meanNNPGramRank+meanNotNNPGramRank))/(nNNPCounter+nNotNNPCounter));
			_buf.append("\n");
		}
		
		public boolean isValid(String[] fields) {
			return (fields[2] != null && fields[2].length() > 2 && fields[1].length() > MIN_CONTENT_LENGTH);
		}
	}
	
	private class NClusteringSets implements ISets {
		private List<double[]> _allDatasetsList = null;
		private List<double[]> _oneGramDatasetsList = null;
		private List<double[]> _nGramDatasetsList = null;
		
		public NClusteringSets() {
			_allDatasetsList = new LinkedList<double[]>();
			_oneGramDatasetsList = new LinkedList<double[]>();
			_nGramDatasetsList = new LinkedList<double[]>();
		}
		
		public final List<double[]> getAllDatasetsList() {
			return _allDatasetsList;
		}
		
		public final List<double[]> getOneGramDatasetsList() {
			return _oneGramDatasetsList;
		}
		
		public final List<double[]> getNGramDatasetsList() {
			return _nGramDatasetsList;
		}
		
		public void add(CNGramsStats nGramsFreqStats) {
			
			String label = nGramsFreqStats.getLabel();
			float tagEstimator = CNGramsModel.getInstance().getTagEstimator();
			float maxTermFreqEstimator = CNGramsModel.getInstance().getMaxTermFreqEstimator();
			
			double[] allDataset = new double[5];
			double isNNPValue = nGramsFreqStats.isNNP() ? 1.0 : 0.0;
			allDataset[0] = nGramsFreqStats.getNGramRank();
			allDataset[1] = tagEstimator;
			allDataset[2] = maxTermFreqEstimator;
			allDataset[3] = isNNPValue;
			allDataset[4] = (label.split(" ").length > 1) ? 1.0 : 0.0;

			
			if( label.split(" ").length > 1) {
				allDataset[4] = 1.0;
				double[] nGramDataset = new double[4];
				nGramDataset[0] = nGramsFreqStats.getNGramRank();
				nGramDataset[1] = tagEstimator;
				nGramDataset[2] = maxTermFreqEstimator;
				nGramDataset[3] = isNNPValue;
				_nGramDatasetsList.add(nGramDataset);
			}
			else {
				allDataset[4] = 0.0;
				double[] oneGramDataset = new double[4];
				oneGramDataset[0] = nGramsFreqStats.getNGramRank();
				oneGramDataset[1] = tagEstimator;
				oneGramDataset[2] = maxTermFreqEstimator;
				oneGramDataset[3] = isNNPValue;
				_oneGramDatasetsList.add(oneGramDataset);
			}
			_allDatasetsList.add(allDataset);
		}
		
		public void compute() {}
		
		public boolean isValid(String[] fields) {
			return (fields[2] != null && fields[2].length() > 2 && fields[1].length() > MIN_CONTENT_LENGTH);
		}
	}
	
	
	private int _fileIndex 	= CEnv.UNINITIALIZED_INT;
	private int _startIndex = CEnv.UNINITIALIZED_INT;
	private int _endIndex 	= CEnv.UNINITIALIZED_INT;
	
	
	
		/**
		 * <p>Create a classifier for N-Gram frequency model with the default
		 * estimator for N-Gram to contains a proper nouns (tag = NNP(S)) and  
		 * the maximum relative frequency of any terms of a N-Gram terms.</p>
		 */
	public CNGramsClassifier(int startIndex, int endIndex) { 
		if( endIndex <= startIndex) {
			throw new IllegalArgumentException("Incorrect Wikipedia database indexes for N-Grams classifier");
		}
		
		_startIndex = startIndex;
		_endIndex  = endIndex;
	}
	
	
	
	public void setFileIndex(int fileIndex) {
		_fileIndex = fileIndex;
	}
	
	
	
			/**
			 * <p>Create or setup a training file using Wikipedia reference
			 * database.</p>
			 * @param startIndex index of the first record in the Wikipedia reference database
			 * @param endIndex index of the last record in the Wikipedia reference database
			 * @return Number of training sets created
			 * @throws SQLException if Wikipedia database is unavailable
			 */
	public int train() throws SQLException {  	
		CDbpediaSql query = CDbpediaSql.getInstance();
		List<String[]> fieldsList = query.execute(_startIndex, _endIndex - _startIndex);
		return executeRun(fieldsList, new NTrainingSets());
	}
	
	
			/**
			 * <p>Execute the validation of the N-Gram frequency model against the
			 * Wikipedia reference database.</p>
			 * @param startIndex index of the first record in the Wikipedia reference database
			 * @param endIndex index of the last record in the Wikipedia reference database
			 * @return Number of training sets created
			 * @throws SQLException if Wikipedia database is unavailable
			 */
	public int validate() throws SQLException {  	
		CDbpediaSql query = CDbpediaSql.getInstance();
		List<String[]> fieldsList = query.execute(_startIndex, _endIndex - _startIndex);

		return executeRun(fieldsList, new NValidationSets());
	}
	
	
			/**
			 * <p>Execute the validation of the N-Gram frequency model against the
			 * Wikipedia reference database.</p>
			 * @param startIndex index of the first record in the Wikipedia reference database
			 * @param endIndex index of the last record in the Wikipedia reference database
			 * @return Number of training sets created
			 * @throws SQLException if Wikipedia database is unavailable
			 */
	public int regress(float[] tagTestValues, float[] maxNumTermTestValues, List<String[]> fieldsList) throws SQLException {  	
		int numRecords = -1;
		NValidationSets validationSets = null;
		StringBuilder buf = new StringBuilder("tagEstimator,maxTermFreqEstimator,meanNNP,meanNotNNP,meanAll\n");
			
		for(float tagTestValue: tagTestValues) {
			for( float maxNumTermTestValue : maxNumTermTestValues) {
		
				CNGramsModel.getInstance().setTagEstimator(tagTestValue);
				CNGramsModel.getInstance().setMaxTermFreqEstimator(maxNumTermTestValue);
					
				validationSets = new NValidationSets();
				numRecords =  executeRun(fieldsList, validationSets);
				buf.append(validationSets.toString());
			}
		}
			
		try {
			CTfIdfScore.ITfDiscriminant discriminant = CTfIdfScore.getInstance().getTfDiscrimant();
			final String fileName = REGRESSION_FILES + "_" + discriminant.getName();
			CFileUtil.write(fileName, buf.toString());
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
	
		return numRecords;
	}

		
		
	public int cluster(float[] tagTestValues, float[] maxNumTermTestValues, int numClusters) throws SQLException {
		
		List<CDataPoint> allDataPointsList 		= new ArrayList<CDataPoint>();
		List<CDataPoint> oneGramDataPointsList	= new ArrayList<CDataPoint>();
		List<CDataPoint> nGramDataPointsList 	= new ArrayList<CDataPoint>();
			
		int numRecords = 0;
		NClusteringSets clusteringSets = null;
		int dataIndex = 0;
		CDbpediaSql query = CDbpediaSql.getInstance();
		query.setQuery(new String[] { "label", "lgabstract" }, null);
		
		List<String[]> fieldsList = query.execute(_startIndex, _endIndex - _startIndex);
		
				
		for(float tagTestValue: tagTestValues) {
			for( float maxNumTermTestValue : maxNumTermTestValues) {
				
				CNGramsModel.getInstance().setTagEstimator(tagTestValue);
				CNGramsModel.getInstance().setMaxTermFreqEstimator(maxNumTermTestValue);
				
				clusteringSets = new NClusteringSets();
				numRecords +=  executeRun(fieldsList, clusteringSets);
					
				CDataPoint datapoint = null;
				
				dataIndex = allDataPointsList.size();
				List<double[]> valuesList = clusteringSets.getAllDatasetsList();
				for( double[] values : valuesList) {
					datapoint = new CDataPoint(values, dataIndex);
					allDataPointsList.add(datapoint);
				}
					
				dataIndex = oneGramDataPointsList.size();
				valuesList = clusteringSets.getOneGramDatasetsList();
				for( double[] values : valuesList) {
					datapoint = new CDataPoint(values, dataIndex);
					oneGramDataPointsList.add(datapoint);
				}
					
				dataIndex = nGramDataPointsList.size();
				valuesList = clusteringSets.getNGramDatasetsList();
				for( double[] values : valuesList) {
					datapoint = new CDataPoint(values, dataIndex++);
					nGramDataPointsList.add(datapoint);
				}
			}
		}
			
			/*
			 * Aggregate the clustering results for all N-Grams, 1-Grams and
			 * N-Grams with N >1
			 */
		StringBuilder resultsBuf = new StringBuilder();
		CKMeansClustering kmeanClustering = null;
		if( allDataPointsList.size() > 0) {
			kmeanClustering = new CKMeansClustering(numClusters, 500,allDataPointsList);
			kmeanClustering.train();
			resultsBuf.append("All NGrams\n");
			resultsBuf.append(kmeanClustering.toString());
		}
		if( oneGramDataPointsList.size() > 0) {
			kmeanClustering = new CKMeansClustering(numClusters, 500, oneGramDataPointsList);
			kmeanClustering.train();
			resultsBuf.append("\n1-Grams only\n");
			resultsBuf.append(kmeanClustering.toString());
		}
		if( nGramDataPointsList.size() > 0) {
			kmeanClustering = new CKMeansClustering(numClusters, 500, nGramDataPointsList);
			kmeanClustering.train();
			resultsBuf.append("\nN-Grams only\n");
			resultsBuf.append(kmeanClustering.toString());
		}
		
			/*
			 * Save the clustering results ...
			 */
		try {
			CTfIdfScore.ITfDiscriminant discriminant = CTfIdfScore.getInstance().getTfDiscrimant();
			final String fileName = CLUSTER_FILES + "_" + discriminant.getName() + "_" + String.valueOf(numClusters);
			CFileUtil.write(fileName, resultsBuf.toString());
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		
		return numRecords;
	}


	
						// --------------------------
						// Private Supporting Methods
						// --------------------------
	
	private int executeRun(List<String[]> entriesList, ISets sets) throws SQLException {
		int counter = 0;
		int recordIndex = 0;
		
		for( String[] fields : entriesList) {
			if( sets.isValid(fields)) {
				try {
					CNGramsStats nGramsFrequencyStat = extractFragment(fields[1], fields[0]);
	
					/*
					 * Adds this new observation to the regression or classifier.
					 */
					if( nGramsFrequencyStat != null) {
						sets.add(nGramsFrequencyStat);
						counter++;
					}
				}		
				catch( SemanticAnalysisException e) {
					CLogger.error(e.toString());
				}

			}
			recordIndex++;
		}
		sets.compute();
		return counter;
	}

		
	private CNGramsStats extractFragment(final String content, String label) throws SemanticAnalysisException {
		int nGramRank = -1;
		
		CText document = new CText(content);
		CNGramsStats nGramsFrequencyStats = new CNGramsStats(label);
		
		CNGramsGenerator nGramsExtractor = new CNGramsGenerator(nGramsFrequencyStats);
		if( nGramsExtractor.extract(document, content) ) {
			nGramsFrequencyStats = nGramsExtractor.getNGramsFrequencyStats();
			if( nGramsFrequencyStats != null) {
				NTaxonomiesConn taxonomyConnection = CTaxonomyConnectionsPool.getInstance().getLabelsAndCatsConnection();
				CTaxonomyExtractor taxonomyExtractor = new CTaxonomyExtractor(taxonomyConnection);
				if( taxonomyExtractor.extract(document) ) {
					
						/*
						 * Walk through the list of taxonomyInstance nouns from
						 * this document document to extract statistics for the label.
						 */
					int rank = 0;
					/*
					for( CTaxonomyInstance taxonomyInstance : document.getObjectsMap().values()) {
						if( label.compareTo(taxonomyInstance.getLabel()) == 0) {
							nGramRank = rank;
							break;
						}
						rank++;
					}
					*/
				}
			}
		}
		
		return ( nGramRank >= 0) ? nGramsFrequencyStats : null;
	}
}

// ------------------ EOF --------------------------------

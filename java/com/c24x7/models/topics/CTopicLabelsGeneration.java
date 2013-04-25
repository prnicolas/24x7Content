/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.exception.ClassifierException;
import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.CText;
import com.c24x7.nlservices.CTextSemanticService;
import com.c24x7.topics.CTopicsExtractor.NFeaturesSet;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Class that generate training and validation sets for the
		 * topic classifier (see CTopicClassifier), using Wikipedia 
		 * reference data.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date June 20, 2012 5:52:59 PM
		 */
public final class CTopicLabelsGeneration {
	private static final String TOPICS_RAW_TRAINING_SETS_FILE 		= CEnv.trainingDir + "sets/topics_raw_train";
	private static final String TOPICS_TAGGED_TRAINING_SETS_FILE 	= CEnv.trainingDir + "sets/topics_tagged_train";
	private static final String PREPARED_STATEMENT = "SELECT label,lgabstract,taxonomy FROM 24x7c.dbpedia WHERE id=?;";
	
	private static final int	MIN_ABSTRACT_SIZE 		 		= 512;
	private static final int	MAX_ABSTRACT_SIZE 		 		= 2048;
	private static final int	MIN_NUM_TRAINING_SAMPLES 		= 100;
	private static final int	MAX_NUM_ABSTRACTS_PER_SAMPLE	= 3;
	private static final int	MAX_NUMBER_DOCUMENTS 			= 20;
	private static final int	CURSOR_SPAN						= 17;
	
	protected CSqlPreparedStmt	_pStmt 	= null;

	
	/**
	 * <p>Create a topic classifier to generate and train a topic model
	 * using Wikipedia database records.</p>

	 */
	public CTopicLabelsGeneration() { }

	
		/**
		 * <p>Create a raw training set for building a model to extract topics from
		 * documents. The training set is generated from Wikipedia entries, abstracts
		 * taxonomy and categories taxonomy.</p>
		 * 
		 * @param startIndex index of the first record used in the generation of raw training set
		 * @param endIndex index of the last record used in the generation of raw traing set
		 * @param numTrainingSamples number of training samples to be created 
		 * @return actual number of records in the training set.
		 */
	public int createRawLabels(int startIndex, int endIndex) {
		if(endIndex <= startIndex) {
			throw new IllegalArgumentException("Incorrect indices for creating raw training data for topics model");
		}
		
		if( _pStmt == null) {
			_pStmt = new CSqlPreparedStmt(PREPARED_STATEMENT);
		}
		
		final int numFieldsInFile   = 3;
		int samples 				= 0,
			numAbstractsPerSample 	= 0,
			labelCounter 			= 0;
		
		String[] fields = null;
		StringBuilder recordBuf  = new StringBuilder();
		
		for( int id = startIndex; id < endIndex && samples < MAX_NUMBER_DOCUMENTS;) {
			numAbstractsPerSample = id%MAX_NUM_ABSTRACTS_PER_SAMPLE +1;
			
			StringBuilder[] buf = new StringBuilder[numFieldsInFile];
			for( int k =0; k < numFieldsInFile; k++) {
				buf[k] = new StringBuilder();
			}
			
			/*
			 * Collect the content (abstract, label and taxonomy lineage) for
			 * Wikipedia entries for which abstract has a length > MIN_ABSTRACT_SIZE
			 */
			for( labelCounter = 0; labelCounter < numAbstractsPerSample; ) {
				try {
					fields = getRecord(id);
					if( fields != null) {
						for( int j = 0; j < fields.length; j++) {
							buf[j].append(fields[j]);
							buf[j].append((j ==0)? " - " : "\n");
						}
						labelCounter++;
					}
					id += CURSOR_SPAN;
				}
				catch( SQLException e) {
					CLogger.error(e.toString());
				}
			}
			samples++;
				/*
				 * Populate the content of the training file.
				 */
			String recordStr = buf[0].toString();
			recordBuf.append(recordStr);
			recordBuf.append("\n");
			recordBuf.append(CEnv.ENTRIES_DELIM);
			recordBuf.append("\n");
			
			recordStr =  buf[1].toString();
			recordBuf.append(recordStr);
			recordBuf.append("\n");
			recordBuf.append(CEnv.ENTRIES_DELIM);
			recordBuf.append("\n");
			
			recordStr =  buf[2].toString();
			recordBuf.append(recordStr);
			recordBuf.append(CEnv.ENTRIES_DELIM);
			recordBuf.append("\n");
		}
		
		/*
		 * Save the training set records into the training file
		 */
		try {
			CFileUtil.write(TOPICS_RAW_TRAINING_SETS_FILE, recordBuf.toString());
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		
		if( _pStmt != null) {
			_pStmt.close();
			_pStmt = null;
		}
		
		return samples;
	}


	
		/**
		 * <p>Create a tagged or labeled training set for building a model to extract topics from
		 * documents. The labels are generated from the raw training set..</p>
		 * @return actual number of records in the training set.
		 */
	public int createLabels() throws ClassifierException {
		int samples = 0;
		List<String[]> fieldsList = new ArrayList<String[]>();
		
		try {
				/*
				 * Extract the fields of the training sets..
				 */
			CFileUtil.readBufferedFields(TOPICS_RAW_TRAINING_SETS_FILE, CEnv.ENTRIES_DELIM, fieldsList, 3);
			CTextSemanticService analyzer = new CTextSemanticService();
			CText document = null;
			
			StringBuilder buf = new StringBuilder();
			
			for(String[] fields : fieldsList) {
				Map<String, NFeaturesSet> topicFeaturesMap = new HashMap<String, NFeaturesSet>();
				document = analyzer.getTopicsFeaturesList(fields[1],topicFeaturesMap);
						
				if( topicFeaturesMap.size() > 0) {
					buf.append(";    ----------------------------  ");
					buf.append(fields[0]);
					buf.append("\n");
					for( NFeaturesSet topicFeature : topicFeaturesMap.values()) {
						buf.append("\n");
						buf.append(topicFeature.toString());
						samples++;
					}

					buf.append("\n;\n;\n");
				
						//INFO
					StringBuilder traceBuffer = new StringBuilder("\n ---------- ");
					traceBuffer.append(fields[0]);
					traceBuffer.append(" ------------ \n");
					traceBuffer.append(document.printTaxonomy());
					traceBuffer.append("\n");
					CLogger.info(traceBuffer.toString(), CLogger.TOPIC_TRAIN_TRACE);
				}
			}
			
			if( samples < MIN_NUM_TRAINING_SAMPLES) {
				throw new ClassifierException("Training sets is too small");
			}
			else {
				CFileUtil.write(TOPICS_TAGGED_TRAINING_SETS_FILE, buf.toString());
			}
		}
		catch( IOException e) {
			throw new ClassifierException(e.toString());
		}
		catch( SemanticAnalysisException e) {
			throw new ClassifierException(e.toString());
		}

		return samples;
	}
	
	
	private String[] getRecord(int id) throws SQLException {
		String[] fields = null;
		
		_pStmt.set(1, id);		
		ResultSet rs = _pStmt.query();
		String taxonomyLineageStr = null;
		String lgAbstract = null;
		String keyword = null;


		if( rs.next() ) {
			keyword = rs.getString("label");
			taxonomyLineageStr = rs.getString("taxonomy");
			lgAbstract = rs.getString("lgabstract");
		}
		
		/*
		 * We only extract abstract with more than 1024 characters and only one
		 * taxonomy lineage (or WordNet hierarchy of hypernyms).
		 */
		if(  taxonomyLineageStr != null && taxonomyLineageStr.length() > 8 &&
			lgAbstract != null && lgAbstract.length() > MIN_ABSTRACT_SIZE && lgAbstract.length() < MAX_ABSTRACT_SIZE) {
			
			String decodedTaxonomyLineageStr = CStringUtil.decodeLatin1(taxonomyLineageStr);
			String[] taxonomyLineagesArray = decodedTaxonomyLineageStr.split(CEnv.ENTRIES_DELIM);
			
			fields = new String[3];
			fields[0] = CStringUtil.decodeLatin1(keyword);
			fields[1] = CStringUtil.decodeLatin1(lgAbstract);
			String[] taxonomyLineageSegments = null;
			
			StringBuilder buf = new StringBuilder();
			for(String taxonomyLineage : taxonomyLineagesArray) {
				taxonomyLineageSegments = taxonomyLineage.split("#");
				for( int k = 0; k < taxonomyLineageSegments.length; k++) {
					buf.append("  ");
					buf.append(taxonomyLineageSegments[k]);
					buf.append("\n");
				}
			}
			fields[2] = buf.toString();
		}
		
		return fields;
	}

}

// -----------------------  EOF --------------------------------------
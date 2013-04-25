// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.tags;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.models.CText;
import com.c24x7.models.ngrams.CNGramsGenerator;
import com.c24x7.textanalyzer.CTaggedNGramsExtractor;
import com.c24x7.textanalyzer.ngrams.CTaggedNGramsMap;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.CIntMap;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



			/**
			 * <p>Class that implement the NGrams tag classifier. The purpose of the 
			 * classifier is to extract, build a model (NGram tag distribution) used in
			 * run time to match content to the reference corpus database (Wikipedia).
			 * The training process consists of collecting statistics on the distribution
			 * of tags for 1-Gram and N-Gram on the reference corpus (Wikipedia).</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/07/12
			 */
public final class CTagsClassifier {
	private static final int 	MIN_CONTENT_LENGTH = 1200;
	private static final String PREPARED_STATEMENT = "SELECT label,lgabstract FROM 24x7c.dbpedia WHERE id=?;";
	private static final String	TAGS_STATISTICS_FILE =  CEnv.trainingDir + "sets/ngrams_statistics";
	private static final String TAGS_VALIDATION_TILE =  CEnv.trainingDir + "validation/ngrams_tag_validation_input";
	private static final String TAGS_VALIDATION_RESULTS =  CEnv.trainingDir + "validation/ngrams_tag_validation_results";
	
	private CSqlPreparedStmt _pStmt = null;
	private CIntMap 		_nGramTagsStatsMap = null;
	private CTagsStats 	_1GramTagsStatsMap = null;

	
			/**
			 * <p>Instantiate a NGrams tag classifier to create a NGram tag
			 * frequencies distribution model. The type of statistics collection
			 * for either 1-Gram and N-Gram has to be specified.</p>
			 * @see com.c24x7.models.learners.ngrams.enableNGramTagsStatistics
			 * @see com.c24x7.models.learners.ngrams.enable1GramTagsStatistics
			 */
	public CTagsClassifier() {
		_pStmt = new CSqlPreparedStmt(PREPARED_STATEMENT);
	}
	
	
			/**
			 * <p>Enable the collection of statistics for any N-Gram tags
			 * in the reference corpus database. </p>
			 */
	public void enableNGramTagsStatistics() {
		_nGramTagsStatsMap = new CIntMap();
	}
	
			/**
			 * <p>Enable the collection of statistics for a specified
			 * list of tags in the reference corpus database. </p>
			 * @param tags list of tags for which statistics are collected.
			 * @throws IllegalArgumentException if the tags are undefined.
			 */
	public void enable1GramTagsStatistics(final String[] tags) {
		if( tags == null) {
			throw new IllegalArgumentException("Cannot enable statistics on undefined tags");
		}
		_1GramTagsStatsMap = new CTagsStats(tags);
	}
	
	
		/**
		 * <p>Main method that train the N-Gram tag classifier to create a 
		 * NGram tag model from the reference corpus database (Wikipedia). The
		 * training is implemented as a Map-Reduce tasks (or threads).</p>
		 * @param startIndex index of the first record in the reference corpus database
		 * @param endIndex index of the last record in the reference corpus database
		 * @throws IllegalArgumentException if the database record indexes are improperly defined.
		 * @return number of database entries used in the training phase, -1 if the training fails
		 */
	public int train(final int startIndex, final int endIndex) {
		if(endIndex <= startIndex) {
			throw new IllegalArgumentException("Improper database record ids in NGram tag training");
		}
		
		int counter = -1;
		try {
			counter = map(startIndex, endIndex);
			reduce();
		}
		catch( SQLException e) {
			CLogger.error("Cannot train NGrams " + e.toString());
		}
		
		return counter;
	}
	
	public int validate() {

		List<String[]> fieldsList =  new LinkedList<String[]>();
		final int numFields = 3;
		try {
			CFileUtil.readFields(TAGS_VALIDATION_TILE, CEnv.ENTRIES_DELIM, fieldsList, numFields);
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		
		if( fieldsList.size() > 0) {
			List<String> errors = new LinkedList<String>();
			for( String[] fields : fieldsList) {
				evaluate(fields, errors);			
			}
			
			StringBuilder buf = new StringBuilder("NGram Tag validation results:\n");
			for(String error : errors) {
				buf.append(" ---------------------------------------- \n");
				buf.append(error);
				buf.append("\n");
			}
			
			try {
				CFileUtil.write(TAGS_VALIDATION_RESULTS, buf.toString());
			}
			catch( IOException e) {
				CLogger.error(e.toString());
			}
		}
		
		return fieldsList.size();
	}
	
	
	
	private void evaluate(final String[] fields, List<String> errorsList) {

		String content = fields[0];
		String nGramTags = fields[1];
		String nGramFreq = fields[2];
		
		/*
		 * extracts the NGrams rank labels
		 */
		Map<String, Integer> labeledNGramsRanks = new HashMap<String, Integer>();
		String[] tagKeyValues = nGramTags.split("#");
		String[] keyValue = null;
		for( String tagKeyValue : tagKeyValues) {
			keyValue = tagKeyValue.split(CEnv.KEY_VALUE_DELIM);
			labeledNGramsRanks.put(keyValue[0], Integer.valueOf(keyValue[1]));
		}
		
		/*
		 * extracts the NGrams frequency labels
		 */
		Map<String, Integer> labeledNGramsFreqs = new HashMap<String, Integer>();
		String[] freqKeyValues = nGramFreq.split("#");
		for( String freqKeyValue : freqKeyValues) {
			keyValue = freqKeyValue.split(CEnv.KEY_VALUE_DELIM);
			labeledNGramsFreqs.put(keyValue[0], Integer.valueOf(keyValue[1]));
		}
		
		/*
		 * Extract N-Grams
		 */
		CText document = new CText(content);
		CTaggedNGramsExtractor extractor = new CTaggedNGramsExtractor();
		List<String> recordsErrorsList = new LinkedList<String>();
		
	//	extractor.debugNGrams(document);
		CTaggedNGramsMap nGramsTestMap = extractor.getNGramsMap();
			
		for( String nGramTag : labeledNGramsRanks.keySet()) {
			if( !nGramsTestMap.containsKey(nGramTag)) {
				recordsErrorsList.add(nGramTag + " is not found");
			}
		}
			
		for( String nGramTag : labeledNGramsFreqs.keySet()) {
			if( nGramsTestMap.containsKey(nGramTag)) {
				Integer freq = labeledNGramsFreqs.get(nGramTag);
				if( freq.intValue()!= nGramsTestMap.getFrequency(nGramTag)) {
					recordsErrorsList.add(nGramTag + " incorrect frequency");
				}
			}
			else {
				recordsErrorsList.add(nGramTag + " not found");
			}
		}
			
		if( recordsErrorsList.size() > 0) {
			errorsList.add("\n" + content + "\n");
			errorsList.add(nGramsTestMap.toString());
			errorsList.addAll(recordsErrorsList);
		}
	}
		
	
	
	
					// ----------------------------
					// Private Supporting Methods
					// ----------------------------
	
	private int map(final int startIndex, final int endIndex) throws SQLException  {
		int counter = 0;
		
		for( int id = startIndex; id < endIndex; id++) {
			_pStmt.set(1, id);		
			ResultSet rs = _pStmt.query();
			String content = null;
			String keyword = null;
	
			if( rs.next() ) {
				keyword = rs.getString("label");
				content = rs.getString("lgabstract");
			}
			
			if(keyword != null && content != null && content.length() > MIN_CONTENT_LENGTH) {
				
				final String decodedContent = CStringUtil.decodeLatin1(content);
				final String decodedKeyword = CStringUtil.decodeLatin1(keyword);
				
				int indexKeyword = decodedContent.indexOf(decodedKeyword);
				if( indexKeyword != -1) {
					String tag = extractTags(decodedContent, decodedKeyword);
					if( tag != null && _nGramTagsStatsMap != null) {
						_nGramTagsStatsMap.put(tag);
					}
					counter++;
				}
			}
		}
		
		return counter;
	}
	
	
	
	private void reduce() {
		
		if( _nGramTagsStatsMap != null) {
			StringBuilder buf = new StringBuilder();
			
			class NTagMap extends HashMap<String, Integer> {
				private static final long serialVersionUID = -7969750149980246999L; 
			}
			
			NTagMap[] tagMaps = new NTagMap[4];
			for( int k = 0; k <tagMaps.length; k++ ) {
				tagMaps[k] = new NTagMap();
			}
			
			Integer countObj = null;
			String[] individualTags = null;
			for( String key : _nGramTagsStatsMap.keySet()) {
				individualTags = key.split(" ");
				countObj = _nGramTagsStatsMap.get(key);
				if( individualTags.length < 5) {
					tagMaps[individualTags.length-1].put(key, countObj);
				}
			}
			
			for( int k = 0; k <tagMaps.length; k++ ) {			
				for(String key : tagMaps[k].keySet()) {
					buf.append(key);
					buf.append(CEnv.KEY_VALUE_DELIM);
					buf.append(tagMaps[k].get(key));
					buf.append("\n");
				}
				buf.append("##\n");
			}
			
			try {
				CFileUtil.write(CTagsModel.MODEL_FILE, buf.toString());
			}
			
			catch( IOException e) {
				CLogger.info(e.toString());
			}
		}
		
		if( _1GramTagsStatsMap != null) {
			try {
				CFileUtil.write(TAGS_STATISTICS_FILE, _1GramTagsStatsMap.toString());
			}
			
			catch( IOException e) {
				CLogger.info(e.toString());
			}
		}
	}
	
	
	private String extractTags(final String content, final String keyword) {
		CNGramsGenerator nGramsExtractor = new CNGramsGenerator();
		return nGramsExtractor.extractTags(content, keyword, _1GramTagsStatsMap);
	}
}

// ----------------------------  EOF ------------------------------
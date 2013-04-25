// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tfidf;


import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.CIntMap;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



		/**
		 * <p>Singleton class that encapsulates the generation of the relative 
		 * frequency of term extracted from a Corpus also known as the inverse
		 * document frequency. The results of the computation is stored in the
		 * dbpedia database.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 12/08/2011
		 */

public final class CIdfVector {
	private final static long 	serialVersionUID 		= -695361762757129204L;
	private final static int	DISPLAY_INTERVAL		= 10000;
	private final static String DELIM 					= "\t";
	private final static String ERROR_FILE 				= CEnv.outputDir + "debug/error_idfvector";
	private final static String CORPUS_PATH 			= CEnv.trainingDir + "corpus/google/";
	private final static String SELECT_ALIAS_RECORD 	= "SELECT label,resourceid from 24x7c.dbpedia_aliases WHERE id=?;";
	private final static String SELECT_ENTRY_RECORD 	= "SELECT label FROM 24x7c.dbpedia WHERE id=?;";
	private final static String UPDATE_ENTRY_RECORD 	= "UPDATE 24x7c.dbpedia SET idf=? WHERE id=?;";
	protected final static String UPDATE_ALIAS_RECORD 	= "UPDATE 24x7c.dbpedia_aliases SET idf=? WHERE id=?;";

	
	private final static String[] datasets = {
		"w1.txt", "w2_.txt", "w3_.txt", "w4_.txt"
	};

	private final static int 	MAX_VALUE 			= 300000;
	private final static int 	NO_IDF_MATCH 		= -1;
	private final static double IDF_DESCRIMINANT 	= 1.12;
	private final static float  DEFAULT_IDF_VALUE 	= 0.90F;
	private final static int 	MIN_FREQ_MOST_COMMON_TERMS = 10000;


		/**
		 * <p>Formula that compute the relative frequency (or number of occurrences) 
		 * of a term within a document or a predefined corpus.<br>
		 * The formula to compute IDF for a term with frequency f is log(2 - f*f/(max*max))/log2
		 * @param freq frequency of a term in the corpus or document
		 * @return floating point value between 0 & 1 representing the inverse document frequency.
		 */
	public static float computeIdf(int freq) {
		double relFreq = freq/MAX_VALUE;
		if( relFreq > 1.0) {
			relFreq = 1.0;
		}
		return (float)(1.0 - Math.pow(relFreq, IDF_DESCRIMINANT));
	}


	
		/**
		 * <p>Generate the IDF value for each Dbpedia entry. If the Wikipedia label 
		 * is not found in the corpus used to extract frequency, attempt to compute
		 * the average frequency for each term or section composing the label. (ie.
		 *  F("value of Things" = (F("value") + F("Things"))/2
		 */
	public static long writeDatabase() {
		long count = 1L;
		long failuresCount = 0;
	
			/*
			 * STEP 1: Load the corpus term frequency into a local map.
			 */
		CIntMap idfMap = loadIDFCorpus();
		CLogger.info("Number of entries: " + idfMap.size());

			/*
			 * STEP 2: Load id and resource id from dbpedia_aliases
			 * table into a local map 
			 */
		Map<Integer, String> aliasResourceMap = new HashMap<Integer, String>();
		Map<String, String> errors = new HashMap<String, String>();
			
		int maxAliases_Id = loadDbpediaAliases(aliasResourceMap, errors);
		
			/*
			 * STEP 3: Compute and update the IDF values in dbpedia table
			 */
		int max_id  = CDbpediaSql.getInstance().getNumEntries();
	
		if( max_id != -1L && maxAliases_Id != -1L) { 
			
			Map<Integer, Float> dbpediaIdtoIdfMap = new HashMap<Integer, Float>();

			CSqlPreparedStmt selectStmt = new CSqlPreparedStmt(SELECT_ENTRY_RECORD);
			CSqlPreparedStmt updateStmt = new CSqlPreparedStmt(UPDATE_ENTRY_RECORD);
			
			for(int k = 1; k <= max_id; k++) {
				try {
					if( !addDbpediaIdf(k, 
									   selectStmt, 
									   updateStmt, 
									   idfMap, 
									   dbpediaIdtoIdfMap, 
									   aliasResourceMap) ) {
						failuresCount++;
					}
				}
				catch( SQLException e) {
					errors.put("Dbpedia", "cannot add IDF value: " + e.toString());
					failuresCount++;
				}
				displayProgress("Update dbpedia entries ", count, failuresCount);
				count++;
			}
				
			selectStmt.close();
			updateStmt.close();
			CLogger.info("Done with dbpedia");

				/*
				 * STEP 4: Update the idf values of dbpedia_aliases
				 */
			selectStmt = new CSqlPreparedStmt(SELECT_ALIAS_RECORD);
			updateStmt = new CSqlPreparedStmt(UPDATE_ALIAS_RECORD);
				
			for( int id = 2; id <= maxAliases_Id; id++) {
				try {
					if( !addDbpediaAliasesIdf(selectStmt, 
											  updateStmt, 
											  id, 
											  dbpediaIdtoIdfMap, 
											  idfMap ) ) {
						failuresCount++;
					}
				}
				catch( SQLException e) {
					errors.put("Dbpedia_aliases", "cannot add IDF value:" + e.toString());
					failuresCount++;
				}
	
				displayProgress("Update Dbpedia Aliases: ", count, failuresCount);
				count++;
			}
				
			selectStmt.close();
			updateStmt.close();
		}
		save(errors);

		return count;
	}
	
	

		
	
								// -------------------------
								// Private supporting methods
								// ---------------------------

	private static CIntMap loadIDFCorpus() {
		CIntMap idfMap = new CIntMap();
			
		StringBuilder buf = null;
		for(int j =0; j < datasets.length; j++) {
			buf = new StringBuilder(CORPUS_PATH);
			buf.append(datasets[j]);
			retrieve(idfMap, buf.toString());
		}
		
		return idfMap;
	}
			
	
	private static int loadDbpediaAliases(Map<Integer, String> aliasResourceMap, 
										   Map<String, String> errors) {

		int max_id = CDbpediaSql.getInstance().getNumAliases();
	
		if( max_id != -1) {
			CSqlPreparedStmt selectStmt = new CSqlPreparedStmt(SELECT_ALIAS_RECORD);
			
			for( int j = 2; j <= max_id; j++) {
				String aliasLabel = null;	
				try {
					selectStmt.set(1, j);
				
					ResultSet rs = selectStmt.query();
					long resourceId = -1L;
		
					while(rs.next()) {
						aliasLabel = rs.getString("label");
						resourceId = rs.getInt("resourceid");
					}
					if( aliasLabel != null && aliasLabel.length() > 2 && resourceId != -1L) {
						aliasResourceMap.put(new Integer((int)resourceId),  aliasLabel);
					}
				}
				catch( SQLException e) {
					errors.put(aliasLabel, "Failed to load dbpedia_aliases resource for id=" + j);
				}
				
			}
			
			selectStmt.close();
		}
	
		return max_id;
	}
	
	
	private static boolean addDbpediaIdf(final int id, 
									 	CSqlPreparedStmt selectStmt, 
									 	CSqlPreparedStmt updateStmt, 
									 	CIntMap idfMap, 
									 	Map<Integer, Float> dbpediaIdtoIdfMap,
									 	Map<Integer, String> aliasResourcedMap) throws SQLException  {
			
		float idf = DEFAULT_IDF_VALUE;
			/*
			 * Extract the label corresponding to this id from the main DBpedia table.
			 */
		selectStmt.set(1,id);
		
		ResultSet rs = selectStmt.query();
		String keyword = null;
		if( rs.next() ) {
			keyword = rs.getString("label");
		}
		
			/*
			 * If the label has been retrieved.
			 */
		if( keyword != null) {
			/*
			 * Decode and clean the label
			 */
			keyword = CStringUtil.decodeLatin1(keyword);
			keyword = keyword.toLowerCase();
			int freq = 0;
			
				/*
				 * If the label do not belong to the
				 * IDF/corpus table, then attempt to match a section of
				 * the label delimited by comma if comma in label. If 
				 * none of the sections of the label has an entry in the
				 * corpus frequency table, then retrieve the alias of this
				 * label and attempt to find a match in the corpus frequency table.
				 */
			if( keyword != null && !idfMap.containsKey(keyword)) {
				
					/*
					 * Attempt to extract section of the label delimited
					 * by a comma.
					 */
				List<String> aliasSectionsList = new LinkedList<String>();
				String[] sections = keyword.split(" ");
				if(sections.length > 0) {
					for( String section : sections) {
						section = section.trim();
						aliasSectionsList.add(section);
					}
				}
					/*
					 * Check the frequency of each terms if the
					 * N-Gram extracted from the corpus contains commas.
					 */
				sections = keyword.split(CEnv.FIELD_DELIM);
				if( sections.length > 0) {
					for( String section : sections) {
						section = section.trim();
						aliasSectionsList.add(section);
					}
				}
				
				freq = getFrequency(aliasSectionsList, idfMap);
				
					/*
					 * If a match has not been found, try 
					 * the alias.
					 */
				if( freq == NO_IDF_MATCH) {
					String aliasLabel = aliasResourcedMap.get(new Integer(id));
					if( aliasLabel != null) {
						aliasLabel = aliasLabel.toLowerCase();
						if( idfMap.containsKey(aliasLabel)) {
							freq = idfMap.get(aliasLabel);
						}
					}
				}
			}
			else {
				freq = idfMap.get(keyword).intValue();
			}
			
			/*
			 * compute the relative IDF value.
			 */
	
			if ( freq != NO_IDF_MATCH) {
				idf = computeIdf(freq);
			}
			
			/*
			 * Update the IDF value for this label..
			 */
			updateStmt.set(1,idf);
			updateStmt.set(2,id);
			updateStmt.update();

			dbpediaIdtoIdfMap.put(new Integer(id), new Float(idf));
		}
		
		return (idf != DEFAULT_IDF_VALUE);
	}
	
	
	
	private static boolean addDbpediaAliasesIdf(CSqlPreparedStmt selectStmt, CSqlPreparedStmt updateStmt, int id, Map<Integer, Float> dbpediaIdtoIdfMap, CIntMap idfMap) throws SQLException  {
		
		float idf = DEFAULT_IDF_VALUE;
			/*
			 * Extract the label corresponding to this id from the main DBpedia table.
			 */
		selectStmt.set(1, id);
		ResultSet rs = selectStmt.query();
		String label = null;
		long resourceid = -1;
		
		if( rs.next() ) {
			label = rs.getString("label");
			resourceid = rs.getInt("resourceid");
		}

		if( label != null && label.length() > 2) {
			label = label.toLowerCase();
				/*
				 * First attempt to match the label in the aliases table
				 */
			if( idfMap.containsKey(label)) {
				int freq = idfMap.get(label);
				idf = computeIdf(freq);
			}
			
				/*
				 * If the label does not have an entry in the IDF
				 * corpus and the label is an alias of one Wikipedia entry
				 * the compute the label IDF value as the idf value of the
				 * wikipedia entry.
				 */
			else if( resourceid > 0) {
				Integer resIdObj = new Integer((int)resourceid);
				if( dbpediaIdtoIdfMap.containsKey(resIdObj)) {
					idf = dbpediaIdtoIdfMap.get(resIdObj).floatValue();
				}
			} 
	
				/*
				 * Set the IDF value in the aliases table.
				 */
			updateStmt.set(1, idf);
			updateStmt.set(2, id);
			
			updateStmt.update();
		}
		
		return (idf != DEFAULT_IDF_VALUE);
	}
	

	
	public static void extract(final String input, CIntMap map) {
		
		StringBuilder buf = new StringBuilder();
		boolean valid = true;
		int freq = -1;
		
		List<String> terms = CStringUtil.split(input.trim().toLowerCase(), DELIM);
		int j = 0,
		    lastIndex = terms.size()-1;
		
		for( String term : terms) {
			if( j == 0) {
				try {
					freq = Integer.parseInt(term);
				}
				catch( NumberFormatException e) {  
					CLogger.error("Incorrect format for term frequency: " + term);
					valid = false;
				}
			}
			else {
				buf.append(term);
				if( j < lastIndex) {
					buf.append(" ");
				}
			}
			j++;
		}
		if( valid ) {
			map.put(buf.toString().trim(), freq);
		}
	}
	
	
		/*
		 * If an alias has not been found in the IDF table, then
		 * attempt to match one of the section of the keyword is 
		 * the keyword contains commas. We are weeding out the most common
		 * keywords with frequency greater than 5000
		 */
	private static int getFrequency(List<String> aliasSectionsList, CIntMap map) {
		int freq = NO_IDF_MATCH;
		
		if( aliasSectionsList != null) {
			int maxFreq = 0;
			int subtermFreq = 0;
			
			for( String aliasSection : aliasSectionsList) {
				if(map.containsKey(aliasSection)) {
					subtermFreq =  map.get(aliasSection).intValue();
					if(subtermFreq < MIN_FREQ_MOST_COMMON_TERMS) {
						if( maxFreq < subtermFreq ) {
							maxFreq = subtermFreq;
						}
					}
				}
			}
			if(maxFreq > 0) {
				freq = maxFreq;
			}
		}
		return freq;
	}
	
	
	
	
	private static void retrieve(CIntMap map, final String fileName) {
		BufferedReader reader = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( line.length() > 2) {
					extract(line, map);
				}
			}
		}
		catch( IOException e) { }
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) { }
			}
		}
	} 
	
	
	private static void save(Map<String, String> errors) {

		if( errors.size() > 0) {
			StringBuilder buf = new StringBuilder("alias:label\n");
			for( String key : errors.keySet()) {
				buf.append("\n");
				buf.append(key);
				buf.append(CEnv.KEY_VALUE_DELIM);
				buf.append(errors.get(key));
			}
			try {
				CFileUtil.write(ERROR_FILE, buf.toString());
			} 
			catch(IOException e) {
				CLogger.error(e.toString());
			}
		}
	}
	
	
	private static void displayProgress(final String description, long count, long failuresCount) {
		if( count++ %DISPLAY_INTERVAL == 0) {
			CLogger.info(description + " count=" + count + " failures=" + failuresCount);
		}
	}
}


// ----------------------  EOF -------------------------------------------

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.lookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Cache to access dbpedia tables, implemented as a thread
			 * unsafe (read only) hash table. The key defines the label 
			 * entry in dbpedia table and the character specifies the
			 * actual table in dbpedia database.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 01/23/2012
			 */
public class CLookup extends HashMap<String, CLookupRecord> {
	protected static final long serialVersionUID = -9201271345717846508L;
	
		/**
		 * <p>File that contains the lookup table for original WordNet taxonomy
		 * and character case.</p>
		 */
	public static final String ORIGINAL 			= CEnv.configDir + "labels";
	
	/**
	 * <p>File that contains the lookup table for original WordNet taxonomy
	 * and entries converted to lower case.</p>
	 */
	public static final String ORIGINAL_LOWERCASE 	= CEnv.configDir + "llabels";
	public static final String EXTENDED 			= CEnv.configDir + "labels_ext";
	public static final String EXTENDED_LOWERCASE 	= CEnv.configDir + "llabels_ext";
	
	public static final char 	DBPEDIA_UNDEFINED 				= '0';
	public static final char 	DPBEDIA_ENTRY_UPPER_CASE 		= '1';
	public static final char 	DPBEDIA_ENTRY_ALIAS_UPPER_CASE 	= '2';
	public static final char 	DBPEDIA_ENTRY 					= '3';
	public static final char 	DPBEDIA_ENTRY_ALIAS 			= '4';
		
	protected static CLookup instance = null;

	
		/**
		 * <p>Initialize the lookup table in memory by loading
		 * the data from file with all the labels in lower case by default.</p>
		 * @throws InitException exception if the lookup table is not properly loaded
		 */
	public static void init() throws InitException {
		init(ORIGINAL_LOWERCASE);
	}
	
		/**
		 * <p>Initialize the lookup table in memory by loading the data from file keeping
		 * the original case for all the labels.</p>
		 * @param lookupType type of labels (lower or original case)
		 * @throws InitException exception if the lookup table is not properly loaded
		 */
	public static void init(final String lookupType) throws InitException {
		if( instance == null) {
			instance = new CLookup();
			try {
				instance.load(lookupType);
				instance.setOriginalLabelCase(!isLowerCaseLookupType(lookupType));
				System.out.println("Semantic lookup table " + lookupType + " ready");
			}
			catch( IOException e) {
				throw new InitException(e.toString());
			}
		}
	}
	
		/**
		 * <p>Retrieve the singleton lookup object.</p>
		 * @return unique, single instance of the Lookup class
		 */
	public static CLookup getInstance() {
		return instance;
	}
	
	
	protected static boolean isLowerCaseLookupType(final String lookupType) {
		return (lookupType.compareTo(EXTENDED_LOWERCASE) ==0);
	}
	
	
	protected static boolean isOriginalLookupType(final String lookupType) {
		return (lookupType.compareTo(ORIGINAL_LOWERCASE) == 0 || 
				lookupType.compareTo(ORIGINAL) ==0);
	}
	
	
	private boolean _originalCase = false;

	
	public void setOriginalLabelCase(boolean originalCase) {
		_originalCase = originalCase;
	}
	
		/**
		 * <p>Get a lookup record for a define NGram or its Stem or any 
		 * sub NGram after removal of special characters as first or
		 * last character in the NGram.</p>
		 * @param nGram  N-Gram to check for semantic definition
		 * @param nGramStem Stem of the NGram to check for semantic definition
		 * @return lookup record if found, null otherwise.
		 */
	public CLookupRecord getLookupRecord(final String nGram, final String nGramStem) {
		if( nGram == null) {
			throw new IllegalArgumentException("Cannot extract lookup entry for undefined nGram");
		}
		
		CLookupRecord lookupRecord = null;
		
		final String lcNGram = _originalCase ? nGram : nGram.toLowerCase();
			/*
			 * If the original NGram has a valid semantic definition,
			 * return the lookup record
			 */
		if( containsKey(lcNGram) ) {
			lookupRecord = get(lcNGram);
			lookupRecord.setLabel(nGram);
		}
			/*
			 * If this NGram has a stem check if the 
			 * stem has a valid semantic definition 
			 */
		else if( nGramStem != null) {
	 		final String lcNGramStem = _originalCase ? nGramStem : nGramStem.toLowerCase();
			
	 		if( containsKey(lcNGramStem)) {
				lookupRecord = get(lcNGramStem);
				lookupRecord.setLabel(nGramStem);
			}
		}

		return lookupRecord;
	}
	
	
		/**
		 * <p>Retrieve the semantic record (from the lookup memory table) for a NGram with 
		 * a specific label and tag of type NNP/NNPS or NN/NNS.</p>
		 * @param nGramlabel label of the NGram to be searched for in the lookup memory map
		 * @param isNNP  flag that specifies whether the NGram contains a proper noun (NNP or NNPS tage=).
		 * @return a Lookup record if the memory lookup table contains the label, null otherwise.
		 */
	public CLookupRecord getLookupRecord(final String nGramLabel) {
		return !containsKey(nGramLabel) ? get(nGramLabel.toLowerCase()) : get(nGramLabel);
	}
	
	

	
	
							// ---------------------------
							//  Supporting Private Methods
							// ---------------------------
	

	protected CLookup() { 	}
	
	
	protected void load(final String lookupType) throws IOException {
		BufferedReader reader = null;
			
		try {
			FileInputStream fis = new FileInputStream(lookupType);
			reader = new BufferedReader(new InputStreamReader(fis));
			String newLine = null;
	
			CLookupRecord record = null;
			double idfValue = 0.97;
					
			while ((newLine = reader.readLine()) != null) {	
				newLine = newLine.trim();
				String[] fields = newLine.split(CEnv.KEY_VALUE_DELIM);
				if(fields.length == 3) {
					try {
						idfValue = Double.parseDouble(fields[2]);
						if( idfValue == -1.0) {
							idfValue = 0.97;
						}
						record = new CLookupRecord(fields[1].charAt(0), (float)idfValue);
						put(fields[0], record);
					}
					catch( NumberFormatException e) {
						CLogger.error("Lookup map idf improper formant " + e.toString());
					}
				}
			}
		}
		finally {
			reader.close();
		}
	}

}

// ------------------------  EOF -------------------------------------------------

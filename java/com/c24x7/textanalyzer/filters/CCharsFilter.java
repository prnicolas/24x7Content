// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.filters;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.textanalyzer.stemmer.CPluralStemmer;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Generic class for filtering labels from semantics
		 * datasets such as Freebase, dbpedia.... The client is responsible for
		 * defining the order those filter (character type, non stop words....)
		 * have to be applied.</p>
		 * @author Patrick Nicolas
		 * @date 10/31/2011
		 */
public final class CCharsFilter extends AFilter {
	private final static String STOPWORDS_FILE = CEnv.dictDir + "stopwords.txt";
	private static Map<String, Object> nonStopWordfilterMap = null;
	
	static {
		nonStopWordfilterMap = new HashMap<String, Object>();
		loadDictionary();
	}
	
	private char[] 	_chars 		= null;
	private int 	_quotation 	= -1;
	
	public CCharsFilter() {	
		super();
	}
	
	
	@Override
	public int getQuotation() {
		return _quotation;
	}
	
	
	
		/**
		 * <p>
		 * Main method used to filter out a word or term according to 
		 * a regular expression or a dictionary. </p>
		 * @param word term to filter out.
		 * @return word or substring if word meets filter criteria, null otherwise
		 */
	public String qualify(final String word) {
		String qualifyingWord = null;
		
		if( word.length() > 1 && !nonStopWordfilterMap.containsKey(word)) {
			_chars = word.toCharArray();
			char[] chars = stripSpecialChars();
			
			/*
			 * we allow only ',' as a single character word.
			 */
			if( chars != null && (chars.length > 1 || chars [0] == ',') ) {
				_chars = chars;
						
				int hexChar = -1;
				for( int k =0; k < _chars.length; k++) {
					hexChar = (int)_chars[k];
					
					if(!((hexChar > 0x40 && hexChar < 0x5B) ||	// Lower case characters
					     (hexChar > 0x60 && hexChar < 0x7B) ||	// Upper case character	
					     (hexChar > 0x2B && hexChar < 0x2E) || 	// Punctuation
					     (hexChar == 0x5F || hexChar == 0x26 || hexChar == 0x20)) ){
							chars = null;
					    	break;
					}
				}
				
					/*
					 * if the term is delimited by parenthesis, remove them.
					 */
				if( _chars != null) {
					qualifyingWord = String.valueOf(_chars);
				}
			}
		}
		
		return qualifyingWord;
	}
	
	
		/**
		 * <p>Generate the stem associated to this set of characters.</p>
		 * @return if a plurals stem exists, null otherwise.
		 */
	public String stem() {
		String stem = null;
		if(_chars != null) {
			stem = CPluralStemmer.getInstance().stem(_chars);
		}
		
		return stem;
	}
	

	
					// ----------------------------
					//  Private Supporting Methods
					// ----------------------------
	
	
	private char[] stripSpecialChars() {
		char[] strippedChars = null;
		
		int cursor 		= 0,
			startIndex 	= -1,
			endIndex 	= _chars.length;
		
		do {
			startIndex++;
			if(startIndex >= _chars.length) {
				return null;
			}
			cursor = (int)_chars[startIndex];
		}while (cursor == 0x28 || cursor == 0x22 || cursor == 0x27 || cursor == 0x5C);
		
		do {
			endIndex--;
			if(endIndex < 0) {
				return null;
			}
			cursor = (int)_chars[endIndex];
		}while( cursor == 0x29 || cursor == 0x22 || cursor == 0x27 || cursor == 0x5C);
			
		if( endIndex - startIndex < 0) {
			strippedChars = null;
		}
		else {
			strippedChars = (startIndex > 0 || endIndex < _chars.length-1) ?  
							Arrays.copyOfRange(_chars, startIndex, endIndex+1) : 
							_chars;
		}
		
		return strippedChars;
	}	

		/**
		 * Load the content of the configuration file into this dictionary.
		 */
	 private static void loadDictionary() {
		BufferedReader reader = null;
		String line = null;
	
		try {
			FileInputStream fis = new FileInputStream(STOPWORDS_FILE);
			reader = new BufferedReader(new InputStreamReader(fis));
				
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					nonStopWordfilterMap.put(line, null);
				}
			}
				
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Cannot load dictionary " + e.toString());
		}
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) {
					CLogger.error("Cannot load dictionary " + e.toString());
				}
			}
		}
	}

}

// ----------------------  EOF -------------------------------

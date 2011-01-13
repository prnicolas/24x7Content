// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.textanalyzer;


import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;


/**
 * <p>
 * Class that configure the filter for extracting terms
 * from any document or messages. This filter allows terms
 * as a combination of letters or digits. This class is implemented as a singleton</p>
 * @author Patrick Nicolas
 */
public class CFilter {

	public final static String STOPWORDS_FILE = CEnv.configDir + "stopwords.lst";
	
	private Map<String, String> _stopWords = new HashMap<String, String>();
	private static CFilter filter = new CFilter();
	
	public static CFilter getInstance() {
		return filter;
	}
	

	
	/**
	 * <p>
	 * main method used to filter out a word or term from
	 * any document or social message, according to its length
	 * content (against dictionary) and character it contains</p>
	 * @param word term to filter out.
	 * @return true if word meets filter criteria, false otherwise
	 */
	public boolean qualify(final String word) {	
		return ( (word != null) && !word.trim().equals("") && validate(word) && !_stopWords.containsKey(word));
	}
	
	
	private CFilter() {
		loadDictionary();
	}
	
	

	/**
	 * <p>
	 * Apply filter to terms according to its character(letter,
	 * alpha-numeric, special characters,..)
	 * @param word term to evaluate
	 * @return true if the word has letter and/or digit..
	 */
	 private boolean validate(final String term) {
		boolean containsLetter = false;
		char[] chars = term.toCharArray();
			
		for( int j = 0; j < chars.length; j++) {
			if(Character.isLetterOrDigit(chars[j])) {
				containsLetter = true;
				break;
			}
		}
		return containsLetter;
	 }
	 
	 
		/**
		 * Load the content of the configuration file into this dictionary.
		 */
	 private void loadDictionary() {
		BufferedReader reader = null;
		String line = null;

		try {
			FileInputStream fis = new FileInputStream(STOPWORDS_FILE);
			reader = new BufferedReader(new InputStreamReader(fis));
				
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					_stopWords.put(line, null);
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

// --------------------------  EOF ----------------------------------
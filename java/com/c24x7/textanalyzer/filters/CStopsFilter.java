// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.filters;

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
		 * Class that configure the filter that eliminate non-stop words. This class is implemented
		 * as a singleton.</p>
		* @author Patrick Nicolas
		 * @date 05/23/2011
		 * @see com.c24x7.nlservices.textanalyzer.filter.AFilter
		 */
public class CStopsFilter extends AFilter {
	public final static int    MIN_WORD_LENGTH = 3;
	public final static String STOPWORDS_FILE = CEnv.dictDir + "stopwords_en.txt";
	
	private Map<String, String> _stopWords = new HashMap<String, String>();
	private static CStopsFilter filter = new CStopsFilter();
	
	protected int _minWordLength = MIN_WORD_LENGTH;

	
	
	/**
	 * <p>Return the Singleton for this filter</p>
	 * @return singleton for this filter.
	 */
	public static CStopsFilter getInstance() {
		return getInstance(MIN_WORD_LENGTH);
	}
	
	/**
	 * <p>Return the Singleton for this filter</p>
	 * @return singleton for this filter.
	 */
	public static CStopsFilter getInstance(int minWordLength) {
		filter._minWordLength = minWordLength;
		return filter;
	}
	
	
	/**
	 * <p>Main method used to filter out a word or term from
	 * any document or social message, according to its length
	 * content (against dictionary) and characters it contains. The filter per type of characters
	 * is implemented by the polymorphic method validateChars.</p>
	 * @param word term to filter out. 
	 * @return true if word meets filter criteria, false otherwise
	 */
	@Override
	public String qualify(final String word) {	 
		return validateChars(word) && !_stopWords.containsKey(word) ? word : null;
	}
	
	protected CStopsFilter() {
		loadDictionary();
	}

	
					// ----------------------
					//  Private Methods 
					// -------------------
	

	
	

	/**
	 * <p>Apply filter to term according to its minimum length.</p>
	 * @param word term to evaluate
	 * @return true if the word has letter and/or digit..
	 */
	 protected boolean validateChars(final String term) {
		return (term.length() >= _minWordLength);
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
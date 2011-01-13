// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.textanalyzer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;



			/**
			 * <p>Very simple text parser that filter character string for stop words.</p>
			 * @author Patrick Nicolas
			 * @date 11/26/2010
			 */
public class CTextAnalyzer implements ITextAnalyzer {
	private CWordsTokenizer _wordsTokenizer = null;
	
	
	public CTextAnalyzer() {
		_wordsTokenizer = new CWordsTokenizer();
	}
	
	
			/**
			 * <p>Retrieve the significant words from a input string</p>
			 * @param inputText input content
			 * @return map of the most significant words
			 */
	public Map<String, Integer> getSignificantWords(final String inputText) {
		
		List<String> words = _wordsTokenizer.tokenize(inputText.toLowerCase());
	
			/*
			 * Filter and trim the list of term frequencies vector
			 */
		String curTerm = null;
		CFilter filter = CFilter.getInstance();
	
		Map<String, Integer> tfVector = new HashMap<String, Integer>();
	
		for (String term : words) {
			curTerm = term.trim();
			/*
			 * Filter terms and extract significant terms.
			 */
			if(filter.qualify(curTerm) ) {
				Integer oldValue = tfVector.get(curTerm);
				int newValue = 1;
				
				if ( oldValue != null ) {
					newValue += oldValue.intValue();
				}
				tfVector.put(curTerm, new Integer(newValue));
			}
		}
	
		return tfVector;
	}
	
	@Override
	public String toString() {
		String className = this.getClass().getName();	
		StringBuilder builder = new StringBuilder(className);
		builder.append("\nTokenizer:");
		builder.append(_wordsTokenizer.toString());
		
		return builder.toString();
	}
}

// -------------------------  EOF -------------------------------
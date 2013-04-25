// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tfidf;


import java.util.HashMap;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Class that contains the term frequency for a document. The term frequency class
		 * is implemented as as Hash table <label, frequency> and the maximum frequency term.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 01/14/2012
		 */
public final class CTfVector extends HashMap<String, Integer> {
	private static final long serialVersionUID = 3438992932486964959L;
	
	private int 				_maxFrequency = Integer.MIN_VALUE;
	private Map<String, String> _termStemMap = null;
		
	public CTfVector() {
		super();
		_termStemMap = new HashMap<String, String>();
	}
	
	
		/**
		 * <p>Compute the maximum number of occurrences of any terms or
		 * keywords in a document.</p>
		 * @return highest frequency (or number of occurrences)
		 */
	public final int getMaxFrequency() {
		return _maxFrequency;
	}
	
			/**
			 * <p>Increment the value associated to a key on this integer map, by 1. The
			 * method also keep track of the maximum frequency of any term in the 
			 * document.</p>
			 * @param key key of the key-value pair {'key", +1}
			 */
	public void put(final String term) {
		Integer oldValue = get(term);
		int newValue = 1;
		
		if ( oldValue != null ) {
			newValue += oldValue.intValue();
		}
		if(newValue > _maxFrequency) {
			_maxFrequency = newValue;
		}
		
			/*
			 * If this is a root (or singular form) of
			 * an existing term, then update the term count.
			 */
		Integer newValueInt = Integer.valueOf(newValue);
		super.put(term, newValueInt);
		if(_termStemMap.containsKey(term) ) {
			super.put(_termStemMap.get(term), newValueInt);
		}
	}
	
	
		/**
		 * <p>Increment the value associated to a key on this integer map, by 1. The
		 * method also keep track of the maximum frequency of any term in the 
		 * document.</p>
		 * @param key key of the key-value pair {'key", +1}
		 */
	public void put(final String term, final String stem) {
		if(stem == null) {
			put(term);
		}
		else {
			int newStemFreq = 1;
				/*
				 * If the root (or singular form) of the term
				 * has already been recorded, update the count
				 * of both the term and its root.
				 */
			if( containsKey(stem)) {
				if( !_termStemMap.containsKey(stem)) {
					_termStemMap.put(stem, term);
				}
				newStemFreq = get(stem).intValue()+1;
				Integer newValueInt = Integer.valueOf(newStemFreq);
				super.put(stem, newValueInt);
				super.put(term, newValueInt);
			}
			
			else {
				_termStemMap.put(stem, term);
				super.put(stem, Integer.valueOf(newStemFreq));
				super.put(term, Integer.valueOf(newStemFreq));
			}
			
			if(newStemFreq > _maxFrequency) {
				_maxFrequency = newStemFreq;
			}
		}
	}


			/**
			 * <p>Add a new word (with its stem) with 1st character with undefined case  
			 * to the term frequency vector. Both the upper case and lower case version
			 * are added to the term frequency vector.</p> 
			 * @param word word with undefined case as first character.
			 * @param stem stemmed version of the word.
			 */
	public void putUnknownCase(final String word, final String stem) {
	
		String lWord = CStringUtil.convertFirstCharToLowerCase(word);
		this.put(lWord, stem);
		this.put(word, stem);
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for( String key : keySet()) {
			buf.append(key);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(get(key));
			buf.append("\n");
		}
		
		return buf.toString();
	}
}


// -----------------------------  EOF -----------------------------------

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.string;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;



			/**
			 * <p>Extract valid words (Dictionary) from a sequence of characters. This algorithms retains only
			 * the largest term that matches a Dictionay entry. For instance 'Verygoodwatermelon' generates for 
			 * part of the speech:  'Very good watermelon' not 'Very good water melon'. 
			 * It is assumed the dictionary is loaded from a file or database table into a hash table</p>
			 * @author Patrick Nicolas
			 * @date 09/12/2011
			 */
public class CStringBreakdown {
	protected List<String> _terms = null;
	
	protected Map<String, Integer> _dictionary = null;
	
	public CStringBreakdown(final Map<String, Integer> dictionary) {
		_dictionary = dictionary;
		_terms = new LinkedList<String>();
	}
	
	public final List<String> getList() {
		return _terms;
	}
	
	
			/**
			 * <p>Recursive call to find a valid term (Dictionary) in the remaining 
			 * characters of the input string.</p>
			 * @param source input character string.
			 */
	public void findWords(final String source) {
				/*
				 * If the input has a valid definition in the dictionary..
				 */
		if( _dictionary.containsKey(source) ) {
			_terms.add(source);
		}
				/*
				 * otherwise attempt to extract the largest valid 
				 */
		else {
			String firstCharacters = null;
			int index = -1;
			int j = 0;
				/*
				 * Walk through the input character string to find valid substring 
				 * which have a dictionary entry.
				 */
			for( ; j < source.length(); j++) {
				firstCharacters = source.substring(0, j);
				if( _dictionary.containsKey(firstCharacters) ) {
					index = j;
				}
			}
				/*
				 * Find the largest valid word in the remaining input
				 */
			if( index != -1 ) {
				_terms.add(source.substring(0, index));
				findWords(source.substring(index, source.length()));
			}
		}
	}
}

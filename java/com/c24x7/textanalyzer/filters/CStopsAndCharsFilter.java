// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.filters;



		/**
		 * <p>Class that configure the filter for eliminate non-stops words or words which 
		 * have characters other than letters. This filter is implemented as a singleton.</p>
		 * @author Patrick Nicolas
		 * @date 05/23/2011
		 * @see com.c24x7.nlservices.textanalyzer.filters.AFilter
		 */
public final class CStopsAndCharsFilter extends CStopsFilter {
	
	private static CStopsAndCharsFilter filter = new CStopsAndCharsFilter();
	
		/**
		 * <p>Return the Singleton for this filter</p>
		 * @return singleton for this filter.
		 */
	public static CStopsAndCharsFilter getInstance() {
		return filter;
	}
	
	/**
	 * <p>Return the Singleton for this filter</p>
	 * @return singleton for this filter.
	 */
	public static CStopsAndCharsFilter getInstance(int minWordLength) {
		filter._minWordLength = minWordLength;
		return filter;
	}

	
	private CStopsAndCharsFilter() {
		super();
	}
	
	

		/**
		 * <p>Apply filter to terms according to its character(letter,
		 * alpha-numeric, special characters,..) as well as its length.</p>
		 * @param word term for which the characters are evaluated.
		 * @return true if the word or token pass the filter.
		 */
	 @Override
	 protected boolean validateChars(final String term) {
		boolean containsOnlyLetter = true;
		
		if( term.length() >= _minWordLength ) {
			char[] chars = term.toCharArray();
			
			for( int j = 0; j < chars.length; j++) {
				if(!Character.isLetter(chars[j])) {
					containsOnlyLetter = false;
					break;
				}
			}
		}
		else {
			containsOnlyLetter = false;
		}
		return containsOnlyLetter;
	 }
}

// --------------------------  EOF ----------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.filters;




		/**
		 * <p>Generic class that validate that a string has a set of 
		 * allowed character filter by a regular expression. The regular
		 * expression has to be specified in the constructor. The default
		 * constructor implements the regular expression to allow letters,
		 * digit and ' (possessive character).
		 * @author Patrick Nicolas
		 * @date 12/09/2011
		 */

public final class CPatternFilter extends AFilter {
	public static final String DEFAULT_REG_EXP = "([A-Za-z0-9'-_.])*"; 
	
	protected String _regExp = DEFAULT_REG_EXP;
	
		/**
		 * <p>Create an instance of a Character filter for which letters,
		 * digit and possessive characters are allowed.</p>
		 */
	public CPatternFilter() {
		this(DEFAULT_REG_EXP);
	}
	
	
		/**
		 * <p>Create an instance of a Character filter with a user defined
		 * regular expression</p>
		 * @param regExp  User defined regular expression.
		 */
	public CPatternFilter(final String regExp) {
		_regExp = regExp;
	}
	
		/**
		 * <p>
		 * Main method used to filter out a word or term according to 
		 * a regular expression defined by the constructor. </p>
		 * @param word term to filter out.
		 * @return true if word meets filter criteria, false otherwise
		 */
	public String qualify(String word) {
		return (word != null && word.matches(_regExp)) ? word : null;
	}
}

// --------------------------  EOF -------------------------------------
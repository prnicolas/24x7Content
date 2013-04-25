// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.filters;

		/**
		 * <p>Interface for word filters.</p>
		 * @author Patrick Nicolas
		 * @date 11/18/2011
		 */
public abstract class AFilter {
		/**
		 * <p>
		 * Main method used to filter out a word or term according to 
		 * a regular expression or a dictionary. </p>
		 * @param word term to filter out.
		 * @return true if word meets filter criteria, false otherwise
		 */
	public abstract String qualify(String word);
	public String stem() { return null; }
	public int getQuotation() { return -1; }
}

// ------------------------------  EOF ---------------------------------------
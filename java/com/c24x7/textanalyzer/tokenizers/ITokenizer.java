// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tokenizers;

import java.util.List;


			/**
			 * <p>Generic interfaces for tokenizers used in the library.</p>
			 * @author Patrick Nicolas         24x7c 
			 * @date October 19, 2011 9:05:18 PM
			 */
public interface ITokenizer {

	/**
	 * <p>Main method to tokenize a input text into words</p>
	 * @param inputText  source to be split into words
	 * @return words or tokens including stop words.
	 */	
	public List<String> tokenize(final String inputText);
}

// ---------------------------  EOF ------------------------------------
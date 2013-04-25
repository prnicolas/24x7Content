// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tokenizers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.c24x7.textanalyzer.filters.AFilter;



/**
 * <p>
 * Default words tokenizer that relies on a predefined regular expression to break out sentences or<br> 
 * paragraphs. The methods in this class are not thread safe</p>
 * @author Patrick Nicolas
 */
public final class SCRegExpTokenizer implements ITokenizer {
	public static final String REG_EXP_WORDS_ONLY  = "(\\w{2,16}\\-\\w{2,16})|\\w{2,16}";
	protected AFilter _filter = null;
	
	protected static SCRegExpTokenizer tokenizer = null;
	
	/**
	 * <p>
	 * Create a words tokenizer using the default regular expression which
	 * is white spaces and SP characters.
	 * </p>
	 */
	public static SCRegExpTokenizer getInstance() {
		return getInstance(null);
	}
	
	/**
	 * <p>
	 * Create a words tokenizer using the default regular expression which
	 * is white spaces and SP characters.
	 * </p>
	 */
	public static SCRegExpTokenizer getInstance(final AFilter filter) {
		if( tokenizer == null ) {
			tokenizer = new SCRegExpTokenizer(filter);
		}
		return tokenizer;
	}

	/**
	 * <p>
	 * Main method to tokenize a input text into words using the default regular expression defined in the<br> 
	 * variable <b>regExpression</b>. This implementation is very fast as it does
	 * not rely on the Java String and StringTokenizer classes</p>
	 * @param inputText  source to be split into words
	 * @return words including stop words.
	 */
	public List<String> tokenize(final String inputText) {
		List<String> tokensList = new LinkedList<String>();
		
		Pattern p = Pattern.compile(REG_EXP_WORDS_ONLY);
		Matcher m = p.matcher(inputText);
		int start = 0;
	
		while(m.find(start)) {
			if(_filter == null) {
				String word = _filter.qualify(m.group());
				if( word != null) {
					tokensList.add(m.group());
				}
			}
			start = m.end();
		}
		return tokensList;
	}
	
	

	protected SCRegExpTokenizer(final AFilter filter) { 
		_filter = filter;
	}

}
// ---------------------  EOF -----------------------------------------
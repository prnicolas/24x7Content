// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.textanalyzer;



import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * Default words tokenizer that relies on a predefined regular expression to break out sentences or<br> 
 * paragraphs. The methods in this class are not thread safe</p>
 * @author Patrick Nicolas
 */
public final class CWordsTokenizer {
	public static final String REG_EXP_WORDS_ONLY  = "(\\w{2,12}\\-\\w{2,12})|\\w{2,12}";
	
	private String _regExpression = REG_EXP_WORDS_ONLY;
	
	/**
	 * <p>
	 * Create a words tokenizer using the default regular expression which
	 * is white spaces and SP characters.
	 * </p>
	 */
	public CWordsTokenizer() { }

	/**
	 * <p>
	 * Create a words tokenizer using the default regular expression which
	 * filtering out words having a range of characters
	 * </p>
	 * @param minNumChars  minimum number of characters for words to be selected
	 * @param maxNumChars  maximum number of characters for words to be selected
	 */
	public CWordsTokenizer(final int minNumChars, final int maxNumChars) {
		StringBuilder buf = new StringBuilder("(\\w{");
		buf.append(String.valueOf(minNumChars));
		buf.append(",");
		buf.append(String.valueOf(maxNumChars));
		buf.append("}\\-\\w{");
		buf.append(String.valueOf(minNumChars));
		buf.append(",");
		buf.append(String.valueOf(maxNumChars));
		buf.append("})|\\w{");
		buf.append(String.valueOf(minNumChars));
		buf.append(",");
		buf.append(String.valueOf(maxNumChars));
		buf.append("}");
		
		_regExpression = buf.toString();
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
	
		List<String> matchList = new ArrayList<String>();
	 			
		Pattern p = Pattern.compile(_regExpression);
		Matcher m = p.matcher(inputText);
		int start = 0;
	
		while(m.find(start)) {
			matchList.add(m.group());
			start = m.end();
		}
	
		return matchList;		
	}

	/**
	 * <p>
	 * Generate a textual representation of this object
	 * </p>
	 * @return  Description of this class attributes
	 */
	@Override
	public String toString() {
		String className = this.getClass().getName();	
		StringBuilder builder = new StringBuilder(className);
		builder.append("Base class: ");
		builder.append(super.toString());
		builder.append("regular expression: ");
		builder.append(_regExpression);

		return builder.toString();
	}
}
// ---------------------  EOF -----------------------------------------
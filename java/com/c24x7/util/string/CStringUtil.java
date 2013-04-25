// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.string;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;



			/**
			 * <p>Generic class that encapsulate basic character string manipulation
			 * used in 24x7 Content platform.</p>
			 * @author Patrick Nicolas
			 * @date 07/07/2011
			 */
public final class CStringUtil {
	protected final static char UNDERSCORE_CHAR = '_';


		
	
			/**
			 * <p>Convert a character to lower case. If the character is already in lower case
			 * then no change is performed.</p>
			 * @param character character to modify
			 * @return lower case character.
			 */
	public static char charToLowerCase(final char character) {
		char newCharacter = character;
		int firstHexChar = (int)character;
		
		/*
		 * If this is a upper case, convert to lower case.
		 */
		if( firstHexChar > 0x40 && firstHexChar < 0x5B) {
			newCharacter = (char)(firstHexChar + 0x20);
		}
		
		return newCharacter;
	}
	
	
		/**
		 * <p>Reformat a sentence by converting the first character as lower
		 * case and removing the last period.</p>
		 * @param sentence original sentence
		 * @return updated sentence.
		 */
	public static String stripSentence(final String sentence) {
		if( sentence == null) {
			throw new IllegalArgumentException("Cannot strip an undefined sentence");
		}
				
		char[] charsSeq = sentence.toCharArray();
		int numChars = charsSeq.length;
		char lastChar = charsSeq[charsSeq.length-1];
		
		/*
		 * Remove the last trailing character for the sentence in 
		 * order to extract the correct stem from the last word
		 * or N-Gram in the sentence.
		 */
		if( lastChar=='.' || lastChar=='?' || lastChar=='!') {
			numChars--;
		}
		
		return String.valueOf(charsSeq, 0, numChars);
	}
	



	public static String allButFirstCharToLowerCase(final String term) {
		String convertedString = null;
		int hexChar = -1;
		char[] charsSeq = null;
		
		/*
		 * If this is a upper case, convert to lower case.
		 */
		for( int k = 1; k < term.length(); k++) {
			hexChar = (int)(term.charAt(k));
			if( hexChar > 0x40 && hexChar < 0x5B) {
				if( charsSeq == null) {
					charsSeq = term.toCharArray();
				}
				charsSeq[k] = (char)(hexChar + 0x20);
			}
		}
		
		if( charsSeq != null) {
			convertedString = String.valueOf(charsSeq);
		}

		return convertedString;

	}

	
			/**
			 * <p>Convert a first character of a word or string
			 * of words into a different case, (upper to lower and
			 * lower to upper case)
			 * @param term
			 * @return word or N-Grams with a different case, or null, or unchanged for non-letter
			 */
	public static String changeFirstCharCase(final String term) {
		String convertedString = null;
		int firstHexChar = (int)(term.charAt(0));
		
		/*
		 * If this is a upper case, convert to lower case.
		 */
		if( firstHexChar > 0x40 && firstHexChar < 0x5B) {
			char[] charsSeq = term.toCharArray();
			charsSeq[0] = (char)(firstHexChar + 0x20);
			convertedString = String.valueOf(charsSeq);
		}
		
		/*
		 * If this is a lower case, convert to upper case.
		 */
		else if( firstHexChar > 0x60 && firstHexChar < 0x7B) {
			char[] charsSeq = term.toCharArray();
			int hexChar = (int)charsSeq[0] - 0x20;
			charsSeq[0] = (char)hexChar;
			convertedString = String.valueOf(charsSeq);
		}
		else {
			convertedString = term;
		}
		
		return convertedString;
	}
	
	
	
	public static String convertFirstCharToLowerCase(final String term) {
		String convertedString = null;
		int firstHexChar = (int)(term.charAt(0));
		
		if( firstHexChar > 0x40 && firstHexChar < 0x5B) {
			char[] charsSeq = term.toCharArray();
			charsSeq[0] = (char)(firstHexChar + 0x20);
			convertedString = String.valueOf(charsSeq);
		}
		else {
			convertedString = term;
		}
		return convertedString;
	}
	
	public static String convertFirstCharToUpperCase(final String term) {
		String convertedString = null;
		char[] charsSeq = term.toCharArray();
			
		int hexChar = (int)charsSeq[0] - 0x20;
		if( hexChar < 0x41) { 
			convertedString = term;
		}
		else {
			charsSeq[0] = (char)hexChar;
			convertedString = String.valueOf(charsSeq);
		}
		return convertedString;
	}
	
	
				/**
				 * <p>Extract the first few words from a sentences within a maximum number of 
				 * characters.</p>
				 * @param text Original sentence or text
				 * @param maxCharacters maximum number of characters...
				 * @return
				 */
	public static String extractFirstWords(final String text, int maxCharacters) {
		String firstFewWords = null;
		
		if( text.length() > maxCharacters) {
			int lastBlankSpaceIndex = text.substring(0, maxCharacters).lastIndexOf(' ');
			StringBuilder buf= new StringBuilder( (lastBlankSpaceIndex != -1) ?
						      					  text.trim().substring(0, lastBlankSpaceIndex) :
						      					  text.substring(0, maxCharacters));
			buf.append(" ...");
			
			firstFewWords = buf.toString();			     
		}
		else {
			firstFewWords = text;
		}
		
		return firstFewWords;
	}
	
	
				/**
				 * <p>Trim a string or word by removing both first and last blank character.</p>
				 * @param str original string
				 * @return trimmed string.
				 */
	public static String trim(String str) {
		str = str.trim();
		if(  str.charAt(0) == ' ') {
			str = str.substring(1, str.length());
		}
		return str;
	}
	
	
			/**
			 * <p>Convert an HTML text into a ASCII text by removing HTML tags.
			 * @param htmlText original HTML content
			 * @return ASCII formatted text
			 */
	public static String removeHtmlTags(final String htmlText) {
		String content = htmlText;

		int startTagIndex = content.indexOf("<");
		if( startTagIndex != -1) {
			int endTagIndex = -1;
			StringBuilder buf = new StringBuilder();

			while ( true ) {
				endTagIndex = content.indexOf(">");
				if(endTagIndex == -1) {
					break;
				}
				content = content.substring(endTagIndex+1);		
				startTagIndex = content.indexOf("<");
				if( startTagIndex == -1) {
					break;
				}
				buf.append(content.substring(0, startTagIndex));
			}
			content = buf.toString();
		}

		return content;
	}

	/**
	 * <p>Remove phonetic characters or string from a content</p>
	 * @param text input content
	 * @return content cleaned up from phonetic characters.
	 */
	public static String removePhonetic(final String text) {
		String content = text;

		int indexParenthesisOpen = text.indexOf("(");
		if( indexParenthesisOpen != -1) {
			if(text.charAt(indexParenthesisOpen+1) == '/' || text.charAt(indexParenthesisOpen+2) == '/') {
				int indexParenthesisEnd = text.indexOf(")");
				if( indexParenthesisEnd != -1) {
					StringBuilder buf = new StringBuilder(text.substring(0, indexParenthesisOpen));
					buf.append(text.substring(indexParenthesisEnd+2));
					content = buf.toString();
				}
			}
		}
		return content;
	}
	
	
	/**
	 * <p>Translate existing HTML code to ASCII.<p>
	 * @param htmlCodeText text 
	 * @return
	 */
	public static String convertHtmlToAscii(final String htmlCodeText) {
		String content = removeHtmlTags(htmlCodeText);
		// TO-DO:  HTMLParser Translate.decode(content)
		return content;
	}
	
	
				/**
				 * <p>Encode URL or input with latin characters (for HTTP GET and database).</p>
				 * @param input original content or text
				 * @return Latin1 encoded text.
				 */
	public static String encodeLatin1(final String input) {
		String result = null;
	
		try {
			result = URLEncoder.encode(input, "Latin1");
		}
		catch( UnsupportedEncodingException e){ }
		catch(Exception e) { }
		return result;
	}
	
			/**
			 * <p>Decode URL or input using latin characters (for HTTP GET and database).</p>
			 * @param input encoded URL or text
			 * @return ASCII string.
			 */
	public static String decodeLatin1(final String input) {
		String result = null;
		
		try {
			if( input != null) {
				result = URLDecoder.decode(input, "Latin1");
			}
		}
		catch( UnsupportedEncodingException e) {}
		catch(Exception e) { }
		return result;
	}
	
	
			/**
			 * <p>Decode URL or input using latin characters (for HTTP GET and database).</p>
			 * @param input encoded URL or text
			 * @return ASCII string.
			 */
	public static String decodeLatin1(final String input, final String delimiter) {
		String result = null;
		
		if( input != null ) {
			try {
		
					result = URLDecoder.decode(input, "Latin1");
				}
			catch(Exception e) { }
			
			if( result == null ) {
				int indexLastSeparator = input.lastIndexOf(delimiter);
				if( indexLastSeparator != -1) {
					String truncatedInput = input.substring(0, indexLastSeparator);
					try {
						result = URLDecoder.decode(truncatedInput, "Latin1");
					}
					catch(Exception e) { }
				}
			}
		}
	
		return result;
	}
	
	
			/**
			 * <p>Extract the first terms or word from the part of speech. The method generate an array
			 * of two strings, first terms and remaining terms...</p>
			 * @param partOfSpeech
			 * @return
			 */
	public static String[] getSplitFirstAndRemaining(String partOfSpeech) {
		String[] splitTerms = null;
		
		partOfSpeech = partOfSpeech.trim();
		int indexFirstSpace = partOfSpeech.indexOf(" ");
			/*
			 * If the part of speech is not a single term.... then 
			 * break it down in first term and remaining terms...
			 */
		if( indexFirstSpace != -1) {
			splitTerms = new String[] {
					partOfSpeech.substring(0, indexFirstSpace).trim(),
					partOfSpeech.substring(indexFirstSpace).trim()
			};
		}
		else {
			splitTerms = new String[] { partOfSpeech, null };
		}
		return splitTerms;
	}
	
	
	public static String getFirstSplit(final String content, final String delim) {
		String firstClass = null;
		
		if( content != null && delim != null) {
			int indexFirstDelim = content.indexOf(delim);
			firstClass = (indexFirstDelim == -1) ? content : content.substring(0,indexFirstDelim);
		}
		return firstClass;
	}
	
	
	/**
	 * <p>Method that implement the generic split function that supplement
	 * the default String.split method that does not allow returning
	 * an empty split.</p>
	 * @param content content to be split
	 * @param delim delimiter used for breakdown content
	 * @return list of segments delimited by the delimiter.
	 */
	public static List<String> split(final String content, final String delim) {
		List<String> splits = null;
		
		if( content != null && delim != null ) {
			splits = new LinkedList<String>();
			String cursor = content;
			int delimLen = delim.length();
			int indexDelim = -1;
			
			while( true ) {
				indexDelim = cursor.indexOf(delim);
				if(indexDelim == -1) {
					splits.add(cursor);
					break;
				}
				splits.add(cursor.substring(0, indexDelim));
				cursor = cursor.substring(indexDelim + delimLen);
			}
		}
		
		return splits;
	}
	
	
	public static String convertClassesToLineages(final String[] taxonomyClasses) {
		if( taxonomyClasses == null ) {
			throw new IllegalArgumentException("Cannot convert undefined taxonomy classes");
		}
		
		int lastIndex = taxonomyClasses.length -1;
		
		StringBuilder buf = new StringBuilder();
		for( int j = 0; j < lastIndex; j++) {
			buf.append(taxonomyClasses[j]);
			buf.append("/");
		}
		buf.append(taxonomyClasses[lastIndex]);
		
		return buf.toString();
	}
	
	
			/**
			 * <p>Convert space characters by underscore characters. This method is used in 
			 * creating id or handle name from common expression.</p>
			 * @param content original content with space characters
			 * @return string or identifiers with underscore characters
			 */
	public static String replaceSpaceUnderscoreChar(String content) {
		return content.replace(' ', UNDERSCORE_CHAR);
	}
	
			/**
			 * <p>Convert underscore characters by space characters.</p>
			 * @param content  identifier that use underscore characters.
			 * @return expression with space or blank characters.
			 */
	public static String replaceUnderscoreSpaceChar(String content) {
		return content.replace(UNDERSCORE_CHAR, ' ');
	}
}

// ---------------------  EOF -----------------------------------------

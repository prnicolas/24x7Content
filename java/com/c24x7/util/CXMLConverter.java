// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.util;


		/**
		 * <p>Convert AContent object to XML and vice versa</p>
		 * @author Patrick Nicolas
		 * @date 12/22/2010
		 */
public final class CXMLConverter {
	public final static String ERROR_TAG 	= "<error>";
	public final static String TEST_TAG 	= "<test>";
	public final static String SETUP_TAG 	= "<setup>";
			
	public final static String SEED_TAG 	= "<seed>";
	public final static String CONTENT_TAG 	= "<content>";
	public final static String TITLE_TAG 	= "<title>";
	public final static String DESC_TAG		= "<desc>";
	public final static String SELECT_TAG	= "<select>";
	
	public final static String PUBLISH_TAG 	= "<publish>";
	public final static String SOCIAL_TAG 	= "<social>";
	public final static String TWITTER_TAG 	= "<twitter>";
	public final static String HEAD_TAG 	= "<head>";
	public final static String TWEET_TAG 	= "<tweet>";
	public final static String LINK_TAG 	= "<link>";
	public final static String WEB_TAG		= "<web>";
	public final static String BODY_TAG		= "<body>";
	public final static String EMAIL_TAG	= "<email>";
	public final static String TO_TAG		= "<to>";
	public final static String SUBJECT_TAG	= "<subject>";
	public final static String FACEBOOK_TAG	= "<facebook>";
	public final static String STATUS_TAG	= "<status>";
		
				/**
				 * <p>Create an XML string with a tag and value.</p>
				 * @param tag tag to be used in the XML string
				 * @param value value of the string 
				 * @return XML representation
				 */
	public static String put(final String tag, final String value) {
		StringBuilder buf = new StringBuilder(tag);
		buf.append(value);
		buf.append(endTag(tag));
		
		return buf.toString();
	}
	
	/**
	 * <p>Extract the value from a single XML.</p>
	 * @param tag tag to be used in the XML string
	 * @param raw XML representation of the field
	 * @return Value of the field
	 */
	public static String get(final String tag, final String raw) {
		int tagLen = tag.length();
		return raw.substring(tagLen, raw.length()-tagLen-1);
	}
	
	/**
	 * <p>Extract the value from a sequence of XML fields.</p>
	 * @param tags array of tags to extract value from
	 * @param raw XML representation of the field
	 * @return array of values of the field
	 */
	public static String[] get(final String[] tags, final String raw) {
		String[] elements = new String[tags.length];
		for( int j = 0; j < tags.length; j++) {
			elements[j] = raw.substring(raw.indexOf(tags[j])+ tags[j].length(), 
					                    raw.indexOf(endTag(tags[j])));
		}
		
		return elements;
	}
	
	private static String endTag(final String tag) {
		return tag.replace("<", "</");
	}
}

// --------------------------  EOF ------------------------------

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.string;


		/**
		 * <p>Convert AContent object to XML and vice versa</p>
		 * @author Patrick Nicolas
		 * @date 12/22/2010
		 */
public final class CXMLConverter {
	public final static String ERROR_TAG 	= "<error>";
	public final static String TEST_TAG 	= "<test>";
	public final static String SETUP_TAG 	= "<setup>";
	public final static String SIGNUP_TAG 	= "<signup>";		
	public final static String SEED_TAG 	= "<seed>";
	public final static String CONTENT_TAG 	= "<content>";
	public final static String TITLE_TAG 	= "<title>";
	public final static String DESC_TAG		= "<desc>";
	public final static String SELECT_TAG	= "<select>";
	
	public final static String PUBLISH_TAG 	= "<publish>";
	public final static String GSEED_TAG 	= "<gseed>";
	public final static String TARGETS_TAG 	= "<targets>";
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
	public final static String ADDRESS_TAG 	= "<address>";
	public final static String SERVER_TAG 	= "<server>";
	
	public final static String MSG_TAG		= "<msg>";
	public final static String BLOG_TAG 	= "<blog>";
	public final static String FACEBOOK_TAG	= "<facebook>";
	public final static String STATUS_TAG	= "<status>";
	public final static String USER_TAG		= "<user>";
	public final static String PASSWORD_TAG = "<password>";
	public final static String TOKEN_TAG 	= "<token>";
	public final static String TOKEN_SECRET_TAG = "<tokensecret>";
		
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
		String extracted = null;
	
		int indexTagStart = raw.indexOf(tag);
		String endTagStr = endTag(new String(tag));
		int indexTagEnd = raw.indexOf(endTagStr);
		if( indexTagStart != -1 && indexTagEnd != -1) {
			extracted = raw.substring(indexTagStart + tag.length(), indexTagEnd);
		}
		
		return extracted;
	}
	
	/**
	 * <p>Extract the value from a sequence of XML fields.</p>
	 * @param tags array of tags to extract value from
	 * @param raw XML representation of the field
	 * @return array of values of the field
	 */
	public static String[] get(final String[] tags, final String raw) {
		if( tags == null || raw == null || raw.length() < 2) {
			throw new IllegalArgumentException("Malformed XML request"); 
		}
		
		String[] elements = new String[tags.length];
		
		if( elements != null ) {
			int indexTag = -1;
			for( int j = 0; j < tags.length; j++) {
				indexTag = raw.indexOf(tags[j]);
				elements[j] = (indexTag != -1 )?
						       raw.substring(raw.indexOf(tags[j])+ tags[j].length(), 
					                    	raw.indexOf(endTag(tags[j]))) :
					           null;
			}
		}
	
		return elements;
	}
	
	protected static String endTag(final String tag) {
		return tag.replace("<", "</");
	}
}

// --------------------------  EOF ------------------------------

// Copyright (c) 2010-2011 Patrick Nicolas
package com.c24x7.clients.rss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



		/**
		 * <p>RSS XML Feed container reader with the following header fields:
		 * <ul><li>Title</li><li>Description</li><li>copyright</li><li>published date</li></ul>
		 * and the list of message of type <b>RSSMessage</b></p>
		 * @author Patrick Nicolas
		 */

public final class CRSSFeed extends CRSSItem {
	public static final String LANGUAGE 	= "language";
	public static final String COPYRIGHT 	= "copyright";
	public static final String PUB_DATE 	= "pubDate";
	public static final String LINK 		= "link";

	private String _language 	= null;
	private String _copyright 	= null;
	private String _pubDate 	= null;
    private String _link		= null; 
	private List<CRSSMessage> _entries = new ArrayList<CRSSMessage>();

	
		/**
		 * <p>Constructor for RSS XML container used for generating feed</p>
		 * @param title  title of the feed
		 * @param description description of the feed
		 * @param link link of the feed
		 * @param language language used in the feed
		 * @param copyright copyright of source of the feed
		 * @param pubDate published date
		 */
	public CRSSFeed(final String title, 
					final String description, 
					final String link, 
					final String language,
					final String copyright,
					final String pubDate) {
		
		super(title, description);
		_language = language;
		_copyright = copyright;
		_link = link;
		_pubDate = pubDate;
	}
	
		/**
		 * <p>Constructor for the RSS XML feed used in parsing feeds</p>
		 * @param attrMap map of Feed attributes.
		 */
	public CRSSFeed(Map<String, String> attrMap) {
		super(attrMap);
		_language = attrMap.get(LANGUAGE);
		_copyright = attrMap.get(COPYRIGHT);
		_link = attrMap.get(LINK);
		_pubDate = attrMap.get(PUB_DATE);
	}

		/**
		 * <p>Retrieve list of RSS messages contained in this feed</p>
		 * @return list of RSS messages
		 */
	public final List<CRSSMessage> getMessages() {
		return _entries;
	}

		/**
		 * <p>Retrieve the language used in this feed</p>
		 * @return language
		 */
	public final String getLanguage() {
		return _language;
	}

		/**
		 * <p>Retrieve the copyright disclaimer of the source of the feed</p>
		 * @return copyright
		 */
	public final String getCopyright() {
		return _copyright;
	}

	/**
	 * <p>Retrieve the published date of this feed</p>
	 * @return published date
	 */
	public final String getPubDate() {
		return _pubDate;
	}
	
		/**
		 * <p>Retrieve the link to the source of this feed</p>
		 * @return
		 */
	public final String getLink() {
		return _link;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Feed [");
		toString(buf);
		if( _copyright != null) {		
			buf.append(", copyright=");
			buf.append(_copyright);
		}
		if( _language != null) {
			buf.append(", language=");
			buf.append(_language);
		}
		if( _link != null) {
			buf.append(", link=");
			buf.append(_link);
		}
		if( _pubDate != null) {
			buf.append(", pubDate=");
			buf.append(_pubDate);
		}
		buf.append("]");
		
		return buf.toString();
	}

}

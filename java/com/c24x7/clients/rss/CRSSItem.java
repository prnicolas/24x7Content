// Copyright (c) 2010-2011 Patrick Nicolas
package com.c24x7.clients.rss;

import java.util.Map;

		/**
		 * <p>Basic class for RSS message and Feed container</p>
		 * @author Patrick Nicolas
		 */
public class CRSSItem {
	public static final String TITLE 		= "title";
	public static final String DESCRIPTION 	= "description";

	
	protected String _title			= null;
	protected String _description	= null;

    
    	/**
    	 * <p>Constructor for this RSS feed item used in generating feed</p>
    	 * @param title title of the message or container
    	 * @param description description of the message or container
    	 */
    public CRSSItem(final String title, final String description) {
    	_title = title;
    	_description = description;
    }
    
    	/**
    	 * <p>Constructor for this RSS feed item used in parsing feeds</p>
    	 * @param attrMap map of feed attributes
    	 */
    
    public CRSSItem(Map<String, String> attrMap) {
    	_title	= attrMap.get(TITLE);
    	_description = attrMap.get(DESCRIPTION); 
    }
    
    	/**
    	 * <p>Retrieve the title of this RSS feed item</p>
    	 * @return title of the RSS item
    	 */
    public final String getTitle() {
		return _title;
	}
    	/**
    	 * <p>Retrieve the description of this RSS feed item</p>
    	 * @return description
    	 */
	public final String getDescription() {
		return _description;
	}
	public void setDescription(final String description) {
		_description = description;
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Msg{");
		toString(buf);
		buf.append(_title);
		buf.append("}");
		
		return buf.toString();
	}
	
	protected void toString(StringBuilder buf) {
		if(_title != null) {
			buf.append("title=");
			buf.append(_title);
		}
		if( _description != null) {
			buf.append(", description=");
			buf.append(_description);
		}
	}
}


// -------------------------------  EOF ---------------------------
// Copyright (c) 2010-2011 Patrick Nicolas
package com.c24x7.clients.rss;


import java.util.Map;

		/**
		 * <p>Basic class that encapsulates a XML-formatted RSS feed message</p>
		 * @author Patrick Nicolas 
		 *
		 */
public final class CRSSMessage extends CRSSItem {

	public static final String AUTHOR 		= "author";
	public static final String GUID 		= "guid";
	public static final String ORIG_LINK 	= "origLink";

    private String _author		= null;
    private String _guid		= null;
    private String _origLink 	= null;
    
    
	public CRSSMessage(	final String title, 
						final String description, 
						final String origLink,
						final String author,
						final String guid) {

		super(title, description);
		_author = author;
		_guid = guid;
		_origLink = origLink;
	}
    
    public CRSSMessage(Map<String, String> attrMap) {
    	super(attrMap);
    	_author	= attrMap.get(AUTHOR);
        _guid 	= attrMap.get(GUID);
        _origLink = attrMap.get(ORIG_LINK);
    }
    
	
	public final String getAuthor() {
		return _author;
	}

	public final String getGuid() {
		return _guid;
	}
	public final String getOrigLink() {
		return _origLink;
	}

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Msg{");
		toString(buf);
		if(_author != null) {
			buf.append(", author=");
			buf.append(_author);
		}
		if( _origLink != null) {
			buf.append(", origLink=");
			buf.append(_origLink);
		}
		buf.append("}");
		
		return buf.toString();
	}
}

// ------------------------  EOF ---------------------------
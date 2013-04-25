// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;




			/**
			 * <p>Defines a model for the document to be analyzed and augmented..The components
			 * of the document model are</p>
			 * <ul>
			 * <li>Title</li>
			 * <li>Original Content</li>
			 * <li>Analysis metrics</li>
			 * <li>Social targets</li>
			 * <li>Media content such as maps, images, videos, Wikipedia entries</li>
			 * </ul>
			 * @author Patrick Nicolas
			 * @date 10/23/2011
			 */
public final class CSummaryModel extends AModel {
	private String	_title 		= null;
	private String 	_abstract	= null;
	private String 	_url 		= null;
	private String	_content 	= null;
	private CContentItemsMap 	_contentMap 	= null;
	
	
			/**
			 * <p>Create an object document with a specific title and content.</p>
			 * @param title title of the document if provided..
			 * @param content content of the document (mandatory)
			 */

	public CSummaryModel(final String title, final String content) {
		_title = title;
		_content = content;
		_contentMap = new CContentItemsMap();
	}


	public final String getTitle() {
		return _title;
	}

	public final CContentItemsMap getContentItemsMap() {
		return _contentMap;
	}
	
	public final String getAbstract() {
		return _abstract;
	}
	
	public final String getUrl() {
		return _url;
	}
	
	public final String getContent() {
		return _content;
	}


	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("\nModel for ");
		buf.append(_url);
		buf.append("\nContent:\n");
		buf.append(_content);
		if( _contentMap != null ) {
			buf.append(_contentMap.toString());
		}
		
		return buf.toString();
	}
	
}

// ---------------------  EOF ---------------------------------------------

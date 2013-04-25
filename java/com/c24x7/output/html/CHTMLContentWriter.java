//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;

			/**
			 * <p>Class that create the dynamic structures for representing content.</p>
			 * @author Patrick Nicolas
			 * @date 07/06/2011
			 */
public class CHTMLContentWriter extends AHTMLSectionWriter {
	protected static final String CONTENT_SECTION_ICON 	= "edit.png";
	protected static final String CONTENT_SECTION_TITLE 	= "Content";
	protected static final String CONTENT_SECTION_ID 		= "content-section";
	
	protected static class NCompare implements Comparator<Integer> {
		public int compare(Integer o1,Integer o2) {
	    	return o1.compareTo(o2);
	    }
	}
	
	public CHTMLContentWriter() {
		super(CONTENT_SECTION_ICON, CONTENT_SECTION_TITLE);
		_sectionId = CONTENT_SECTION_ID;
	}
	

	
			/**
			 * <p>Create a HTML document related to this model.</p>
			 * @param model
			 */
	/*
	public void writexx(final AModel model) {
		_htmlBuffer.append("\nvar title_str = null;");
		_htmlBuffer.append("\nvar content_str = null;");
		_htmlBuffer.append("\nvar keywords_array = null;");
		_htmlBuffer.append("\nfunction Document_content(title, content, keywords) { \nthis.title = title; \nthis.content = content; \nthis.keywords = keywords;\n}");
        
        CContentItemsMap itemsMap = model.getContentItemsMap();
        if ( itemsMap != null && itemsMap.size() > 0 ) {
        	_htmlBuffer.append("\nkeywords_array = [];");
        
        	for(String keyword : itemsMap.keySet()) {
        		_htmlBuffer.append("\nkeywords_array.push(\"");
        		_htmlBuffer.append(keyword);
        		_htmlBuffer.append("\");");
        	}
        }
        
        _htmlBuffer.append("\ncontent_str=");
   
        _htmlBuffer.append("\"<span style=\'background-color:#EEEEEE;color:#333333;font-family:Ubuntu, Helvetica, Tahoma, sans-serif;font-size:12px\'>\"");
        _htmlBuffer.append(" + ");
        writeContentxx(model);
        _htmlBuffer.append(" + ");
        _htmlBuffer.append("\"</span><br>\";");
   
        _htmlBuffer.append("\n var doc1 = new Document_content(title_str, content_str, keywords_array);");
	}
	*/
	
	
				// -----------------------------
				// Supporting Private Methods
				// ----------------------------
	

	
	public void writeInternal(final CSummaryModel model) {
		CContentItemsMap itemsMap = model.getContentItemsMap();
		String content = model.getContent();
		
				/*
				 * Sort the map of keywords by the index of those keywords 
				 * in the original content
				 */
		SortedMap<Integer, String> keywordsMap = sort(model);
			
		Iterator<Integer> iterator = keywordsMap.keySet().iterator();	
		int indexKeyword = -1;
		String nextKeyword = null;
		String subString = null;
		int k = 0, cursor = 0;
		NContentItem contentItem = null;
				
		_htmlBuffer.append("\n<div class=\"canvas-info-layout\">\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n<span style=\"color:#333333;font-family:Ubuntu, Helvetica, Tahoma, sans-serif;font-size:12px\">");

	    while (iterator.hasNext()) {
	    	Integer key = iterator.next();	
	    	indexKeyword = key.intValue();
	    	
	    	if( indexKeyword >=0 ) {
	    		nextKeyword = keywordsMap.get(key);
	    		contentItem = itemsMap.get(nextKeyword);
	     	 	
	    		try {
	    			subString = content.substring(cursor, indexKeyword);
	    			_htmlBuffer.append(subString.length() > 1 ? subString : " ");
	  
	    			if( !contentItem.isMisspelled() ) {
	    				String keyword_id = CStringUtil.replaceSpaceUnderscoreChar(nextKeyword);
	    				_htmlBuffer.append("<a id=\"k_");
	    				_htmlBuffer.append(keyword_id);
	    				_htmlBuffer.append("\" href=\"javascript:void(0)\" class=\"highlight\" onMouseOver=\"view_summary(\'");
	    				_htmlBuffer.append(keyword_id);
	    				_htmlBuffer.append("\', '");
	    				_htmlBuffer.append(nextKeyword);
	    				_htmlBuffer.append("\');\"");
	    				_htmlBuffer.append(" onMouseOut=\"hide_summary()\"");
	    				_htmlBuffer.append(" onClick=\"");
	    				_htmlBuffer.append("show_summary(\'d_");
	    				_htmlBuffer.append(keyword_id);
	    				_htmlBuffer.append("\', '");
	    				_htmlBuffer.append(nextKeyword);
	    				_htmlBuffer.append("\');\">");
	    				_htmlBuffer.append(nextKeyword);
	    				_htmlBuffer.append("</a>");
	    			}
	    			else {
	    				_htmlBuffer.append(nextKeyword);
	    			}
	    			cursor = indexKeyword + nextKeyword.length();
	    			k++;
	    		}
	    		catch( StringIndexOutOfBoundsException e) {
	    			CLogger.error(nextKeyword + " is not properly ordered");
	    		}
	    	}
		}
	    	/*
	    	 * Write the remaining content or terms after the last keywords.
	    	 */
	    subString = content.substring(cursor);
	    if( subString != null ) {
	    	_htmlBuffer.append(subString);
	    }
        _htmlBuffer.append("\n</span>\n</div><br>");
	}
	
	
	
	protected static SortedMap<Integer, String> sort(CSummaryModel model) {
		CContentItemsMap itemsMap = model.getContentItemsMap();
		String content = model.getContent();
		Set<String> keywords = itemsMap.keySet();
		
		SortedMap<Integer, String> keywordsMap = new TreeMap<Integer, String>(new NCompare());
		int indexKeyword = -1;
		for( String keyword : keywords) {
			indexKeyword = content.indexOf(keyword);
			if( indexKeyword != -1) {
				keywordsMap.put(new Integer(indexKeyword), keyword);
			}
		}
		
		return keywordsMap;
	}

}

// --------------------------------  EOF ---------------------------------

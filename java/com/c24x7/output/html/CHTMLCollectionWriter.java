//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.models.CSummaryModel;



public class CHTMLCollectionWriter extends AHTMLWriter {
	
	public CHTMLCollectionWriter(CHTMLStyle style) {
		super(style);
	}
					/**
					 * <p>Write a collection of keywords entries extracted from a model.</p>
					 * @param model model for the content analyzed...
					 * 
					 */
	public void write(final CSummaryModel model) {
		if( model != null ) {
			
			CContentItemsMap itemsMap = model.getContentItemsMap();
			if( itemsMap != null ) {
				NContentItem item = null;
				CHTMLKeywordWriter htmlWriter =  null;
		
						/*
						 * Walks through the list of keywords. The keywords writers are
						 * created as non visible.
						 */
		
				for( String key : itemsMap.keySet() ) {
					item = itemsMap.get(key);

					if( item  != null ) {
						htmlWriter =  new CHTMLKeywordWriter(_style, key);
						item.toHtml(htmlWriter);	
						_htmlBuffer.append(htmlWriter.toString());
					}
				}
			}
		}
	}
}

// --------------------  EOF ----------------------------------------
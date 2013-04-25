//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import com.c24x7.models.CSummaryModel;


			/**
			 * <p>Class that contains all the pieces of information of knowledge collected, 
			 * recorded and stored by the user.</p>
			 * @author Patrick Nicolas
			 * @date 09/25/2011
			 */
public final class CHTMLScrapBookWriter extends AHTMLSectionWriter {
	protected static final String CONTENT_SECTION_ICON 	= "bookmark.png";
	protected static final String CONTENT_SECTION_TITLE 	= "Scrapbook";
	protected static final String CONTENT_SECTION_ID 		= "scrapbook-section";

	
	public CHTMLScrapBookWriter() {
		super(CONTENT_SECTION_ICON, CONTENT_SECTION_TITLE);
		_sectionId = CONTENT_SECTION_ID;
	}
	
	public void writeInternal(final CSummaryModel model) {
	    _htmlBuffer.append("\n<div id=\"scrapbook-content\" class=\"canvas-info-layout\" style=\"min-height:180px;height:auto\" >\n</div>");
	}
}

// ------------------------- EOF ------------------------------------------
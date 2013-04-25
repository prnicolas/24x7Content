package com.c24x7.output.html;

import com.c24x7.models.CSummaryModel;


public class CHTMLAnalysisWriter extends AHTMLSectionWriter {
	protected static final String CONTENT_SECTION_ICON 	= "zoom.png";
	protected static final String CONTENT_SECTION_TITLE 	= "Analysis";
	protected static final String CONTENT_SECTION_ID 		= "analysis-section";
	
	public CHTMLAnalysisWriter() {
		super(CONTENT_SECTION_ICON, CONTENT_SECTION_TITLE);
		_sectionId = CONTENT_SECTION_ID;
		_sectionTitleWriter.setDescription("keyword-desc");
	}
	
	

	public void writeInternal(final CSummaryModel model) {
	    _htmlBuffer.append("\n<div id=\"keyword-section\" class=\"canvas-info-layout\" >\n</div>");
	}
}

// ---------------------------------  EOF ----------------------------------------------
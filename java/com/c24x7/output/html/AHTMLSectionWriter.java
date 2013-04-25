package com.c24x7.output.html;

import java.util.List;
import com.c24x7.models.CSummaryModel;


public abstract class AHTMLSectionWriter extends AHTMLWriter {
	protected CHTMLSectionTitleWriter _sectionTitleWriter = null;
	protected String  _sectionId = null;
	protected String  _sectionStyle = null;
	
	public AHTMLSectionWriter(final String icon, final String title) {
		super();
		_sectionTitleWriter = new CHTMLSectionTitleWriter(icon, title);
	}

	
	public void write(final List<CSummaryModel> modelsList) {
	    _htmlBuffer.append("\n<div id=\"");
	    _htmlBuffer.append(_sectionId);
	    		/*
	    		 * Create a style for this content section..
	    		 */
	    if(_sectionStyle != null) {
	    	 _htmlBuffer.append("\" class=\"canvas-section-layout\" style=\"");
	    	 _htmlBuffer.append(_sectionStyle);
	    	 _htmlBuffer.append("\" >");
	    }
	    else {
		    _htmlBuffer.append("\" class=\"canvas-section-layout\" >");
	    }
	    
	    _sectionTitleWriter.write(_sectionId);
		_htmlBuffer.append(_sectionTitleWriter.toString());
		for( CSummaryModel model : modelsList) {
			writeInternal(model);
		}
		_htmlBuffer.append("\n</div>");
	}
	
	
	public void write(final CSummaryModel model) {
	    _htmlBuffer.append("\n<div id=\"");
	    _htmlBuffer.append(_sectionId);
	    		/*
	    		 * Create a style for this content section..
	    		 */
	    if(_sectionStyle != null) {
	    	 _htmlBuffer.append("\" class=\"canvas-section-layout\" style=\"");
	    	 _htmlBuffer.append(_sectionStyle);
	    	 _htmlBuffer.append("\" >");
	    }
	    else {
		    _htmlBuffer.append("\" class=\"canvas-section-layout\" >");
	    }
	    
	    _sectionTitleWriter.write(_sectionId);
		_htmlBuffer.append(_sectionTitleWriter.toString());
		writeInternal(model);
		_htmlBuffer.append("\n</div>");
	}
	
	abstract public void writeInternal(final CSummaryModel model);
}

// ----------------------   EOF ------------------------------------------------------------
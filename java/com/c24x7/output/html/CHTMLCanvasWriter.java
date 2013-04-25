//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import java.util.List;

import com.c24x7.models.CSummaryModel;


			/**
			 * <p>Create a canvas to display the current text with highlight, analysis and scrapbook
			 * @author Patrick
			 *
			 */
public class CHTMLCanvasWriter extends CHTMLBlockWriter {
	
	public CHTMLCanvasWriter(CHTMLStyle style) {
		super(style);
	}
	
			/**
			 * <p>Write HTML document for the entire canvas...</p>
			 * @param model model of the document
			 */
	public void write(final List<CSummaryModel> modelsList) {
		_htmlBuffer.append("\n<div id=\"canvas_id\">");
		
			/*
			 * Create the content display section
			 */
		AHTMLSectionWriter sectionWriter = new CHTMLContentWriter();
		sectionWriter.write(modelsList);
		_htmlBuffer.append(sectionWriter.toString());   
		    
		/*
		sectionWriter = new CHTMLAnalysisWriter();
		sectionWriter.write(model);
		_htmlBuffer.append(sectionWriter.toString());  
		
		
		sectionWriter = new CHTMLScrapBookWriter();
		sectionWriter.write(model);
		_htmlBuffer.append(sectionWriter.toString());  
		*/
			
		_htmlBuffer.append("\n</div>");
	}
	
	
			/**
			 * <p>Write HTML document for the entire canvas...</p>
			 * @param model model of the document
			 */
	public void write(final CSummaryModel model) {
		_htmlBuffer.append("\n<div id=\"canvas_id\">");
	
			/*
			 * Create the content display section
			 */
	    AHTMLSectionWriter sectionWriter = new CHTMLContentWriter();
	    sectionWriter.write(model);
	    _htmlBuffer.append(sectionWriter.toString());   
		    
			/*
			 * Create the analysis display section
			 */
	    sectionWriter = new CHTMLAnalysisWriter();
	    sectionWriter.write(model);
	    _htmlBuffer.append(sectionWriter.toString());  
	    
			/*
			 * Create the HTML document for the scrapbook section
			 */
	    sectionWriter = new CHTMLScrapBookWriter();
	    sectionWriter.write(model);
	    _htmlBuffer.append(sectionWriter.toString());  
	    
//	    _htmlBuffer.append("\n<div id=\"share_div\" class=\"canvas-section-layout\">\n</div>");

	    _htmlBuffer.append("\n</div>");
	}
	
}

// ----------------------------  EOF -------------------------------------------------------
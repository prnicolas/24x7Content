//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;


import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.output.html.CHTMLStyle;



			/**
			 * <p>Class that manage the generation of HTML document for a keyword or
			 * part of speech.</p>
			 * @author Patrick Nicolas
			 * @date 07/08/2011
			 */
public class CHTMLKeywordWriter extends CHTMLSummaryWriter {

	protected CHTMLSummaryWriter 		_htmlSummaryWriter = null;
	protected CHTMLDescriptionWriter 	_htmlDescriptionWriter = null;
	
	/**
	 * <p>Create a HTML writer to create a HTML document for a keyword or part of speech 
	 * with a specific style.</p>
	 * @param style Style used in the creation of the keyword document.
	 * @param key keyword for which the HTML document is created.
	 */

	public CHTMLKeywordWriter(CHTMLStyle style, final String key) {
		super(style, key);
		_htmlSummaryWriter = new CHTMLSummaryWriter(style, key);
		_htmlDescriptionWriter = new CHTMLDescriptionWriter(style, key);
	}
	
	

				/**
				 * <p>Write the description of the keyword such as Semantics, Wikipedia, Maps, images or videos.</p>
				 * @param contentItem the content element or keyword that needs to be exposed.
				 */
	public void write(final NContentItem contentItem) {
		if( contentItem != null ) {
			_htmlBuffer.append("\n<div id=\"d_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" style=\"position:relative;display:none;background-color:#EEEEEE\" >");

			_htmlSummaryWriter.write(contentItem);
			_htmlBuffer.append(_htmlSummaryWriter.toString());
			_htmlDescriptionWriter.write(contentItem);
			_htmlBuffer.append(_htmlDescriptionWriter.toString());
			_htmlBuffer.append("\n<div id=\"AP_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" ></div>");
			_htmlBuffer.append("\n</div>");
		}
	}
	

	
	
	public void write(final String line) {
		_htmlBuffer.append(line);
	}

	
	public String toString() {
		return _htmlBuffer.toString();
	}
}

// -------------------------------  EOF -------------------------------------------------------

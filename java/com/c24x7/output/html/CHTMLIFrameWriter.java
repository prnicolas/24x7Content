//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import com.c24x7.models.AModel;



				/**
				 * <p>Class to create a pop window that relies on IFrame.</p>
				 * @author Patrick Nicolas
				 * @date 07/22/2011
				 */
public class CHTMLIFrameWriter extends CHTMLBlockWriter {
	public final static String DRAG_SCRIPT = "\n\n$(function() { \n$( \"#selected-image, #selected-video, #selected-text, #selected-summary\" ).draggable();\n$( \"#scrapbook-section\" ).droppable({\ndrop: function( event, ui ) {\nvar dragged_el = ui.draggable[0];\nselect_item();\nvar close_el=dragged_el.getElementsByTagName(\'a\');\nif(close_el != null && close_el.length >0) {\ndragged_el.removeChild(close_el[0]);\n}\nvar div_el = get_content_el(dragged_el.firstElementChild);\nif( div_el != null) {\nvar scrapbook_el = document.getElementById(\'scrapbook-content\');\nscrapbook_el.appendChild(div_el);\nscrapbook_object.expand(div_el);\nactivation_state=0;\nscrapbook_el.style.height = scrapbook_object.height + \'px\';\n position_footer();\n}\nwhile( dragged_el.hasChildNodes()) {\ndragged_el.removeChild(dragged_el.firstChild);\n}\ndragged_el.style.display = 'none';\n}\n})\n});";
	public final static String IFRAMECODE = "\n<div id=\"selected-image\" align=\"center\" class=\"popup-basic\" style=\"width:530px;\">\n</div>";
	public final static String IFRAMEVIDEO = "\n<div id=\"selected-video\" class=\"popup-basic\" ></div>";
	public final static String IFRAMETEXT = "\n<div id=\"selected-text\" align=\"left\" class=\"popup-basic\" style=\"width:320px;\"></div>";
	public final static String IFRAMESUMMARY = "\n<div id=\"selected-summary\" align=\"left\" class=\"popup-basic\" style=\"width:400px;font-size:12px;font-family:Ubuntu, Helvetica, Tahoma, sans-serif;color:#444444;\" ></div>";

	public CHTMLIFrameWriter(CHTMLStyle style) {
		super(style, 80, 40, 520);
	}
	
	public CHTMLIFrameWriter(CHTMLStyle style, int width) {
		super(style, 0, 0, width);
	}
	
	public CHTMLIFrameWriter(CHTMLStyle style, int top, int left, int width) {
		super(style, top, left, width);
	}
	
	public String getScript() {
		return DRAG_SCRIPT;
	}
	
			/**
			 * <p>Create a iframe component for display HTML page for a specific URL
			 * @param url url for the HTML page to display 
			 */
	public void write(final String url) {
		_htmlBuffer.append("\n<iframe src=\"");
		_htmlBuffer.append(url);
		_htmlBuffer.append("\" align=\"center\" width=\"");
		_htmlBuffer.append(_width);
		_htmlBuffer.append("\" height=\"400\"></iframe>");
	}
	
	
			/**
			 * <p>Create iFrame components to display images and videos.</p>
			 * @param model model of the document analyzed.
			 */
	public void write(final AModel model) {
		if( model != null ) {
			_htmlBuffer.append(IFRAMECODE);
			_htmlBuffer.append(IFRAMEVIDEO);
			_htmlBuffer.append(IFRAMETEXT);
			_htmlBuffer.append(IFRAMESUMMARY);
		}
	}
}

// -------------------------  EOF ------------------------------------------------------

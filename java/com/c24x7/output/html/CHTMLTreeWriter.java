//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.string.CStringUtil;



				/**
				 * <p>Class to generate a tree for organizing data.</p>
				 * @author Patrick Nicolas
				 * @date 08/13/2011
				 */
public class CHTMLTreeWriter extends CHTMLBlockWriter {

	public final static String  script = "\n\n$(document).ready(function() { \n$(\'#tree\').explrTree({ treeWidth : 230}); \nvar canvas_el = document.getElementById(\'canvas_id\'); \nif( canvas_el != null) { \n var leftVal = jQuery(\'#tree\').width() + 26;\n canvas_el.style.left =  leftVal + \'px\'; \n} \n} );";

	public CHTMLTreeWriter(CHTMLStyle style) {
		super(style);
	}
	

	public String getScript() {
		return script;
	}
	

	public void setTop(int top) {
		_top = top;
	}
	
	public void setLeft(int left) {
		_left = left;
	}
			/**
			 * <p>Create the JQUery tree from the model content.</p>
			 * @param model model of the document or content.
			 */
	public void write(final CSummaryModel model) {
		if( model != null ) {
			_htmlBuffer.append("\n<ul id=\"tree\" >\n<li class=\"icon-content\">\n<a href=\"javascript:void(0)\" onclick=\"show_content();\">");
			_htmlBuffer.append(_style.getHiliSpan("Content"));
			_htmlBuffer.append("</a></li>\n<li class=\"icon-analysis\">\n<a href=\"javascript:void(0)\" onclick=\"show_analysis();\">");
			_htmlBuffer.append(_style.getTextSpan("Analysis"));
			_htmlBuffer.append("</a>\n<ul>");		
	  
					/*
					 * Display the list of keywords and associated links...
					 */
			CContentItemsMap itemsMap = model.getContentItemsMap();
			if ( itemsMap.size() > 0 ) {
				NContentItem contentItem = null;
		
				for(String keyword : itemsMap.keySet()) {
					contentItem = itemsMap.get(keyword);
					
					if( !contentItem.isMisspelled() ) {
						_htmlBuffer.append("\n<li class=\"");
						_htmlBuffer.append("icon-right");
						_htmlBuffer.append("\"><a href=\"javascript:void(0)\" onclick=\"show_item(\'d_");
						_htmlBuffer.append(CStringUtil.replaceSpaceUnderscoreChar(keyword));
						_htmlBuffer.append("\', \'");
						_htmlBuffer.append(keyword);
						_htmlBuffer.append("\');\">");
						_htmlBuffer.append(_style.getHiliSpan(keyword));
						_htmlBuffer.append("</a></li>");
					}
				}
			}
	
			_htmlBuffer.append("\n</ul>\n</li>"); //
	
			_htmlBuffer.append("\n<li class=\"icon-select\" ><a href=\"javascript:void(0)\" onclick=\"show_scrapbook();\">Scrapbook</a>");
			_htmlBuffer.append("\n<ul id=\"sel_list\">");
			_htmlBuffer.append("\n</ul></li>");
	
			/*
			CTargetItemsMap targetsMap = model.getTargetItemsMap();
			if( targetsMap != null && targetsMap.size() > 0 ) {
				_htmlBuffer.append("\n<li class=\"icon-share\"><a href=\"javascript:void(0)\" onclick=\"show_share();\">Share</a>");
				_htmlBuffer.append("\n<ul>");
				for( String target : targetsMap.keySet() ) {
					_htmlBuffer.append("\n<li class=\"icon-");
					_htmlBuffer.append(target);
					_htmlBuffer.append("\"><a href=\"javascript:void(0)\" onclick=\"show_item(\'");
					_htmlBuffer.append(target);
					_htmlBuffer.append("\');\">");
					_htmlBuffer.append(target);
					_htmlBuffer.append("</a></li>");
				}
				_htmlBuffer.append("\n</ul></li>");
			}
			*/
			_htmlBuffer.append("\n</ul>");  //tree
		
		}
	}
}

// -----------------------  EOF ----------------------------------------------------
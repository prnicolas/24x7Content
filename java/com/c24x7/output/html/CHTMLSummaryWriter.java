//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.models.CContentItemsMap.NDbpediaRecord;
import com.c24x7.semantics.dbpedia.CDbpediaResources.NSummaryLabel;
import com.c24x7.util.string.CStringUtil;



			/**
			 * <p>Create the HTML document for the definition of the keyword.</p>
			 * @author Patrick Nicolas
			 * @date 08/13/2011
			 */
public class CHTMLSummaryWriter  extends AHTMLWriter {
	protected String 	_key 	= null;
	
	
			/**
			 * <p>Create a summary for the keyword or part of the speech</p>
			 * @param key keyword of part of the speech to be represented...
			 */
	public CHTMLSummaryWriter(String key) {
		super();
		_key = CStringUtil.replaceSpaceUnderscoreChar(key);
	}
	
	
		/**
		 * <p>Create a summary for the keyword or part of the speech with a specific HTML style</p>
		 * @param key keyword of part of the speech to be represented...
		 * @param style HTML style used to represent the keyword or part of the speech.
		 */

	public CHTMLSummaryWriter(CHTMLStyle style, final String key) {
		super(style);
		_key = CStringUtil.replaceSpaceUnderscoreChar(key);
	}
	
	/**
	 * <p>Initialize the keyword for this HTML document.
	 * @param key
	 */
	public void setKey(String key) {
		_key = CStringUtil.replaceSpaceUnderscoreChar(key);
	}


	public final String getKey() {
		return _key;
	}
	
	
	/**
	 * <p>Write the description of the keyword such as Semantics, Wikipedia, Maps, images or videos.</p>
	 * @param contentItem the content element or keyword that needs to be exposed.
	 */
	public void write(final NContentItem contentItem) {
			/*
			 * If this keyword was found as misspelled
			 */
		if( contentItem.isMisspelled() ) {
			_htmlBuffer.append(_style.getTextSpan("Misspelled"));
		}
		/*
		 * otherwise displays the content information for this keyword
		 * as a list of semantics definition, images, maps or videos..
		 */
		else {
			_htmlBuffer.append("\n<table width=\"100%\" >\n<tr>\n<td width=\"90\">");
			_htmlBuffer.append(writeSummaryKeyword("Semantics", 'S'));
			_htmlBuffer.append("<br>");
			_htmlBuffer.append(writeSummaryKeyword("Wikipedia", 'W'));
			_htmlBuffer.append("<br>");
			_htmlBuffer.append(writeSummaryKeyword("Maps", 'M'));
		    _htmlBuffer.append("<br>");
			_htmlBuffer.append(writeSummaryKeyword("Images", 'I'));
		    _htmlBuffer.append("<br>");
			_htmlBuffer.append(writeSummaryKeyword("Videos", 'V'));
			_htmlBuffer.append("<br>");
			_htmlBuffer.append(writeSummaryKeyword("References", 'Y'));
			_htmlBuffer.append("\n</td>");
	
				/*
				 * In  case of there is an entry in Wikipedia..
				 */
			if(contentItem.getDbpediaEntry() != null ) {
				write(contentItem.getDbpediaEntry());
			}
			_htmlBuffer.append("\n</tr>\n</table>");
		}
	}
	
	
						// ---------------------------
						// Supporting Private Methods
						// ---------------------------
	
	
	/**
	 * <p>Write the HTML representation for a Wikipedia entry.</p>
	 * @param dbpediaEntry specific entry in Wikipedia
	 * @param entityEntries entries for the semantics description of the keyword.
	 */
	protected void write(final NDbpediaRecord dbpediaEntry) {
		
		if( dbpediaEntry != null) {
			Object result = dbpediaEntry.get();
			if( result != null ) {
				NSummaryLabel dbpedia = (NSummaryLabel)result;
				
				String shAbstractStr = dbpedia.getShAbstract();
				if( shAbstractStr != null ) {
					String thumbnailStr = dbpedia.getThumbnail(); 
					String typeStr = dbpedia.getOntology();
					
					if(typeStr != null) {
						_htmlBuffer.append("\n<div id=\"tp_");
						_htmlBuffer.append(_key);
						_htmlBuffer.append("\" style=\"display:none\"> ");
						
		
						_htmlBuffer.append(_style.getTextSpan(typeStr));
						_htmlBuffer.append("</div>");
					}
							/*
							 * Display an image if one is available..
							 */
					if( thumbnailStr != null ) {
						_htmlBuffer.append("\n<td id=\"simg_");
						_htmlBuffer.append(_key);
						_htmlBuffer.append("\" >\n<a href=\"javascript:void(0)\" onMouseOver=\"view_image(\'");
						_htmlBuffer.append(shAbstractStr);
						_htmlBuffer.append("\', \'\', \'\', this, \'");
						_htmlBuffer.append(_key);
						_htmlBuffer.append("\');\" onMouseOut=\"hide_image();\"><img src=\"");
						_htmlBuffer.append(shAbstractStr);
						_htmlBuffer.append("\" width=\"80\" alt=\"Image for ");
						_htmlBuffer.append(_key);
						_htmlBuffer.append("\" ></a>");
						_htmlBuffer.append("\n</td>");
					}
				
					_htmlBuffer.append("\n<td bgcolor=\"#EEEEEE\" >");
										
							/*
							 * Display the abstract button.. The short abstract is also used in the 
							 * mouse over thumb description..
							 */
					_htmlBuffer.append(_style.getTextSpanSelection(shAbstractStr, "sabs_", _key));
					_htmlBuffer.append("<br>");
					
					_htmlBuffer.append(writeSummaryKeyword("Abstract", 'A'));
					_htmlBuffer.append("\n</td>");
				}
			}
		}
	}
	
	
		
	
	protected String writeSummaryKeyword(final String title, char prefix) {
		return writeSummaryKeyword(title, prefix, 0);
	}

	
	
	protected String writeSummaryKeyword(final String title, char prefix, int indent) {
		 StringBuilder summary = new StringBuilder("\n<a href=\"javascript:void(0)\"");
		 if( indent > 0 ) {
			 summary.append(" style=\"position:relative;left:");
			 summary.append(String.valueOf(indent));
			 summary.append("px\" ");
		 }
		 summary.append(" onclick=\"expand(\'");
		 summary.append(prefix);
		 summary.append("_");
		 summary.append(_key);
		 summary.append("\', this);\" ><img id=\"");
		 summary.append(prefix);
		 summary.append("_");
		 summary.append(_key);
		 summary.append("_img\" src=\"images/controls/expand.png\" width=\"11\" height=\"11\" >&nbsp;");
		 summary.append(_style.getHiliSpan(title));
		 summary.append("\n</a>");

		 return summary.toString();
	}


}

// -----------------------  EOF -------------------------------------

//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import java.util.Collection;
import java.util.Map;

import com.c24x7.models.CContentItemsMap.NContentItem;
import com.c24x7.models.CContentItemsMap.NDbpediaRecord;
import com.c24x7.models.CContentItemsMap.NEntityEntries;
import com.c24x7.models.CContentItemsMap.NEntityEntry;
import com.c24x7.models.CContentItemsMap.NImages;
import com.c24x7.models.CContentItemsMap.NMaps;
import com.c24x7.models.CContentItemsMap.NSearchResults;
import com.c24x7.models.CContentItemsMap.NVideos;
import com.c24x7.models.CContentItemsMap.NWikipedia;
import com.c24x7.semantics.dbpedia.CDbpediaResources.NSummaryLabel;
import com.c24x7.search.CImagesSearch.NImageEntry;
import com.c24x7.search.CWikipediaSearch.NWikipediaEntry;
import com.c24x7.search.CWebSearch.NSearchItem;
import com.c24x7.search.CYouTubeSearch.NVideoEntry;
import com.c24x7.util.string.CStringUtil;



public class CHTMLDescriptionWriter extends CHTMLSummaryWriter {
	protected static final int MAX_NUM_CHARS_SUMMARY = 106;
	protected CHTMLIFrameWriter _htmlIFrameWriter = null;
	
	
	public CHTMLDescriptionWriter(final String key) {
		super(key);
	}
	
	public CHTMLDescriptionWriter(CHTMLStyle style, final String key) {
		super(style, key);
	}
	
	/**
	 * <p>Write the description of the keyword such as Semantics, Wikipedia, Maps, images or videos.</p>
	 * @param contentItem the content element or keyword that needs to be exposed.
	 */
	public void write(final NContentItem contentItem) {
		contentItem.print(this);
	}
	
	
	public void write(final NDbpediaRecord entry) {
		if( entry != null ) {
			Object entryObj = entry.get();
			if( entryObj != null ) {
				NSummaryLabel dbpedia = (NSummaryLabel)entryObj;
				String abstractText = dbpedia.getLgAbstract();
	
				if(abstractText != null) {
					_htmlBuffer.append("\n<div id=\"A_");
					_htmlBuffer.append(_key);
					_htmlBuffer.append("\" style=\"display:none;position:relative;left:8px\" >");
					_htmlBuffer.append(_style.getSubtitleSpan("Abstract"));
					_htmlBuffer.append("<br>\n<div style=\"width:96%\">");
					/*
					StringBuilder abstractId = new StringBuilder("id=\"aid_");
					abstractId.append(_key);
					abstractId.append("\"");
					abstractId.append(" onclick=\"view_text(\'");
					abstractId.append("aid_");
					abstractId.append(_key);
					abstractId.append("\');\"");
					*/
					_htmlBuffer.append(_style.getTextSpanSelection(abstractText, "aid_", _key));
					/*
					_htmlBuffer.append("<a href=\"javascript:void(0)\" onclick=\"view_text(\'");
					
					_htmlBuffer.append(abstractId.toString());
					_htmlBuffer.append("\');\">");
					_htmlBuffer.append(writeSelect());
					_htmlBuffer.append("\n</a>");
					*/
					_htmlBuffer.append("\n</div>\n</div>");
				}
			}
		}
	}
	
	

	
	
	/**
	 * <p>Write the HTML representation for the entity entries, from Freebase and
	 * wikipedia entries.</p>
	 * @param entityEntries entity entries for this entity.
	 */
	public void write(final NEntityEntries entityEntries) {
		if( entityEntries != null ) {
			Object entriesObject = entityEntries.get();
				/*
				 * If there named entities have been found....
				 */
			if( entriesObject != null ) {
				NEntityEntry[] entries = (NEntityEntry[])entriesObject;
				StringBuilder htmlBuffer = new StringBuilder();
	 
				StringBuilder summary = new StringBuilder("\n<div id=\"S_");
				summary.append(_key);
				summary.append("\" style=\"display:none;position:relative;left:8px\" >");
				summary.append(_style.getSubtitleSpan("Semantics"));
	 
				htmlBuffer.append("\n<br><table id=\"NEtbl");
				htmlBuffer.append(_key);
				htmlBuffer.append("\" width=\"96%\" style=\"display:none\" > ");

				String article = null;
				String imageStr = null;
				int imageWidth = _style.getThumbnailWidth() + 8;
				int count = 0;
				String summaryStr = null;
				StringBuilder textId = null;
				
				for( NEntityEntry entry : entries ) {
					article = entry.getArticle();
					imageStr = entry.getImage();
						/*
						 * Shows only complete information as a combination of
						 * image and text.
						 */
					if( imageStr != null && article != null && article.length() > 10 ) {
						/*
						 * Extract the summary
						 */
						summary.append("\n<br>\n&nbsp;<a href=\"javascript:void(0)\" onClick=\"select(\'NE");
						summary.append(_key);
						summary.append(count);
						summary.append("\', \'NEtbl");
						summary.append(_key);
						summary.append("\', this);\" ><img src=\"images/controls/expand.png\" width=\"11\" height=\"11\"/>&nbsp");
						summaryStr = CStringUtil.extractFirstWords(article, MAX_NUM_CHARS_SUMMARY);
						summary.append(_style.getHiliSpan(summaryStr));
						summary.append("</a>");
			 
						/*
						 * Shows images associated with the Freebase entry..
						 */
						htmlBuffer.append("\n<tr id=\"NE");
						htmlBuffer.append(_key);
						htmlBuffer.append(count);
						htmlBuffer.append("\" style=\"display:none\">\n<td width=\"");
						htmlBuffer.append(imageWidth);
						htmlBuffer.append("\">\n<a href=\"javascript:void(0)\" onClick=\"view_item(\'");
						htmlBuffer.append(entry.getImage());
						htmlBuffer.append("\', \'\', \'\', this, \'");
						htmlBuffer.append(_key);
						htmlBuffer.append(" ");
						htmlBuffer.append(count);
						htmlBuffer.append("\', 'sel_image');\" onMouseOver=\"view_image(\'");
						htmlBuffer.append(entry.getImage());
						htmlBuffer.append("\', \'\', \'\', this, \'");
						htmlBuffer.append(_key);
						htmlBuffer.append("\');\" onMouseOut=\"hide_image();\" ><img src=\"");
						htmlBuffer.append(entry.getImage());
						htmlBuffer.append("\" width=\"");
						htmlBuffer.append(_style.getThumbnailWidth());
						htmlBuffer.append("\" alt=\"Image for ");
						htmlBuffer.append(_key);
						htmlBuffer.append("\" ></a>\n</td>");
		 
						/*
						 * Shows "cleaned up" articles
						 */
						/*
						textId = new StringBuilder("id=\"S_");
						textId.append(_key);
						textId.append(count);
						textId.append("\" onclick=\"view_text(\'");
						textId.append("S_");
						textId.append(_key);
						textId.append(count);
						textId.append("\');\"");
						*/
						textId = new StringBuilder(_key);
						textId.append(count);
						htmlBuffer.append("\n<td>");
						htmlBuffer.append(_style.getTextSpanSelection(article, "S_", textId.toString()));
						htmlBuffer.append("\n</td>\n</tr>");
						count++;
					}
				}
				summary.append("\n<br>");
				_htmlBuffer.append(summary.toString());
	
				htmlBuffer.append("\n</table>\n</div>");
				_htmlBuffer.append(htmlBuffer.toString());
			}
		}
	}


	
	public void write(final NWikipedia entries) {
		if( entries != null ) {
			Object entryObj = entries.get();
			if( entryObj != null ) {
				NWikipediaEntry entry = (NWikipediaEntry)entryObj;
				writeWikipedia(entry);
			}
		}
	}

	
	public void writeWikipedia(final NWikipediaEntry entry) {
		_htmlBuffer.append("\n<div id=\"W_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\" style=\"display:none\" >");
		_htmlBuffer.append(_style.getSubtitleSpan("Wikipedia"));
		_htmlBuffer.append("<br>");
				/*
				 * Display the Wikipedia page in the iFrame in the HTML canvas..
				 */
		if(_htmlIFrameWriter == null ) {
			_htmlIFrameWriter = new CHTMLIFrameWriter(_style, 670);
		}
		_htmlIFrameWriter.write(entry.getUrl());
		
		
		_htmlBuffer.append(_htmlIFrameWriter.toString());
		_htmlBuffer.append("\n</div>");
	}
	
	
	public void write(final NMaps maps) {
		if( maps != null ) {
			Object mapObject = maps.get();
			if( mapObject != null ) {
				writeMap((String)mapObject);
			}
		}
	}
	
				
	public void writeMap(final String mapUrl) {
		String[] sizeStr = { "90", "90" };
				
		_htmlBuffer.append("\n<div id=\"M_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\" style=\"display:none;position:relative;left:8px\" >");
		_htmlBuffer.append(_style.getSubtitleSpan("Maps"));
		_htmlBuffer.append(_style.getSubtitleDescSpan("&nbsp;&nbsp;(Mouse over to enlarge maps, click to select)"));
		_htmlBuffer.append("\n<br>\n<a href=\"javascript:void(0)\" onClick=\"view_item(\'");
		_htmlBuffer.append(mapUrl);
		_htmlBuffer.append("\', \'300\', \'300\', this, \'"); 
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\', 'icon-maps');\" onMouseOver=\"view_image(\'");
		_htmlBuffer.append(mapUrl);
		_htmlBuffer.append("\', \'300\', \'300\', this);\" onMouseOut=\"hide_image();\" ><img src=\"");
		_htmlBuffer.append(mapUrl);
		_htmlBuffer.append("\" width=\"");
		_htmlBuffer.append(sizeStr[0]);
		_htmlBuffer.append("\" height=\"");
		_htmlBuffer.append(sizeStr[1]);
		_htmlBuffer.append("\" ></a>");
		_htmlBuffer.append("\n</div>");
	}	
		
				/**
				 * <p>Create a HTML document for display the images associated with this keyword.</p>
				 * @param images collection of images...
				 */
	public void write(final NImages images) {
		if( images != null ) {
			Object imgEntriesObj = images.get();
			if( imgEntriesObj != null )  {
			
				@SuppressWarnings("unchecked")
				Collection<NImageEntry> imgEntries = (Collection<NImageEntry>)imgEntriesObj;
			
				if( imgEntries.size() > 0) {
					_htmlBuffer.append("\n<div id=\"I_");
					_htmlBuffer.append(_key);
					_htmlBuffer.append("\" style=\"display:none;position:relative;left:8px;width:96%\" >");
					_htmlBuffer.append(_style.getSubtitleSpan("Royalty free images"));
					_htmlBuffer.append(_style.getSubtitleDescSpan("&nbsp;&nbsp;(Move over image to enlarge, click to select)"));
			
					_htmlBuffer.append("\n<br>\n<table>\n<tr>\n<td id=\"IMG_");
					_htmlBuffer.append(_key);
					_htmlBuffer.append("\" >");
					_htmlBuffer.append("\n<div id=\"CI_");
					_htmlBuffer.append(_key);
					_htmlBuffer.append("\" >");
				
						/*
						 * Add UL for images gallery..
						 */
					int index = 0;
					for( NImageEntry entry : imgEntries) {
						if( entry.getUrl() != null ) {
							String[] dims = entry.resize();
							if( dims != null) {
								writeImage(entry, index++, dims);
							}
							else {
								writeImage(entry, index++);
							}
						}
					}
					_htmlBuffer.append("\n</div>\n</td>\n</tr>\n</table>\n</div>");
				}
			}
		}
	}
	
	
			/**
			 * <P>Create a HTML document for a new set of images added after the cursor.</p>
			 * @param imageEntriesMap map of images entries
			 * @param cursor index of the first new images added...
			 */
	public void writeImages(final Map<String, NImageEntry> imageEntriesMap, int cursor) {
		int count = 0;
		for( NImageEntry entry : imageEntriesMap.values()) {
			if( ++count > cursor) {
				if( entry.getUrl() != null ) {
					String[] dims = entry.resize();
					if( dims != null) {
						writeImage(entry, count, dims);
					}
					else {
						writeImage(entry, count);
					}
				}
			}
		}
	}
	
				/**
				 * <p>Create a HTML document to display video thumbnail and run the video player.</p>
				 * @param videos container for videos information.
				 */
	public void write(final NVideos videos) {

		if( videos != null ) {
			Object result = videos.get();
			if( result != null ) {
				NVideoEntry[] entries = (NVideoEntry[])result;
				writeVideos(entries);
			}
		}
	}
				
		
	public void writeVideos(final NVideoEntry[] entries) {
		if( entries.length > 0) {
			_htmlBuffer.append("\n<div id=\"V_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" style=\"display:none;position:relative;left:8px;width:96%\" >");
			_htmlBuffer.append(_style.getSubtitleSpan("Royalty free videos"));
			_htmlBuffer.append(_style.getSubtitleDescSpan("&nbsp;&nbsp;(Click to view or select videos)"));
			_htmlBuffer.append("\n<br>\n<table>\n<tr id=\"");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("video\" style=\"display:block\" ><td>\n<div id=\"CV_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" >");

			int index = 0;
			for( NVideoEntry videoEntry : entries) {
				writeVideo(videoEntry, index++);
			}
			_htmlBuffer.append("\n</div>\n</td>\n</tr>\n</table>\n</div>");
		}
	}

	
	public void write(final NSearchResults searchResults) {

		if( searchResults != null ) {
			Object result = searchResults.get();
			if( result != null ) {
				NSearchItem[] searchItems = (NSearchItem[])result;
				writeSearchResults(searchItems);
			}
		}
	}
		
		
	public void writeSearchResults(final NSearchItem[] searchItems) {
		if( searchItems.length > 0) {
			
			_htmlBuffer.append("\n<div id=\"Y_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" style=\"display:none;position:relative;left:8px;width:97%;\" >");
			_htmlBuffer.append(_style.getSubtitleSpan("References"));
			_htmlBuffer.append("\n<br><div id=\"CY_");
			_htmlBuffer.append(_key);
			_htmlBuffer.append("\" >");
			int index = 0;
			for( NSearchItem item : searchItems) {
				writeSearchItem(item, index++);
			}
			_htmlBuffer.append("\n</div>\n</div>");
		}
	}
	
						// ---------------------------
						//  Private Supporting Methods
						// ----------------------------
		

	protected void writeImage(NImageEntry entry, int counter, String[] dims) {
		_htmlBuffer.append("\n<div>");
		_htmlBuffer.append("\n<a href=\"javascript:void(0)\" onClick=\"view_item(\'");
		_htmlBuffer.append(entry.getUrl());
		_htmlBuffer.append("\', \'"); 
		_htmlBuffer.append(entry.getWidth());
		_htmlBuffer.append("\', \'");
		_htmlBuffer.append(entry.getHeight());
		_htmlBuffer.append("\', this, \'");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\', 'icon-image');\" onMouseOver=\"view_image(\'");
		_htmlBuffer.append(entry.getUrl());
		_htmlBuffer.append("\', \'"); 
		_htmlBuffer.append(entry.getWidth());
		_htmlBuffer.append("\', \'");
		_htmlBuffer.append(entry.getHeight());
		_htmlBuffer.append("\', this);\" onMouseOut=\"hide_image();\"><img src=\"");
		_htmlBuffer.append(entry.getUrl());
				
		_htmlBuffer.append("\" width=\"");
		_htmlBuffer.append(dims[0]);
		_htmlBuffer.append("\" height=\"");
		_htmlBuffer.append(dims[1]);
		_htmlBuffer.append("\" onError=\"remove(\'IMG_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append(counter);
		_htmlBuffer.append("\', \'IMG_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\');\" ></a>&nbsp;");
		_htmlBuffer.append("\n</div>");
	}
	
	protected void writeImage(NImageEntry entry, int counter) {	
		_htmlBuffer.append("\n<div>");
		_htmlBuffer.append("\n<a href=\"javascript:void(0)\" onClick=\"view_item(\'");
		_htmlBuffer.append(entry.getUrl());
		_htmlBuffer.append("\', \'\', \'\', this, \'");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\', 'icon-image');\" onMouseOver=\"view_image(\'");
		_htmlBuffer.append(entry.getUrl());
		_htmlBuffer.append("\', \'\', \'\', this);\" onMouseOut=\"hide_image();\"><img src=\"");
		_htmlBuffer.append(entry.getUrl());
		_htmlBuffer.append("\" onLoad=\"load_image(this);\" onError=\"remove(\'IMG_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append(counter);
		_htmlBuffer.append("\', \'IMG_");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\');\" ></a>&nbsp;");
		_htmlBuffer.append("\n</div>");
	}
	
	protected void writeVideo(NVideoEntry videoEntry, int counter) {
		_htmlBuffer.append("\n<div>");
		_htmlBuffer.append("\n<a href=\"javascript:void(0)\" onclick=\"view_video(\'");
		_htmlBuffer.append(videoEntry.getFlashUrl());
		_htmlBuffer.append("\', this, \'");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\');\"><img src=\"");
		_htmlBuffer.append(videoEntry.getThumbnail());
		_htmlBuffer.append("\" width=\"90\" height=\"80\" ></a>&nbsp;");
		_htmlBuffer.append("\n</div>");
	}
	

	protected void writeSearchItem(NSearchItem item, int counter) {
		_htmlBuffer.append("\n<div>\n<a href=\"javascript:void(0)\" onclick=\"show_url(\'");
		_htmlBuffer.append(item.getUrl());
		_htmlBuffer.append("\', \'");
		_htmlBuffer.append(_key);
		_htmlBuffer.append("\' );\" >");
		_htmlBuffer.append(_style.getHiliSpan(item.getUrl().substring(7)));
		_htmlBuffer.append("</a>&nbsp;&nbsp;");
	//	_htmlBuffer.append(_style.getTextSpan(item.getAbstract()));
		_htmlBuffer.append("\n</div>");
	}
}


// -----------------------------------  EOF ---------------------------------------------------------
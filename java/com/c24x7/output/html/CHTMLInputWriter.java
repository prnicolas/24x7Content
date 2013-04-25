// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.output.html;






			/**
			 * <p>Class that generate the content for the seed or content input.</p>
			 * @author Patrick Nicolas
			 * @date 09/19/2011
			 */
public class CHTMLInputWriter extends AHTMLWriter {

	protected int _indexFavorites = 0;
	
	public CHTMLInputWriter() {
		super();
	}
	
				/**
				 * <p>Write the input text from the user.</p>
				 * @param userId the user ID for this user 
				 */
	public void write(long userId) {
		_indexFavorites = 0;
		_htmlBuffer.append("\n<div id=\"content-entry\" >\n<form action=\"CAnalysisServlet\" method=\"POST\">\n<span id=\"title_entry\"></span>\n<input type=\"hidden\" name =\"seed\">\n<input type=\"hidden\" name=\"user_id\" value=\"");
		_htmlBuffer.append(String.valueOf(userId));
		_htmlBuffer.append("\" ><br>&nbsp;&nbsp;\n<div id=\"source_input\" style=\"display:none\"></div><textarea id=\"text_input\" cols=\"110\" rows=\"12\" style=\"font-size:12px; font-family:Ubuntu, Helvetica, Tahoma, sans-serif;\" onKeyUp=\"valid_seed(this.value);\">");
		_htmlBuffer.append("London is reeling from three nights of rioting with David Cameron.");
	    _htmlBuffer.append("</textarea>\n<br><br><table width=\"620\"><tr><td align=\"center\">\n<input id=\"explore-seed\" type=\"button\" class=\"btn\" value=\"Explore\" disabled=\"disabled\" onClick=\"explore_seed(this.form);\"/>&nbsp;&nbsp;\n<input id=\"clear-seed\" type=\"button\" class=\"btn\" value=\"Clear\" disabled=\"disabled\" onClick=\"clear_seed()\"></td></tr><table></form>\n</div>\n<div id=\"summary_keyword\"></div>\n<div id=\"content-favorites\" class=\"scroll-pane\">");
		
	    /*
	    CFavoritesManager favoritesManager = new CFavoritesManager();
	    try {
	    	List<CDocument> myDocuments = favoritesManager.getList();
	    	if( myDocuments != null ) {
	    		toHtml("Favorites", myDocuments);
	    	}
	    }
	    catch (SQLException e) {
	    	CLogger.error(e.toString());
	    }
	    */
		_htmlBuffer.append("\n</div>");
	}
	
	
	
					// ----------------------------
					// Private Supporting Methods
					// ----------------------------
	
	/*
	protected final void toHtml(final String label, List<CDocument> favsList) {
		_htmlBuffer.append("\n<span class=\"text-title\" style=\"color:white\">");
		_htmlBuffer.append(label);
		_htmlBuffer.append("</span><br>");
		CFavorite favorite = null;
		
		for( CDocument doc : favsList) {
			favorite = (CFavorite)doc;
			
			_htmlBuffer.append("\n<br><a href=\"javascript:void(0)\" onclick=\"load_text_input(");
			_htmlBuffer.append(favorite.getId());
			_htmlBuffer.append(", ");
			_htmlBuffer.append("this");
			_htmlBuffer.append("); \">");
			_htmlBuffer.append("\n<span class=\"text\" style=\"color:cyan\">");
			_htmlBuffer.append(favorite.getTitle());
			_htmlBuffer.append("</span>");
			_htmlBuffer.append("</a><br><img src=\"images/logos/");
			_htmlBuffer.append(favorite.getSrcImg());
			_htmlBuffer.append("\" width=\"14\" height=\"14\" alt=\"Image for news\">&nbsp;&nbsp;<span class=\"small-italic\">");
			_htmlBuffer.append(favorite.getSrc());
			_htmlBuffer.append("&nbsp;&nbsp;<img src=\"images/controls/rating_");
			_htmlBuffer.append(favorite.getRating());
			_htmlBuffer.append(".png\">&nbsp;&nbsp;");
			_htmlBuffer.append(favorite.getCreatedAt());
			_htmlBuffer.append("</span><br>\n<div id=\"favorite_");
			_htmlBuffer.append(favorite.getId());
			_htmlBuffer.append("\" style=\"display:none\">");
			_htmlBuffer.append(favorite.getContent());
			_htmlBuffer.append("\n</div>");
			_indexFavorites++;
		}
	}
	*/
	
}

// ------------------------  EOF ---------------------------------------------------
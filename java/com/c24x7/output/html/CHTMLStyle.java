//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;



public class CHTMLStyle {
	public static final String SPACER = "&nbsp;";
	public static final String DSPACER = "&nbsp;&nbsp;";
	public static final int    DEFAULT_THUMBNAIL_SIZE = 90;
	
	protected int	_thumbnailWidth = DEFAULT_THUMBNAIL_SIZE;
	protected int	_thumbnailHeight =DEFAULT_THUMBNAIL_SIZE;
	
	public CHTMLStyle() { 	}

	public void setThumbnailWidth(int tw) {
		_thumbnailWidth = tw;
	}
	
	public void setThumbnailHeight(int th) {
		_thumbnailHeight = th;
	}
	
	
	public String getTextSpan(final String text) {
		StringBuilder buf = new StringBuilder("<span class=\"text\">");
		buf.append(text);
		buf.append("</span>");
		return buf.toString();
	}
	
	
	public String getTextSpanSelection(final String text, final String prefix, final String key) {
		StringBuilder idStr = new StringBuilder(prefix);
		idStr.append(key);
		
		StringBuilder buf = new StringBuilder("<span id=\"");
		buf.append(idStr);
		buf.append("\" ");
		buf.append(" onMouseOver=\"highlight_text(\'");
		buf.append(idStr);
		buf.append("\', 'yellow');\" ");
		buf.append(" onMouseOut=\"highlight_text(\'");
		buf.append(idStr);
		buf.append("\', '#EEEEEE');\" ");
		buf.append("onClick=\"view_text(\'");
		buf.append(idStr);
		buf.append("\');\" ");
		buf.append(" class=\"text\">");
		buf.append(text);
		buf.append("</span>");
		return buf.toString();
	}

	public final String getHiliSpan(final String text) {
		StringBuilder buf = new StringBuilder("<span class=\"text-highlight\">");
		buf.append(text);
		buf.append("</span>");
		return buf.toString();
	}
	
	public final String getErrorSpan(final String text) {
		StringBuilder buf = new StringBuilder("<span class=\"text-error\">");
		buf.append(text);
		buf.append("</span>");
		return buf.toString();
	}
	
	
	public final String getTitleSpan(final String title) {
		StringBuilder buf = new StringBuilder("<span class=\"text-title\">");
		buf.append(title);
		buf.append("</span>");
		
		return buf.toString();
	}
	
	public final String getSubtitleSpan(final String title) {
		StringBuilder buf = new StringBuilder("<span class=\"text-subtitle\">");
		buf.append(title);
		buf.append("</span>");
		
		return buf.toString();
	}
	
	public final String getSubtitleDescSpan(final String title) {
		StringBuilder buf = new StringBuilder("<span class=\"text-subtitle-desc\">");
		buf.append(title);
		buf.append("</span>");
		
		return buf.toString();
	}

	public final int getThumbnailWidth() {
		return _thumbnailWidth ;
	}
	
	public final int getThumbnailHeight() {
		return _thumbnailHeight;
	}
}


// ----------------  EOF ----------------------------------------
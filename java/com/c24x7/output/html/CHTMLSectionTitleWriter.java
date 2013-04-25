//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

public class CHTMLSectionTitleWriter extends AHTMLWriter {
	protected String _icon = null;
	protected String _title = null;
	protected String _description = null;
	
	public CHTMLSectionTitleWriter(final String icon, final String title) {
		super();
		_icon = icon;
		_title = title;
	}
	
	public void setDescription(final String label) {
		_description = label;
	}
	

	
	public void write(final String id) {
		_htmlBuffer.append("\n<div style=\"position:relative\" >\n<span class=\"text-tab\" id=\"key-span\"><img src=\"images/controls/");
		_htmlBuffer.append(_icon);
		_htmlBuffer.append("\" width=\"14\" height=\"14\" alt=\"Image ");
		_htmlBuffer.append(_title);
		_htmlBuffer.append(" section\" >&nbsp;&nbsp;");
		_htmlBuffer.append(_title);
		_htmlBuffer.append("&nbsp;&nbsp;");
		
		if( _description != null ) {
			_htmlBuffer.append("<span id=\"");
			_htmlBuffer.append(_description);
			_htmlBuffer.append("\" style=\"color:blue\" ></span>&nbsp;&nbsp");
		}
		
		_htmlBuffer.append("<img src=\"images/controls/close_button.jpg\" width=\"12\" height=\"12\" onClick=\"close_section(\'");
		_htmlBuffer.append(id);
		_htmlBuffer.append("\');\" Alt=\"Image close section\" >\n</span>\n</div>");
	}
}

// ------------------------  EOF ---------------------------------------------------
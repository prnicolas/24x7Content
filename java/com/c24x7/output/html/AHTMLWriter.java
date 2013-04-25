//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;




			/**
			 * <p>Base class for HTML components generators. The generators rely on 
			 * buffer and a style.</p>
			 * @author Patrick Nicolas
			 * @date 07/08/2011
			 */
public abstract class AHTMLWriter {
	protected StringBuilder	_htmlBuffer = null;
	protected CHTMLStyle 	_style = null;
	
	
	public AHTMLWriter() {
		this(new CHTMLStyle());
	}
	
			/**
			 * <p>Create an HTML component with a specific style.
			 * @param style
			 */
	public AHTMLWriter(CHTMLStyle style) {
		_style = style;
		_htmlBuffer = new StringBuilder();
	}
		
			/**
			 * <p>Retrieve the content of the HTML component buffer.
			 */
	public String toString() {
		return _htmlBuffer.toString();
	}
}

// -----------------------  EOF ----------------------------------------
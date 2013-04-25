//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;


			/**
			 * <p>Class that generate HTML block (<div>) such as navigation, canvas... which 
			 * are defined as sub classes.</p>
			 * @author Patrick Nicolas
			 * @date 07/08/2011
			 */
public abstract class CHTMLBlockWriter extends AHTMLWriter {
	protected int _top 	= 0;
	protected int _left = 0;
	protected int _width = 0;
	
			/**
			 * <p>Create a HTML writer/generator for block of function with a predefined style with
			 * null dimension.<p>
			 * @param style style used in this block
			 */
	public CHTMLBlockWriter(CHTMLStyle style) {
		super(style);
	}
	
	
			/**
			 * <p>Create a HTML writer/generator for block of function with a predefined style and
			 * predefined dimension.<p>
			 * @param style style used in this block
			 * @param top absolute top value of this block
			 * @param left absolute left value of this block
			 * @param width absolute width of this block
			 */
	public CHTMLBlockWriter(CHTMLStyle style, int top, int left, int width) {
		super(style);
		_top = top;
		_left = left;
		_width = width;
	}
	
	public void setTop(int top) {
		_top = top;
	}
	
	public void setLeft(int left) {
		_left = left;
	}
	
	public void setWidth(int width) {
		_width = width;
	}
	
	public final int getTop() {
		return _top;
	}
	
	public final int getLeft() {
		return _left;
	}
	
	public final int getWidth() {
		return _width;
	}
	
}
// -----------------------   EOF ---------------------------------
//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.output.html;

import java.util.List;

import com.c24x7.models.CSummaryModel;
import com.c24x7.users.CUsersManager.CUser;


				/**
				 * <p>Create a HTML page using different components.</p>
				 * @author Patrick Nicolas
				 * @date 07/08/2011
				 */
public class CHTMLPage extends AHTMLWriter {
	protected final static String HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html>";
	protected final static String CSS_IMG_BORDER = "\n<style type=\"text/css\">\na img { border:none; }\n</style>";
	protected final static String JSFILES = "\n<script type=\"text/javascript\" src=\"javascripts/jquery-1.6.2.min.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/jquery-ui-1.8.7.custom.min.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/login.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/site.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/jquery.bxSlider.min.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/jquery.jscrollpane.min.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/jquery-explr-1.3.min.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/input.js\" ></script>\n<script type=\"text/javascript\" src=\"javascripts/workbench.js\" ></script>";
	protected final static String CSSFILES = "\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/style.css\" />\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/site.css\" />\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/jquery-explr-1.3.css\" />\n<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/workbench.css\" />";
	
	protected CHTMLTreeWriter 		_treeWriter = null;
	protected CHTMLIFrameWriter		_iFrameWriter = null;
	protected CHTMLCanvasWriter 		_canvasWriter = null;
	protected CHTMLCollectionWriter	_collectionWriter = null;	
	protected CUser 					_user = null;
	
				/**
				 * <p>Create an HTML page for a specific user with a default style.</p>
				 * @param loginName login or user name for the user..
				 */
	public CHTMLPage(final CUser user) {
		this(new CHTMLStyle());
		_user = user;
	}
			/**
			 * <p>Generate a page for analysis and search results with a specific style.</p>
			 * @param style style or theme used in the generation of the HTML page.
			 */
	public CHTMLPage(CHTMLStyle style) {
		super(style);
	}
	
	
	public void addTree() {
		_treeWriter = new CHTMLTreeWriter(_style);
	}
	
	public void addCanvas() {
		_canvasWriter = new CHTMLCanvasWriter(_style);
	//	_collectionWriter = new CHTMLCollectionWriter(_style);
	}
	
	public void addFrame() {
		_iFrameWriter = new CHTMLIFrameWriter(_style);
	}
		
	
	public void write(final List<CSummaryModel> modelsList) {
		if( modelsList != null ) {
			_htmlBuffer.append(HEADER);
			writeHeader();
			writeBody(modelsList);
			_htmlBuffer.append("\n</html>");
		}
	}
	
	public void write(final CSummaryModel model) {
		_htmlBuffer.append(HEADER);
		writeHeader();
		writeBody(model);
		_htmlBuffer.append("\n</html>");
	}
	
	
	public final String writeContent(final List<CSummaryModel> modelsList) {
		StringBuilder buf = new StringBuilder("\n<div id=\"main-content\" >");
		

		if( _canvasWriter != null ) {
			_canvasWriter.write(modelsList);
			buf.append(_canvasWriter.toString());
		}

		buf.append("\n</div>");
		
		return buf.toString();
	}
	
	
	
	public final String writeContent(final CSummaryModel model) {
		StringBuilder buf = new StringBuilder("\n<div id=\"main-content\" >");
		
		if( _treeWriter != null ) {
			_treeWriter.write(model);
			buf.append(_treeWriter.toString());
		}
		
		if( _canvasWriter != null ) {
			_canvasWriter.write(model);
			buf.append(_canvasWriter.toString());
		}
		/*
		if( _collectionWriter != null ) {
			_collectionWriter.write(model);
			buf.append(_collectionWriter.toString());
		}
		
		if( _iFrameWriter != null )  {
			_iFrameWriter.write(model);
			buf.append(_iFrameWriter.toString());
		}
		*/
		buf.append("\n</div>");
		
		return buf.toString();
	}
	
	
	
			/**
			 * <p>Add javascript snippets in the header of the model.</p>
			 * @return concatenate scripts..
			 */
	public final String writeScripts() {
		StringBuilder buf = new StringBuilder("\n<script type=\"text/javascript\" >");
		String script = null;
		
		if( _treeWriter != null )  {
			script = _treeWriter.getScript();
			if( script != null ) {
				buf.append(script);
			}
		}

		if( _iFrameWriter != null ) {
			script = _iFrameWriter.getScript();
			if( script != null ) {
				buf.append(script);
			}
		}
		
		buf.append("\n</script>");
		return buf.toString();
	}
	
	
				// -----------------------
				//  Private supporting methods
				// ---------------------------
	
	protected void writeHeader() {
		_htmlBuffer.append("\n<head>\n<title>24x7 Content Knowledge Workbench</title>");
		writeCSSfiles();
		writeJSfiles();
		_htmlBuffer.append(writeScripts());
		_htmlBuffer.append("\n</head>");
	}
	
	
	protected void writeBody(final List<CSummaryModel> modelsList) {
		_htmlBuffer.append("\n<body onLoad=\"show_background();show_content();\">\n<div id=\"container\" >");
		
		if( _user != null ) {
			_htmlBuffer.append("\n<input type=\"hidden\" name=\"user_id\" value=\"");
			_htmlBuffer.append(_user.getId());
			_htmlBuffer.append("\" >");
		}
		
		writePageHeader();
		_htmlBuffer.append(writeContent(modelsList));
		writePageFooter();
		_htmlBuffer.append("\n</div>\n</body>");
	}
	
	protected void writeBody(final CSummaryModel model) {
		_htmlBuffer.append("\n<body onLoad=\"show_background();show_content();\">\n<div id=\"container\" >");
		
		_htmlBuffer.append("\n<input type=\"hidden\" name=\"user_id\" value=\"");
		_htmlBuffer.append(_user.getId());
		_htmlBuffer.append("\" >");

		writePageHeader();
		_htmlBuffer.append(writeContent(model));
		writePageFooter();
		_htmlBuffer.append("\n</div>\n</body>");
	}
	
	
	protected void writePageHeader() {
		_htmlBuffer.append("\n<div id=\"header\">\n<DIV id=\"top_menu\"><SPAN id=\"wloginname\" >");
		_htmlBuffer.append((_user != null) ? _user.getLogin() : "tester");
		_htmlBuffer.append("</SPAN>\n<TABLE width=280>\n<TR>\n<TD width=48><A href=\"index.html\">Home</A></TD>\n<TD width=48><A href=\"javascript:void(0)\" onClick=\"text_input();\">Input</A></TD><TD width=48><A id=load_login_id onclick=logout(); href=\"javascript:void(0)\">Logout</A></TD><TD width=38><A href=\"faq.html\">FAQ</A></TD><TD width=62><A onclick=online_help() href=\"javascript:void(0)\"><IMG src=\"images/home/help.png\" width=18 height=18></A></TD></TR></TABLE>");

		_htmlBuffer.append("\n</DIV>\n</div>");
	}
	
	
	protected void writePageFooter() {
		_htmlBuffer.append("\n<div id=\"footer\" >\n<table width=\"940\" >\n<tr><td width=\"40\" align=\"center\"><a href=\"privacy.html\" class=\"small\">Privacy</a></td><td align=\"left\" width=\"2\">|</td><td width=\"78\" align=\"center\"><a href=\"termsofuse.html\" class=\"small\">Terms of Use</a></td><td align=\"left\" width=\"2\">|</td><td width=\"54\" align=\"center\"><a href=\"about.tml\" class=\"small\">About Us</a></td><td align=\"left\" width=\"2\">|</td><td width=\"28\"><a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://24x7content.blogspot.com/', '24x7_Content_Blog', 'width=680,height=560,location,menubar,resizable,scrollbars');\" >Blog</a></td><td align=\"left\" width=\"2\">|</td><td width=\"60\"><a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://twitter.com/24x7Content', 'Twitter', 'width=660,height=520,location,menubar,resizable=yes,scrollbars=yes');\" ><img src=\"images/home/follow_twitter.png\" width=\"13\" height=\"13\" alt=\"Follow us on Twitter\" /> Twitter</a></td><td align=\"left\" width=\"2\">|</td><td width=\"74\"><a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://www.facebook.com/24x7Content', 'Facebook', 'width=660,height=520,location,menubar,resizable,scrollbars');\"><img src=\"images/home/follow_facebook.png\" width=\"13\" height=\"13\" alt=\"Follow us on Facebook\" /> Facebook</a></td><td align=\"left\" width=\"2\">|</td><td width=\"50\" align=\"center\"><a href=\"sitemap.html\" class=\"small\">Site Map</a></td><td align=\"left\" width=\"2\">|</td><td align=\"left\" width=\"30\"><a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('').document.open('text/plain', '').write(document.documentElement.outerHTML)\">Source</a></td><td class=\"small\" align=\"right\">24x7 Content All rights reserved &#169 Copyright 2011</td></tr></table></div>");
	}
		

	protected void writeJSfiles() {
		_htmlBuffer.append(JSFILES);
	}
	
	protected void writeCSSfiles() {
		_htmlBuffer.append(CSSFILES);
		_htmlBuffer.append(CSS_IMG_BORDER);
	}
	
	
	protected String createSessionId() {
		long value = System.currentTimeMillis();
		return String.valueOf(value);
	}
 }

// ----------------------- EOF ------------------------------
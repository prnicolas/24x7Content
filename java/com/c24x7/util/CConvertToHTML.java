// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.util;


			/**
			 * <p>Class to convert generated content into a HTML page</p>
			 * @author Patrick Nicolas
			 * @date 12/03/2010
			 */
public final class CConvertToHTML {
	private final static String HTML_HEAD_1 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"";
	private final static String HTML_HEAD_2 = "\"http:/www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"";
    private final static String COPYRIGHT_NOTICE = "<h5>Copyright 2010 24x7 Content</h5>";
    
    
    public CConvertToHTML() {
    	
    }
	
	public static String convert(final String title, final String body) {
		StringBuilder buf = new StringBuilder(HTML_HEAD_1);
		buf.append(HTML_HEAD_2);
		buf.append("<html>");
		buf.append("<head><title>");
		buf.append(title);
		buf.append("</head></title>");
		buf.append("<body>");
		buf.append("<h3>");
		buf.append(title);
		buf.append("</h3>");
		buf.append("<p>");
		buf.append(body);
		buf.append("</p>");
		buf.append("<hr><p>");
		buf.append(COPYRIGHT_NOTICE);
		buf.append("</p></body>");
		buf.append("</html>");
		
		return buf.toString();
	}
}

// -------------------------------- EOF ----------------------------------------
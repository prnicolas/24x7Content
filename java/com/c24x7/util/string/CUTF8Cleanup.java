// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.string;


import java.io.UnsupportedEncodingException;
import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Very basic converter from ASCII to UTF-8 encoded characters.
			 * for more elaborate conversion, uses CUTF8Conversion class.</p>
			 * @author Patrick Nicolas
			 * @date 01/04/2011
			 * @see CUTF8Conversion
			 */
public final class CUTF8Cleanup {
	
				/**
				 * <p>Convert 2-byte characters into single byte character to comply 
				 * with UTF-8 encoding.</p>
				 * @param content original content
				 * @return UTF-8 encoded character string
				 */
	public static String cleanup(final String content) {
		String cleanedStr = null;
		try {	
			byte[] p = content.getBytes();
			byte[] newP = new byte[p.length];
			
			int k = 0;
			for( int j = 0; j < p.length; j++) {
				if( (p[j] > 0) && ((char)p[j] != '?')) {
					newP[k] = p[j];
					k++;
				}
			}	
	
			cleanedStr = new String(newP, 0, k, "UTF-8");
		}
		catch( UnsupportedEncodingException e) {
			CLogger.error(e.toString());
		}
		return cleanedStr;
	}
}

// ------------------------  EIF ---------------------------------
// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.nlservices.textanalyzer;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import java.io.UnsupportedEncodingException;
import com.c24x7.util.logs.CLogger;

			/**
			 * <p>Detects and converts ASCII or UNICODE encoded characters strings 
			 * to UTF-8. This class is an alternative to the simpler CUTF8cleanup class.</p>
			 * @author Patrick Nicolas
			 * @date 01/06/2011
			 */
public final class CUTF8Conversion {
	private String _charSet = null;
	
	public String convert(final String content) {
		String converted = null;
		nsDetector det = new nsDetector(nsPSMDetector.ALL) ;
		
		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				_charSet = charset;
			    System.out.println("CHARSET = " + _charSet);
			}
	    });
			
		byte[] originalBytes = content.getBytes();
		boolean isAscii = det.isAscii(originalBytes, originalBytes.length);
		if( !isAscii ) {
			 det.DoIt(originalBytes, originalBytes.length, false);
		}
		det.DataEnd();

		try {
	
			byte[] p = content.getBytes();
			converted = new String(p, "UTF-8");
			System.out.println("Converted: " + converted);
				
				
			byte[] newP = new byte[p.length];
			int k = 0;
			for( int j = 0; j < p.length; j++) {
				if( p[j] <= 0) {
					System.out.println((char)p[j]);
				}
				else {
					System.out.println((char)p[j]);
					newP[k++] = p[j];
				}
			}	
	
			converted = new String(newP, 0, k, "UTF-8");
			System.out.println("Converted: " + converted);
				
		}
		catch( UnsupportedEncodingException e) {
			CLogger.error(e.toString());
		}
		
		return converted;
	}
}

// ------------------------  EIF ---------------------------------
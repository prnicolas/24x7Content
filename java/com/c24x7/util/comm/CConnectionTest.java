package com.c24x7.util.comm;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.c24x7.util.logs.CLogger;


public final class CConnectionTest {
	public final static String SITE_TEST = "http://www.google.com";
	public static URL testUrl = null;
	
	static {
		try {
			testUrl = new URL(SITE_TEST);
		}
		catch( MalformedURLException e) {
			CLogger.error(e.toString());
		}
	}
	
	public static boolean test() {
		boolean succeed = false;
		
		try {
			testUrl.openConnection();
			succeed = true;
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		return succeed;
	}
}

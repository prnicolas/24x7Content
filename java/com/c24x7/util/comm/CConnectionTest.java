// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.comm;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
	
	public static boolean find(final String urlStr) throws UnknownHostException, IOException {
		boolean found = false;
		/*
		 * Open a HTTP connection to test if this URL is accepted
		 */
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			found = (connection.getResponseCode()  == HttpURLConnection.HTTP_OK);
		}
		catch( UnknownHostException e) {
			CLogger.error(urlStr + " not found");
		}
		
		return found;
	}

		
	
	public static String get(final String urlStr) throws UnknownHostException, IOException {
		if( urlStr == null ) {
			throw new IllegalArgumentException("Undefined url for connectivity");
		}
		
		StringBuilder responseBuf = null;
		BufferedReader reader = null;
		/*
		 * Open a TCP connection
		 */
		try {
			URL url = new URL(urlStr);
			URLConnection connection = url.openConnection();

			/*
			 * read the output from the Freebase REST server...
			 */
			String line = null;
			responseBuf = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
			while((line = reader.readLine()) != null) {
				responseBuf.append(line);
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		reader.close();
		reader = null;
		
		return (responseBuf == null) ? null : responseBuf.toString();
	}
}

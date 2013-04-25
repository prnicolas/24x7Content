// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.proxies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Proxy to manage the interaction with the NLG engine. The proxy is implemented
		 * as a thread (Runnable)</p>
		 * @author Patrick Nicolas
		 * @date 11/23/2010
		 */
public final class CNlgProxy implements Runnable {
	private URL 	_url 	 = null;
	private String 	_generatedContent = null;
	
		/**
		 * <p>Create a proxy object to manage connectivity with the remote NLG engine</p>
		 * @param url REST get URL
		 */
	public CNlgProxy(final URL url) {
		_url = url;
	}
	
		/**
		 * <p>Retrieve the generated content from the remote NLG engine</p>
		 * @return content
		 */
	public final String getResults() {
		return _generatedContent;
	}
	
			/**
			 * <p>Implementation of the coroutine used to manage 
			 * connectivity to NLG engine</p>
			 */
	public void run() {
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(_url.openStream()));

			StringBuilder buf = new StringBuilder();
			String newLine = null;
			while ((newLine = in.readLine()) != null) {
				buf.append(newLine);
			}
			_generatedContent = buf.toString();
			in.close();
		}
		catch( IOException e) {
			CLogger.error("failed to generate " + _generatedContent + " " + e.toString());
			e.printStackTrace();
		}
		finally {
			if(in != null) {
				try {
					in.close();
				}
				catch(IOException ex) {
					CLogger.error("failed to generate " + _generatedContent + " " + ex.toString());
					ex.printStackTrace();
				}
			}
			
		}
	}
}

// ----------------  EOF ---------------------------------------------------

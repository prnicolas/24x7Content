// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Queue;



		/**
		 * <p>Proxy to manage the interaction with the NLG engine. The proxy is implemented
		 * as a thread (Runnable)</p>
		 * @author Patrick Nicolas
		 * @date 11/23/2010
		 */
public final class CNlgClient {
	protected URL _url  = null;	
	
		/**
		 * <p>Create a proxy object to manage connectivity with the remote NLG engine</p>
		 * @param url REST get URL
		 */
	public CNlgClient(final URL url) {
		_url = url;
	}
	
		/**
		 * <p>Retrieve the generated content from the remote NLG engine</p>
		 * @return content
		 */
	public final String getResults() {
		return null;
	}
	
	
	public void execute(Queue<String> linkedQueue) throws IOException {
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(_url.openStream()));

			String newLine = null;
			StringBuilder buf = new StringBuilder();
			while ((newLine = in.readLine()) != null) {
				buf.append(newLine);
			}
			linkedQueue.add(buf.toString());
			System.out.println("Generated content: " + buf.toString());
			in.close();
		}
		finally {
			if(in != null) {
				in.close();
			}
			
		}
	}
}

// ----------------  EOF ---------------------------------------------------

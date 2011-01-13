// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.proxies;

import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;
import com.c24x7.util.CCache;
import com.c24x7.util.CXMLConverter;
import com.c24x7.nlservices.CNlGenerator;
import com.c24x7.nlservices.CNlOutputAnalyzer;
import com.c24x7.nlservices.CNlOutputFormatter;
import com.c24x7.nlservices.INlProcessor;
import com.c24x7.nlservices.content.AContent;
import com.c24x7.nlservices.content.CStructuredOutput;


		/**
		 * <p>Thread listening to GUI requests</p>
		 * @author Patrick Nicolas
		 * @date 12/22/2010
		 */
public final class CUIListener extends Thread {

	
	private final static int PORT_NUM = 18000;
	private final static String END_RESPONSE = "\nbye";
	private final static String END_REQUEST = "end";
		
	private ServerSocket _ss 				= null;
	private INlProcessor _nlGenerator 		= null;
	private INlProcessor _nlOutputAnalyzer 	= null;
	private INlProcessor _nlOutputFormatter = null;
	private CCache 		 _cachedSeed		= null;
	
	
	public CUIListener() throws IOException {
		CEnv env = new CEnv();
		_ss = new ServerSocket(PORT_NUM);
		_nlOutputAnalyzer = new CNlOutputAnalyzer();
		_nlGenerator = new CNlGenerator();
		_nlOutputFormatter = new CNlOutputFormatter(env);
		_cachedSeed = new CCache();
	}
	
	
			/**
			 * <p>Thread that manage the socket connection initiated by the 
			 * Rails-based Web services.</p>
			 */
	public void run() {
		System.out.println("Server started");
		String generatedContent = null;
		Socket s = null;
	
		while(true) {
			try {
				s = _ss.accept();
				
						/*
						 * Extract lines (\r\n delimited) from the buffered input stream.
						 */
				String input = extractInput(s);
				
						/*
						 * Process the test tag
						 */
				if(input.startsWith(CXMLConverter.TEST_TAG)) {
					generatedContent = "succeed";
				}

						/*
						 * Process seed tag..
						 */
				else if( input.startsWith(CXMLConverter.SEED_TAG)) {
					generatedContent = processSeed(input);
				}
						/*
						 * Process publish request
						 */
				else if( input.startsWith(CXMLConverter.PUBLISH_TAG)) {
					generatedContent = processPublish(input);
				}
				else if(input.startsWith(CXMLConverter.SETUP_TAG) ) {
					generatedContent = processSetup(input);
				}
				else {
					generatedContent = CXMLConverter.put(CXMLConverter.ERROR_TAG, "incorrect GUI request");
				}
				
			}
			catch( SocketException e) {
				CLogger.error(e.toString());
			}
			catch(IOException e) {
				generatedContent = CXMLConverter.put(CXMLConverter.ERROR_TAG, e.toString());
				CLogger.error(generatedContent);
			}
			
			finally {
				try {
					if( s != null) {
						PrintWriter out = new PrintWriter(s.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
						push(out, generatedContent);
					}
				}
				catch( IOException ex) {
					CLogger.error("Exception: " + ex.toString());
				}
			}
		}
	}
	
	
	private String extractInput(Socket s) throws IOException {
		String input = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		
		StringBuilder buf = new StringBuilder();
		do {
			input = reader.readLine();
			buf.append(input);
		}
		while(!endStream(input));				
		
		return buf.toString().substring(0, buf.length()-3);
	}
	
	
	private String processSetup(final String input) throws IOException {
		return "";
	}
	
	
	
	private String processSeed(final String input) throws IOException {
		String converted = CXMLConverter.get(CXMLConverter.SEED_TAG, input).trim();
		
		AContent genContent = _cachedSeed.get(converted);
		
		if( genContent == null) {
			genContent = new AContent(converted);
			System.out.println("Need to retrieve generated content!");
			genContent = _nlGenerator.process(genContent);
			genContent = _nlOutputAnalyzer.process(genContent);
		}
		
		return (genContent == null) ? 
				CXMLConverter.put(CXMLConverter.ERROR_TAG, "no content generated") : 
				genContent.toString();
	}
 
	
	
	private String processPublish(final String input) throws IOException {
		String converted = CXMLConverter.get(CXMLConverter.PUBLISH_TAG, input);
		String[] tags = {  
				CXMLConverter.TITLE_TAG,  
				CXMLConverter.DESC_TAG,
				CXMLConverter.SELECT_TAG
		};
	 	String[] elements =  CXMLConverter.get(tags, converted);
		
	 	String generatedContent = null;
		
		if( elements == null || elements.length < 2) {
			generatedContent = CXMLConverter.put(CXMLConverter.ERROR_TAG, "improperly formatted publish request");
		}
		else {
			AContent genContent = new CStructuredOutput(elements);
			genContent = _nlOutputFormatter.process(genContent);
			generatedContent = genContent.toString();
		}
		
		return generatedContent;
	}

		
	private void push(PrintWriter out, final String content) {
		out.println(content+ END_RESPONSE);
	}
	
	private final boolean endStream(final String content) {
		return content.endsWith(END_REQUEST);
	}
}

// ----------------------------  EOF --------------------------------------------

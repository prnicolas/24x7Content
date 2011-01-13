// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices;


import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;


import com.c24x7.nlservices.content.CRawOutput;
import com.c24x7.nlservices.content.AContent;
import com.c24x7.util.logs.CLogger;
import com.c24x7.nlservices.textanalyzer.CUTF8Cleanup;
import com.c24x7.proxies.CNlgProxy;


/**
 * <p>Language and parser independent client Java class used to process the generated
 * content using a seed, URL and key as input.
 * </p>
 * @author Patrick Nicolas  
 * @date 10/23/10
 * @see com.c24x7.nlservices.INlProcessor 
 */
public class CNlGenerator implements INlProcessor {

	private final static String KEY_FILE 		= "key.txt";
	private final static String DEFAULT_URL 	= "http://84.88.144.166:8080/wise/cg";
	private final static String SEED_LABEL 		= "?seed=";
	private final static String KEY_LABEL 		= "&key=";
	private final static String BEGIN_MARKER 	= "\"generatedContent\":[{\"string\":[";		
	private final static String END_MARKER 		= "]}]}";
	private final static String DELIM			= "\",\"";

	private String 	_urlStr 	= null;
	private String 	_key 		= null;
	
	/**
	 * <p>Constructor with default URL:target and default API key for NLG API</p>
	 */
	public CNlGenerator() {
		this(CNlGenerator.DEFAULT_URL);
	}
	

	/**
	 * <p>Constructor with used defined URL target and Key for the NLG API</p>
	 * @param urlStr user defined URL for NLG API
	 * @param key string used as a key to access API
	 * @throws IllegalArgumentException if url string is incorrectly defined.
	 */	
	public CNlGenerator(final String urlStr) throws IllegalArgumentException {
		if( urlStr == null || urlStr.length() < 2) {
			throw new IllegalArgumentException("Incorrect URL");
		}
		_urlStr = urlStr;
	}
	
	
	/**
	 * <p>Extract the content generated from a seed input</p>
	 * @param content Seed sentence(s) input to the NLG engine. The seed can be a file_name that contains a string if the
	 * 		parameter isFile is true, or a command line argument is isFile is false
	 * @return CRawOutput object that contains the list of sentences related to the topic & semantics defined in the seed 
	 * @throws IOException if API is not available or URL or Key is incorrect
	 */
	@Override
	public AContent process(AContent content) throws IOException  {
		if( content == null ) {
			throw new IllegalArgumentException("Output analyzer input is null");
		}	
		return getContent(content.toString());
	}
	
	
			/**
			 * <p>Function to test connectivity with remote NLG engine.</p>
			 * @param seedStr test seed
			 * @return true is connectivity is successful, false otherwise
			 */
	public boolean test(final String seedStr) {
		boolean connected = false;
		try {
			connected = (getContent(seedStr) != null);
		}
		catch(IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return connected;
	}
	
	


	protected CRawOutput getContent(final String seedStr) throws IOException {
		CRawOutput rawGenerated = null;
		
		try {
				/*
				 * Get the key from file
				 */
			getKey();
			
				/*
				 * Process the REST 'Get' request results, using a time out thread
				 * to avoid infinite blocking..
				 */
			URL url = createURL(seedStr);
			CNlgProxy req = new CNlgProxy(url);
			Thread t = new Thread(req);
			t.start();
			try {
				t.join();
			}
			catch(InterruptedException ex) { 
				throw new IOException("Interrupted connection to NLG engine: " + ex.toString());
			}
		
				/*
				 * Extract sentences from the stream buffer 
				 */
			CRawOutput gen = new CRawOutput(seedStr);
					/*
					 * Convert to UTF-8 format..
					 */
			String resultsStr =  CUTF8Cleanup.cleanup(req.getResults());
			rawGenerated = CNlGenerator.extractContent(resultsStr, gen);
			System.out.println("Done with seed: " + seedStr);
		}
		catch( MalformedURLException e) {
			throw new IOException("Incorrect URL for NLG engine: " + e.toString());
		}
		catch( UnsupportedEncodingException e) {
			throw new IOException("Improperly encoded generated content: " + e.toString());
		}
			
		return rawGenerated;
	}
	
	
	
	// ----------------
	// Private methods
	// ----------------

	private static CRawOutput extractContent(final String rawContent, CRawOutput gen) {
		if( rawContent == null ) {
			throw new NullPointerException("Raw content is not found");
		}
		String remaining = rawContent;
		final int beginMarkerLen = BEGIN_MARKER.length()+1;
		
		int indexFragment 	  = remaining.indexOf(BEGIN_MARKER),
		    indexEndFragment  = 0;
		String[] sentences = null;
		
				/*
				 * Break down the results stream into 
				 */
		while(indexFragment != -1 ) {
			remaining = remaining.substring(indexFragment+beginMarkerLen);
			indexEndFragment = remaining.indexOf(END_MARKER);
				/*
				 * Extract the response from this sentence seed
				 */
			sentences = remaining.substring(0,indexEndFragment).split(DELIM);
			if( sentences != null) {
				for( String sentence : sentences)  {
					gen.add(sentence.startsWith("\\\"") ? sentence.substring(2) : sentence );
				}
			}
			indexFragment = remaining.indexOf(BEGIN_MARKER);
		}
		
		return gen;
	}
		
		
	
	private final URL createURL(final String seed) throws MalformedURLException, UnsupportedEncodingException  {
		StringBuilder buf = new StringBuilder(_urlStr);
		buf.append(SEED_LABEL);
		buf.append(URLEncoder.encode(seed, "utf8"));
		buf.append(KEY_LABEL);
		buf.append(_key);
		CLogger.info("URL: " + buf.toString());
		
		return new URL(buf.toString());
	}
	
	private void getKey() throws IOException {
		FileInputStream fis = new FileInputStream(KEY_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		
		String line = reader.readLine();
		if(line == null) {
			throw new IOException("No key defined in file");
		}
		_key = line.trim();
		reader.close();
	}
}


// -------------------------  EOF --------------------------------------------
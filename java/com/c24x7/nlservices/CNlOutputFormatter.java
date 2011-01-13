// Copyright (C) 2010-11 Patrick Nicolas
package com.c24x7.nlservices;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFastIO;
import com.c24x7.util.CXMLConverter;
import com.c24x7.nlservices.content.AContent;
import com.c24x7.nlservices.content.CStructuredOutput;
import com.c24x7.formats.*;
import com.c24x7.util.comm.CConnectionTest;


		/**
		 * <p>Generic filter implementation of formatting rules for Twitter, Facebook, 
		 * Web, Blogs, email related content. The filtering and formatting is
		 * implemented in nested classes</p> 
		 * @author Patrick Nicolas
		 * @date 11/20/2010
		 * @see com.c24x7.nlservices.INlProcessor 
		 */
public final class CNlOutputFormatter implements INlProcessor {
	public static final String EMAIL_LBL 	= "email";
	public static final String WEB_LBL 		= "web";
	public static final String TWITTER_LBL 	= "twitter";
	public static final String RSS_LBL 		= "rss";
	public static final String FACEBOOK_LBL = "facebook";
		
	private static Map<String, AFormatProxy> channel = null;
	static {
		channel = new HashMap<String, AFormatProxy>();
		channel.put(EMAIL_LBL, new CMailFormat());
		channel.put(WEB_LBL,new CWebFormat());
		channel.put(RSS_LBL, new CRSSFormat());
		channel.put(TWITTER_LBL, new CTwitterFormat());
		channel.put(FACEBOOK_LBL, new CFacebookFormat());
	}
	
			/**
			 * <p>Generic abstract formatted generated content.</p>
			 * @author Patrick Nicolas
			 */
	public static abstract class AFormatProxy implements Runnable {
		protected CEnv 		_env 		= null;
		protected String	_head 		= null;
		protected String	_error		= null;
		
				/**
				 * <p>Initialize the environment for this formatting object.</p>
				 * @param env user environment
				 */
		public void setEnv(CEnv env) {
			_env = env;
		}
		
			/**
			 * <p>Generates a formatted output content from a list of generated sentences (NLG)</p>
			 * @param structuredOutput structured generated content
			 */
		abstract public void format(CStructuredOutput structuredOutput) throws IOException ;
		abstract public String results();
		abstract protected void process();
		
				/**
				 * <p>Thread to process 
				 * @see java.lang.Runnable#run()
				 */
		public void run() {
			process();
			CLogger.info(getClass().getName() + " complete execution");
		}
		
		protected void debug(final String outputString, final String debugPath) {
			if(debugFile != null) {
				try {
					write( CEnv.OUTPUT_PATH + debugPath + debugFile,  outputString);
				}
				catch( IOException e) {
					CLogger.error("Cannot access debug file: " + debugPath + debugFile + " " + e.toString());
				}
			}
		}
		
	
		protected static void write(final String fileName, 
									final String content) throws IOException {
			CFastIO.write(fileName, content);
		}
	}
		
	private static String debugFile = null;
	
			/**
			 * <p>Create a NLG output filter and formatter object</p>
			 * @param env environment for this user.
			 */	
	public CNlOutputFormatter(CEnv env) {		
		AFormatProxy format = null;
		for(String destination : env.getConfig("user").keySet()) {
		
			format = channel.get(destination);
			if( format != null) {
				format.setEnv(env);
			}
		}
	}
	
			/**
			 * <p>Define the debugging mode for this formatter object.</p>
			 * @param debugFileName name of the debugging log, null to switch debugging OFF
			 */
	public static void setDebugFile(String debugFileName) {
		debugFile = debugFileName;
	}
	
	
	
			/**
			 * <p>Format the current structured content generated by the NLG engine into a set of
			 * format covering social messages, email, blogs and web sites.</p>
			 * @param content structured generated output.
			 * @return original content
			 * @throws IllegalArgumentException if content is undefined.
			 */
	@Override
	public AContent process(AContent content) throws IOException { 
		if( content == null ) {
			throw new IllegalArgumentException("Output formatter input is null");
		}
		
		if( !CConnectionTest.test() ) {
			CLogger.error("Ping failed");
			throw new IOException("Internet is not reachable");
		}
				/*
				 * Launch all the proxy threads to communicate with 
				 * the different web sites and social networks
				 */
		CStructuredOutput structuredOutput = (CStructuredOutput)content;
		String[] selection = structuredOutput.getSelection();
	
		Thread t = null;
		AFormatProxy type = null;
				/*
				 * walk through the selected social targets./.
				 */
		for(String selected : selection) {
						
			type = channel.get(selected);
			if(type != null) {
				type.format(structuredOutput);
				t = new Thread(type);
				CLogger.info("Start a thread for " + type.getClass().getName());
				t.start();
				try {
					t.join();
				}
				catch(InterruptedException ex) {
					System.out.println(ex.toString());
				}
			}
		}
				/*
				 * Collect all the responses from the social networks
				 * blogs and destination web sites.
				 */
		StringBuilder buf = new StringBuilder();
		
		String result = null;
		for(String selected : selection) {
			type = channel.get(selected);
			if( type != null ) {
				result = type.results();
				buf.append(result);
			}
		}
		return new AContent(CXMLConverter.put(CXMLConverter.SOCIAL_TAG, buf.toString()));
	}
	

						// ------------------
						//  Private Methods
						// ------------------
	
	
	
} 

// -------------------  EOF ------------------------------------
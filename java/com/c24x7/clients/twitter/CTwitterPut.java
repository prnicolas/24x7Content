// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.twitter;

import java.io.IOException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;
import com.c24x7.clients.IClientWriter;
import com.c24x7.util.string.CXMLConverter;


		/**
		 * <p>
		 * Main object responsible to creating and retrieving Tweets. The update
		 * function is synchronous while the retrieval of tweets is asynchronous. Tweets
		 * are retrieved and stored into a temporary file for further processing if necessary.
		 * @author Patrick Nicolas
		 */

public final class CTwitterPut implements IClientWriter {
	protected	Twitter	_twitter	= null;
	protected String	_target		= null;
	protected String  _tweet 		= null;
	protected String	_error		= null;
	
	/**
	 * <p>
	 * Create a Twitter object to retrieve and create Tweets using the
	 * default logging id and password. This constructor relies on OATH authentication.</p>
	 */

	public CTwitterPut() {
//		requestAccessToken();
	}
	
	protected CTwitterPut(String content) {
		if( content == null ) {
			throw new IllegalArgumentException("Credentials for Twitter undefined");
		}
		
		final String consumerKey = CEnv.get(CEnv.TWITTER_LABEL, "oauth.consumerKey");
		final String consumerSecret = CEnv.get(CEnv.TWITTER_LABEL, "oauth.consumerSecret");
		final String token = CXMLConverter.get("<token>", content);
		final String secret = CXMLConverter.get("<secret>", content);
		_tweet = CXMLConverter.get("<msg>", content);
		_twitter = (new TwitterFactory()).getOAuthAuthorizedInstance(consumerKey, consumerSecret, new AccessToken(token, secret));
	}
	
	public IClientWriter create(String content) {
		return new CTwitterPut(content);
	}
	
	
	public void run() {
		try {
			if( _twitter.updateStatus(_tweet) == null ) {
				_error = "Could not post tweet";
				CLogger.error(_error);
			}
		}
		catch( TwitterException e) {
			_error = "Fails to update Twitter";
			CLogger.error(_error + e.toString());
		}
	}
	
	public final String getTarget() {
		return _target;
	}
	

	public final String getError() {
		return _error;
	}
		
	
			/**
			 * <p>Send an update to twitter.</p>
			 * @param tweet (<140 characters) text
			 * @return true if the update succeeded, false otherwise.
			 * @throws TwitterException
			 */
	public void update(final String tweet) throws IOException {
		try {
			if( _twitter.updateStatus(tweet) == null ) {
				throw new IOException("Could not post tweet");
			}
		}
		catch( TwitterException e) {
			throw new IOException(e.toString());
		}
	}
	
	
	
	/*
	protected void requestAccessToken()   {
		if( twitterOath == null ) {
			twitterOath = CEnv.getConfiguration(CEnv.TWITTER_LABEL);
		}
		
		Properties prop = requestRequestToken();  
		Configuration config = new PropertyConfiguration(prop);	     
		_twitter = new TwitterFactory(config).getInstance();
	//	_target =  CEnv.getConfiguration(CEnv.USER_LABEL, TWITTER_LABEL).get(TARGET);
	}
	
	

	protected static Properties requestRequestToken() {
		  File file = new File(TWITTER_PROPERTY_FILE);
		  Properties prop = new Properties();
		  InputStream is = null;
		  OutputStream os = null;
	        
		  try{
			  if (file.exists()) {
				  is = new FileInputStream(file);
				  prop.load(is);
				  is.close();
			  }
			  for(String key : twitterOath.keySet()) {
				  prop.setProperty(key, twitterOath.get(key));
			  }
			  
			  os = new FileOutputStream(TWITTER_PROPERTY_FILE);
              prop.store(os, TWITTER_PROPERTY_FILE);
              os.close();
		  }
		  
		  catch(IOException ioe){
			  CLogger.error("Cannot retrieve the request token");
		  }
		  finally{
	            if(null != is){
	                try {
	                    is.close();
	                } 
	                catch (IOException ignore) {
	                	  CLogger.error("Cannot retrieve the request token");
	                }
	            }
	            if(null != os){
	                try {
	                    os.close();
	                } 
	                catch (IOException ignore) {
	                	  CLogger.error("Cannot retrieve the request token");
	                }
	            }
	       }
		  return prop;
	}
	*/
}


// -----------------------------  EOF -----------------------------------
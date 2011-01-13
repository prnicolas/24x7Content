// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.clients;

import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Map;


import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.Paging;
import twitter4j.TwitterException;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogCursor;



		/**
		 * <p>
		 * Main object responsible to creating and retrieving Tweets. The update
		 * function is synchronous while the retrieval of tweets is asynchronous. Tweets
		 * are retrieved and stored into a temporary file for further processing if necessary.
		 * @author Patrick Nicolas
		 */

public class CTwitterClient implements Runnable {
	private static final String TWITTER_LABEL = "twitter";
	private static final String TARGET 		  = "target";
	
	private static final long INITIAL_INTER_REQUEST_TIME = 1200*1000;
	private static final String TWITTER_ID_FILE 	= CEnv.TWITTER_LOGS_DIR + "sinceid";
	private static final String DELIM				= "\n\r";
	private static final String TWITTER_PROPERTY_FILE = "twitter4j.properties";
	
	private static Map<String, String> twitterOath = null;

	
	private long 		_interRequestTime = INITIAL_INTER_REQUEST_TIME;
	private	Twitter		_twitter	= null;
	private String		_target		= null;
	private boolean 	_running 	= true;
	private int 		_requestNum = 0;
	private CLogCursor  _logCursor 	= null;

	
	/**
	 * <p>
	 * Create a Twitter object to retrieve and create Tweets using the
	 * default logging id and password. This constructor relies on OATH authentication.</p>
	 * @throws TwitterException if the OATH authentication fails
	 */

	public CTwitterClient(CEnv cenv) throws TwitterException {
		requestAccessToken(cenv);
	}
	

		/**
		 * <p>Stop the thread that extract tweets</p>
		 */
	public void stop() {
		_running = false;
	}
	
	
	public final String getTarget() {
		return _target;
	}
	
			/**
			 * <p>Retrieve the current limit for hourly retrieval of tweets</p>
			 * @return current maximum number of messages that can be retrieved.
			 * @throws TwitterException if Twitter feed fails
			 */
	public int getHourlyLimit() throws TwitterException {
		RateLimitStatus limitStatus = _twitter.getRateLimitStatus();
		return limitStatus.getHourlyLimit();
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
	
	
	
			/**
			 * Retrieve the status of the last few tweets..
			 * @throws TwitterException  if there is not enough messages to retrieve
			 */
	public void retrieve() throws TwitterException {
			/*
			 * If log cursor is not initialized, then create a new one.
			 */
		if( _logCursor == null) {
			_logCursor = new CLogCursor(TWITTER_ID_FILE);
			CLogger.addAppender(CEnv.TWITTER_LOGS_DIR);
		}
		
		try {
			int numRemainingHits = getRemainingHits(_twitter);
			if( numRemainingHits < 2) {
				throw new TwitterException("Not enough hits available for Twitter login");
			}

			List<Status> statusList = (  !_logCursor.exists()) ?
										_twitter.getFriendsTimeline() :
										_twitter.getFriendsTimeline(new Paging(_logCursor.get()));
	
			StringBuilder buf = null;
			boolean isLastStatusID = false;
			
			for( Status curStatus : statusList) {
				buf = new StringBuilder();
				buf.append(DELIM);
				if( isLastStatusID == false) {
					_logCursor.set(curStatus.getId());
					isLastStatusID = true;
				}
				buf.append(curStatus.getId());
				buf.append(DELIM);
				
				buf.append(curStatus.getCreatedAt());
				buf.append(DELIM);
				buf.append(curStatus.getUser().getName());
				buf.append(DELIM);
				buf.append(curStatus.isTruncated());
				buf.append(DELIM);
				
				String msgContent = curStatus.getText(); 
				buf.append(msgContent);				
				CLogger.info(buf.toString());
		
				_logCursor.save();
			}
		}
		catch(IOException e) {
			throw new TwitterException(e.toString());
		}
	}
	
	
			/**
			 * <p>Main thread to extract tweets</p>
			 */
	public void run() {
		try {
			_logCursor.load();
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		
		while( _running ) {
			try {
				retrieve();
				CLogger.info("Retrieve status #" + _requestNum++);
				Thread.sleep(_interRequestTime);
			}
			catch( TwitterException e) {
				CLogger.error(e.toString());
			}
	
			catch( InterruptedException e) {
				CLogger.error(e.toString());
			}
		}
	}
	
	
				// ----------------------------------
				//  Private Support Methods
				// -------------------------------
	
	
	
	private static int getRemainingHits(Twitter twitter) throws TwitterException {  
		RateLimitStatus limitStatus = twitter.getRateLimitStatus();
		return limitStatus.getRemainingHits();
	}

	private void requestAccessToken(CEnv env) throws TwitterException  {
		if( twitterOath == null ) {
			twitterOath = env.getConfiguration("app", "twitter");
		}
		Properties prop = requestRequestToken();   
		Configuration config = new PropertyConfiguration(prop);	     
		_twitter = new TwitterFactory(config).getInstance();
		_target =  env.getConfiguration(CEnv.USER_LABEL, TWITTER_LABEL).get(TARGET);
	}
	
	

	private static Properties requestRequestToken() {
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
	
	/*
	private static void getTwitterOAuth() {
		twitterOath = new HashMap<String, String>();
		BufferedReader reader = null;
		String line = null;

		try {
			FileInputStream fis = new FileInputStream(TWITTER_OATH_CONFIG);
			reader = new BufferedReader(new InputStreamReader(fis));
			String[] keyValues = null;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					keyValues = line.split(FIELD_DELIM);
					twitterOath.put(keyValues[0].trim(), keyValues[1].trim());
					CLogger.info(keyValues[0].trim() + "," + keyValues[1].trim());
				}
			}
				
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Cannot load dictionary " + e.toString());
		}
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) {
					CLogger.error("Cannot load dictionary " + e.toString());
				}
			}
		}
	}
	*/
}

// -----------------------------  EOF -----------------------------------
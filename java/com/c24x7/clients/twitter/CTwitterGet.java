// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.twitter;

import java.util.List;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.net.URLEncoder;


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
import twitter4j.http.AccessToken;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;
import com.c24x7.util.CIntMap;
import com.c24x7.util.logs.CLogCursor;




		/**
		 * <p>
		 * Main object responsible to creating and retrieving Tweets. The update
		 * function is synchronous while the retrieval of tweets is asynchronous. Tweets
		 * are retrieved and stored into a temporary file for further processing if necessary.
		 * @author Patrick Nicolas
		 */

public class CTwitterGet {
	protected static final String JDBC_DRIVER_NAME 			= "com.mysql.jdbc.Driver";
	protected static final String TWITTER_ID_FILE 			= CEnv.twitterlogsDir + "sinceid";
	protected static final String TWITTER_PROPERTY_FILE 		= "twitter4j.properties";
	protected static final String QUERY_COUNT					= "SELECT count(*) FROM 24x7c.trends;";
	protected static final String QUERY_KEYWORDS				= "SELECT keywords FROM 24x7c.trends";
	
	protected static Map<String, String> twitterConfig = null;
	protected static int 		numRows		= -1;
	protected static String 	jdbcURL 	= null;
	protected static String 	dbRole		= null;
	protected static String 	dbPwd		= null;

	static {
		twitterConfig = CEnv.getConfiguration(CEnv.TWITTER_LABEL);
		Map<String, String> trendConfig = CEnv.getConfiguration(CEnv.TRENDS_LABEL);
		String numRowsStr = trendConfig.get("rows");
		try {
			numRows = Integer.parseInt(numRowsStr);
		}
		catch( NumberFormatException e) {
			numRows = 30;
		}
			
		jdbcURL = trendConfig.get("jdbc_url");
		dbRole = trendConfig.get("db_role");
		dbPwd = trendConfig.get("db_pwd");
		

		/*
		try {
			CNewsAnalyzer.maxNumTrendyChars =  Integer.parseInt(maxNumChars);
		}
		catch( NumberFormatException e) {
			CNewsAnalyzer.maxNumTrendyChars  = 78;
		}
		*/
	}

	protected Twitter		_twitter 	= null;
	protected CLogCursor  _logCursor 	= null;
//	protected CNewsAnalyzer	_newsFilter	= null;
	protected long		_counter	= 0L;
	

	
	/**
	 * <p>
	 * Create a Twitter object to retrieve and create Tweets using the
	 * default logging id and password. This constructor relies on OATH authentication.</p>
	 * @param env environment variable
	 */

	public CTwitterGet() {
		requestAccessToken();
		_logCursor = new CLogCursor(TWITTER_ID_FILE);
	//	_newsFilter = new CNewsAnalyzer();
	}
	
	/*
	public void attachFilter(final CNewsAnalyzer filter) {
		_newsFilter = filter;
	}
	
	public void detachFilter() {
		_newsFilter = null;
	}
	*/


			/**
			 * <p>Retrieve the current limit for hourly retrieval of tweets</p>
			 * @return current maximum number of messages that can be retrieved.
			 * @throws TwitterException if Twitter feed fails
			 */
	public static int getHourlyLimit(Twitter twitter) throws IOException {
		int status = -1;
		try {
			RateLimitStatus limitStatus = twitter.getRateLimitStatus();
			status = limitStatus.getHourlyLimit();
		}
		catch(TwitterException e ) {
			throw new IOException(e.toString());
		}
		return status;
	}
	
	
	
			/**
			 * Retrieve the status of the last few tweets..
			 * @throws TwitterException  if there is not enough messages to retrieve
			 */
	public void retrieve() throws IOException {

		Statement stmt = null;
		
		try {
			_logCursor.load();
			Paging paging = new Paging();
			long sinceID = _logCursor.getSinceID();
			paging.setSinceId(sinceID);
			List<Status> statusList = _twitter.getFriendsTimeline(paging);
			
			if( statusList.size() > 0 ) {
			
					//Register the JDBC driver for MySQL.
				Class.forName(JDBC_DRIVER_NAME);
					//Define URL of database server...
				Connection con = DriverManager.getConnection(jdbcURL, dbRole, dbPwd);
	
					//Get a Statement object
				stmt = con.createStatement();
				

				long lastTweetId = -1L;
				int rowCursor = _logCursor.getRowID();
				getNumRows(stmt);
								
				CIntMap allKeywords = getAllKeywords(stmt);
				System.out.println("Existing keywords: "  + allKeywords.toString());
				
				String message = null, src = null, msg = null, queryStr = null;
				StringBuilder keywords = null;
				
				for( Status curStatus : statusList) {
					
					if( lastTweetId == -1L) {
						_logCursor.setSinceID(curStatus.getId());
					}
					
					src = curStatus.getUser().getName();
					msg = curStatus.getText();
					lastTweetId = curStatus.getId();
					keywords = new StringBuilder();
		//		    message = (_newsFilter != null) ? _newsFilter.filter(src, msg, allKeywords, keywords) : msg;
				    
					if( lastTweetId > sinceID && message != null ) {
						try {
							queryStr = ( _counter++ >= numRows) ?
										queryUpdateStr(src, curStatus.getCreatedAt(), msg, keywords.toString().trim(), rowCursor++) :
										queryInsertStr(src, curStatus.getCreatedAt(), msg, keywords.toString().trim());
					
							stmt.executeUpdate(queryStr);  	
						    System.out.println("New Tweet recorded.. " + lastTweetId);
						    
							if( rowCursor >= numRows) {
								rowCursor = 0;
							}
						}
						catch (SQLException e) {
							CLogger.error(e.toString());
						}
					}	
				}
				_logCursor.save(rowCursor);
			}
		}
		catch( TwitterException e) {
			throw new IOException(e.toString());
		}
		catch( ClassNotFoundException e) {
			throw new IOException(e.toString());
		}
		catch( SQLException e) {
			throw new IOException(e.toString());
		}
		finally {
			if( stmt != null) {
				try  {
					stmt.close();
				}
				catch(SQLException e) {
					CLogger.error(e.toString());
				}
			}
		}
	}
	

			/**
			 * <p>
			 * Extract all the Keywords currently in the list of the last trendy topics.</p>
			 * @param stmt Reference to the JDBC statement
			 * @return Hashmap for the keywords in the current list of topics.
			 * @throws SQLException if statement cannot be executed
			 */
	protected CIntMap getAllKeywords(Statement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery(QUERY_KEYWORDS);
		
		CIntMap allKeywords = new CIntMap();
		String keywordsStr;
		String[] keywords = null;
		
		while (rs.next() ) {
			keywordsStr= rs.getString("keywords");
			if( keywordsStr != null) {
				keywords = keywordsStr.split(" ");
				if( keywords != null ) {
					for( String keyw : keywords) {
						allKeywords.put(keyw);
					}
				}
			}
		}
		return allKeywords;
	}
		
	
	
	protected String queryInsertStr(	final String src, 
									final Date date, 
									final String msg,
									final String keywords) throws SQLException, UnsupportedEncodingException  {
		
		StringBuilder buf = new StringBuilder("INSERT INTO 24x7c.trends (source, date, user_id, message, keywords) VALUES(\'");
		buf.append(src);
		buf.append("\',\'");
		buf.append(date);
		buf.append("\',\'");
		buf.append("1");
		buf.append("\',\'");
		String msg_str = msg.replace("#", " ");
		buf.append(URLEncoder.encode(msg_str, "UTF-8"));
		buf.append("\',\'");
		buf.append(keywords);
		buf.append("\');");
		
		return buf.toString();
	}
	
	
	protected String queryUpdateStr(	final String src, 
									final Date date, 
									final String msg,
									final String keywords,
									final int nextId) throws SQLException, UnsupportedEncodingException  {

		StringBuilder buf = new StringBuilder("UPDATE 24x7c.trends SET source=\'");
		buf.append(src);
		buf.append("\', date=\'");
		buf.append(date);
		buf.append("\', user_id=\'");
		buf.append("1");
		buf.append("\',message=\'");
		String msg_str = msg.replace("#", " ");
		buf.append(URLEncoder.encode(msg_str, "UTF-8"));
		buf.append("\',keywords=\'");
		buf.append(keywords);
		buf.append("\' where id =\'");
		buf.append(nextId);
		buf.append("\';");
		
		return buf.toString();
	}
	
	
	
	protected void getNumRows(final Statement stmt) throws SQLException {
		if( _counter < numRows ) {
			ResultSet rs = stmt.executeQuery(QUERY_COUNT);
			int count = -1;
			while (rs.next() ) {
				count = rs.getInt("COUNT(*)");
			}
			_counter = count;
		}
	}
	

	
				// ----------------------------------
				//  Private Support Methods
				// -------------------------------
	
	
	/*
	protected static int getRemainingHits(Twitter twitter) throws TwitterException {  
		RateLimitStatus limitStatus = twitter.getRateLimitStatus();
		return limitStatus.getRemainingHits();
	}
	*/
	
	protected void requestAccessToken()   {
		final String consumerKey = twitterConfig.get("oauth.consumerKey");
		final String consumerSecret = twitterConfig.get("oauth.consumerSecret");
		final String token = twitterConfig.get("oauth.accessToken");
		final String secret = twitterConfig.get("oauth.accessTokenSecret");
		
		_twitter = (new TwitterFactory()).getOAuthAuthorizedInstance(consumerKey, consumerSecret, new AccessToken(token, secret));
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
			  for(String key : twitterConfig.keySet()) {
				  prop.setProperty(key, twitterConfig.get(key));
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
	
}

// -----------------------------  EOF -----------------------------------
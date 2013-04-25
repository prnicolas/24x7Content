// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.twitter;

import java.util.Map;
import java.util.Hashtable;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.RequestToken;
import twitter4j.http.AccessToken;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;



		/**
		 * <p>
		 * Main object responsible to creating and retrieving Tweets. The update
		 * function is synchronous while the retrieval of tweets is asynchronous. Tweets
		 * are retrieved and stored into a temporary file for further processing if necessary.
		 * @author Patrick Nicolas
		 */

public final class CTwitterAuth  {
	public static final String TWITTER_OAUTH_URL = "http://localhost:3000/dashboard/signup_oauth?";
	
	protected static Map<String, String> 		twitterOath = null;
	protected static Map<String, RequestToken> requestTokenCache = new Hashtable<String, RequestToken>();

	protected	Twitter		_twitter	= null;

	
	
		/**
		 * <p>
		 * Create an unauthenticated Twitter object to authenticate and authorize 24x7 Content
		 * on the behalf of the user.</p>
		 */

	public CTwitterAuth() {
		if( twitterOath == null ) {
			twitterOath = CEnv.getConfiguration(CEnv.TWITTER_LABEL);
		}
		String consumerKey = twitterOath.get("oauth.consumerKey");
		String consumerSecret = twitterOath.get("oauth.consumerSecret");
		_twitter = new TwitterFactory().getOAuthAuthorizedInstance(consumerKey, consumerSecret);
	}
	
	
	public String authenticate(final String user) {
		String twitterAuthURL = null;
		try {
			RequestToken requestToken = _twitter.getOAuthRequestToken(TWITTER_OAUTH_URL);
			if( requestToken != null) {
				twitterAuthURL = requestToken.getAuthorizationURL();
				requestTokenCache.put(user, requestToken);
			}
		}
		catch( TwitterException e) {
			CLogger.error("Twitter authentication failed: " + e.toString());
		}
		
		return twitterAuthURL;
	}
	
	
	public String[] authorize(final String user, final String auth_token) {
		System.out.println("Auth token: " + auth_token);
		
		String[] tokenStr = null;
		try {
			RequestToken requestToken  = requestTokenCache.get(user);
			if( requestToken != null) {
				AccessToken accessToken = _twitter.getOAuthAccessToken(requestToken);
				tokenStr = new String[] { 
						accessToken.getToken(), 
						accessToken.getTokenSecret() 
				};
				
				if( requestTokenCache.remove(user) == null ) {
					CLogger.error("cannot remove data from user in authentication cache");
				}
			}
		}
		catch(TwitterException e) {
			CLogger.error("Twitter authorization failed: " + e.toString());
		}
		
		return tokenStr;
	}
}

// -----------------------------  EOF -----------------------------------
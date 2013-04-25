// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.facebook;


import com.google.code.facebookapi.*;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Class to authenticate user with Facebook through the application.</p>
		 * @author Patrick Nicolas
		 * @date 01/29/2011
		 */
public final class CFacebookAuth extends AFacebook {
	protected final static String FACEBOOK_URL = "http://www.facebook.com/login.php?api_key=";

	
	public CFacebookAuth() {
		initialize(null);
	}
	
	public String authenticate() {
		String url = null;
		boolean isDesktop = _client.isDesktop();
		
		try {
			String token = _client.auth_createToken();
		    url = FACEBOOK_URL + _client.getApiKey() + "&v=1.0&auth_token=" + token + "&req_perms=status_update,read_stream,publish_stream,offline_access";
		}
		
		catch( FacebookException e) {
			CLogger.error("Cannot authenticate with facebook: " + e.toString());
		}
		return url;
	}
	
	
	public String[] authorize(final String authToken) {
		String[] results = null;
		
		try {
			String sessionKey = _client.auth_getSession(authToken,true );
			String tempSecret = _client.getSecret();
			results = new String[] {
		       sessionKey, tempSecret
			};
			
		}
		catch( FacebookException e) {
			CLogger.error("Cannot authorize Facebook: " + e.toString());
		}
		return results;
	}
}




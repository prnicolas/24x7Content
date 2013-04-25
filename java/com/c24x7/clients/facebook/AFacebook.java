// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.facebook;

import com.c24x7.util.CEnv;
import com.google.code.facebookapi.FacebookJsonRestClient;



		/**
		 * <p>Generic base class for Facebook objects used in authentication,
		 * retrieval of messages and status updates.</p>
		 * @author Patrick Nicolas
		 * @date  02/14/2011
		 */
public abstract class AFacebook {
	protected final static String API_KEY 			= "apikey";
	protected final static String API_SECRET 		= "apisecret";
	protected final static String FACEBOOK_LABEL 	= "facebook";
	protected static String apiKey = CEnv.get(FACEBOOK_LABEL, API_KEY);
	protected static String apiSecret = CEnv.get(FACEBOOK_LABEL, API_SECRET);

	protected FacebookJsonRestClient _client = null;
	
	protected void initialize(String sessionKey) {
		_client = (sessionKey == null) ?
				new FacebookJsonRestClient(apiKey, apiSecret) :
			    new FacebookJsonRestClient(apiKey, apiSecret, sessionKey);	
	}
}

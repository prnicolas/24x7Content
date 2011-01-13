package com.c24x7.clients;

import com.google.code.facebookapi.*;
import com.c24x7.util.CEnv;
import java.io.IOException;


		/**
		 * <p>Facebook client class</p>
		 * @author Patrick Nicolas
		 * @date 12/27/2010
		 */
public final class CFacebookClient {
	private final static String API_KEY 		= "apikey";
	private final static String API_SECRET 		= "apisecret";
	private final static String FACEBOOK_UID	= "uid";
	private final static String FACEBOOK_LABEL 	= "facebook";
	private static final String TARGET 			= "target";
	
	private FacebookJsonRestClient _facebookCLient = null;
	private long	_uid = -1L;
	private String	_target = null;
	 
			/**
			 * <p>Create a Facebook client code by extracting the account information from 
			 * the user configuration record.</p>
			 * @param env environment variable
			 */
	public CFacebookClient(CEnv env) {
		String apiKey = env.getConfiguration(CEnv.APP_LABEL,FACEBOOK_LABEL).get(API_KEY);
		String apiSecret = env.getConfiguration(CEnv.APP_LABEL,FACEBOOK_LABEL).get(API_SECRET);
		String uidStr =  env.getConfiguration(CEnv.USER_LABEL,FACEBOOK_LABEL).get(FACEBOOK_UID);
		
		_facebookCLient = new FacebookJsonRestClient(apiKey, apiSecret);
		_uid =  Long.parseLong(uidStr);
		_target = env.getConfiguration(CEnv.USER_LABEL,FACEBOOK_LABEL).get(TARGET);
	}
	
	     
	public final String getTarget() {
		return _target;
	}
	
	public boolean update(final String status) throws IOException{
		try {
			_facebookCLient.users_setStatus(status, _uid);
			return true;
		}
		catch(FacebookException e) {
			throw new IOException(e.toString());
		}
	}

}

// --------------------  EOF --------------------------------
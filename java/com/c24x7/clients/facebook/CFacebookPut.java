package com.c24x7.clients.facebook;

import com.google.code.facebookapi.*;
import com.c24x7.clients.IClientWriter;
import com.c24x7.util.string.CXMLConverter;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Facebook client class</p>
		 * @author Patrick Nicolas
		 * @date 12/27/2010
		 */
public final class CFacebookPut extends AFacebook implements IClientWriter {
	
	protected String	_target = null;
	protected String	_error = null;
	protected String  _status = null;
	

	public CFacebookPut() { }
	
			/**
			 * <p>Create a Facebook client code by extracting the account information from 
			 * the user configuration record.</p>
			 * @param env environment variable
			 */
	public CFacebookPut(String content) {
		
		if( content == null ) {
			throw new IllegalArgumentException("Credentials for Facebook undefined");
		}		
		final String sessionKeyStr = CXMLConverter.get("<token>", content);
		initialize(sessionKeyStr);
		
		_status = CXMLConverter.get("<msg>", content);
	}
	
	public IClientWriter create(String content) {
		return new CFacebookPut(content);
	}
	

	public final String getTarget() {
		return _target;
	}
	
	public String getError() {
		return _error;
	}
	
	public void run()  {
		try {
			 
			if ( _client.users_hasAppPermission(Permission.PUBLISH_STREAM)) {
				long facebookUserID = _client.users_getLoggedInUser();
				_client.stream_publish(_status, null, null, null, facebookUserID);
			}
			else {
				_error = "App does not have status update permission for " + Permission.PUBLISH_STREAM.toString();
				CLogger.error(_error);
			}
		}
		catch(FacebookException e) {
			_error = "Could not access Facebook: ";
			CLogger.error(_error + e.toString());
		}
	}

}

// --------------------  EOF --------------------------------
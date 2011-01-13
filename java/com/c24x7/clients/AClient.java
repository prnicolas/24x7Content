// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.clients;

import java.io.IOException;
import java.util.Map;



		/**
		 * <p>Generic client class to handle communication with content
		 * destinations.This abstract base class define the state of login,
		 * the destination server and login (or user) name</p>
		 * @author Patrick Nicolas
		 * @date 12/06/2010
		 */
public abstract class AClient {
		
	protected String 	_serverName = null;
	protected String	_userName 	= null;
	protected String	_password	= null;
	protected boolean	_isLoggedIn = false;
	
	
		/**
		 * <p>connect, login and push content to a content destination.</p>
		 * @param content1 1st component of the content to be pushed
		 * @param content2 second component of the content to be pushed
		 * @throws IOException
		 */
	abstract public void update(final String content) throws IOException;
	
	

		/**
		 * <p>Initialize the properties for this user with the login name, password and
		 * content destination server.</p>
		 * @param properties key,value pairs of user information.
		 */
	protected void load(final Map<String, String> properties) {
		_userName = properties.get("user");
		_password = properties.get("password");
		_serverName = properties.get("servername");
	}
	
	public final String getServerName() {
		return _serverName;
	}
	
	public final String getUserName() {
		return _userName;
	}
}

// ---------------------  EOF --------------------------------------
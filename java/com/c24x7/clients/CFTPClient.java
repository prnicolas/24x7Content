// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.clients;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.util.Map;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;


		/**
		 * <p>Implement the protocol to upload an ASCII (text or HTML) file on a
		 * remote FTP server.</p>
		 * @author Patrick Nicolas
		 * @date 12/06/2010
		 */
public final class CFTPClient extends AClient {

	private FTPClient 	_ftpClient 	= null;
	private String		_outputFile	= null;
	private String		_urlStr		= null;
	
			/**
			 * <p>Create a FTP client to upload and down load files. The server name, user
			 * name and password are provided during the instantiation of the class for 
			 * connecting and logging in the FTP server.</p>
			 * @param env name environment for the content destination defined by the user name, password and server name
			 */
	
	public CFTPClient(CEnv env) {
		load(env.getConfiguration(CEnv.USER_LABEL, "web"));
	}
	
			/**
			 * <p>Access the name of the remote HTML file to be updated through FTP.</p>
			 * @return remote HTML page names
			 */
	public final String getOutputFile() {
		return _outputFile;
	}
	
	
			/**
			 * <p>Extract the complete URL for the web site</p>
			 * @return URL with HTTP prefix
			 */
	public final String getUrlStr() {
		StringBuilder buf = new StringBuilder("http://");
		buf.append(_urlStr);
		buf.append("/");
		buf.append(_outputFile);
		
		return buf.toString();
	}
	
	
			/**
			 * <p>Retrieve the URL string for the file loaded on the new web server</p>
			 * @return URL of the new web page
			 */
	public final String getURLString() {
		String webString = "No target";
		
		int indexWeb = _serverName.indexOf(".");
		if( indexWeb != -1) {
			StringBuilder buf = new StringBuilder(_serverName.substring(indexWeb+1, _serverName.length()));
			buf.append("/");
			buf.append(_outputFile);
			webString = buf.toString();
		}
			
		return webString;
	}
	
	
				/**
				 * <p>Send (or upload) a file on a remote FTP server. This method does not assume
				 * that the local and remote file name share the same name.</p>
				 * @param localFileName name of the file in the local directory
				 * @param  remoteFileName name of the destination file, in the remote directory
				 * @throw IOException if local or remote file system cannot be accessed
				 */
	@Override
	public void update(final String localFileName) throws IOException {
		InputStream input = null;
		
		try {
			_ftpClient = new FTPClient();
					/*
					 * First connect and login using user name and password.
					 */
			if( connect() && login()) {
					/*
					 * Transfer the file to the remote FTP server
					 */ 
				input = new FileInputStream(localFileName);
				_ftpClient.storeFile(_outputFile, input);
				input.close();
			}
		}
		catch( NullPointerException e) {
			_ftpClient.disconnect();
			if(input != null) {
				input.close();
			}
		}
		finally {
			if(_ftpClient.isConnected() ) {
				_ftpClient.disconnect();
			}
			if(input != null) {
				input.close();
			}
		}
	}
	
	/**
	 * <p>Initialize the properties for this user with the login name, password and
	 * content destination server.</p>
	 * @param properties key,value pairs of user information.
	 */
	@Override
	protected void load(final Map<String, String> properties) {
		super.load(properties);
		_outputFile = properties.get("filename");
		_urlStr = properties.get("target");
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("web site:");
		buf.append(_serverName);
		buf.append(" from ");
		buf.append(_userName);
		buf.append(" created ");
		buf.append(_outputFile);
		
		return buf.toString();
	}
	
					// ------------------------------
					//   Private Methods
					// ---------------------
	
	private boolean connect() throws IOException {
		boolean statusOK = true;

				/*
				 * Simulate the Command line interaction
				 */
		_ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		_ftpClient.connect(_serverName);
			
				/*
				 * Validate the reply code 
				 */
		int reply = _ftpClient.getReplyCode();
		if((reply == -1) ||  !FTPReply.isPositiveCompletion(reply) ) {
			_ftpClient.disconnect();
			statusOK = false;
		}
		
		return statusOK;
	}
	
	
	private boolean login() throws IOException  {		
		try {
			_isLoggedIn = _ftpClient.login(_userName, _password);
		}
		catch( FTPConnectionClosedException e) {
			CLogger.error(e.toString());
		}
				/*
				 * If failed to log in, then logout, otherwise, 
				 * We assume transfer of an ASCII file, behind
				 * a firewall.. (local passive mode).
				 */
		if(!_isLoggedIn) {
			_ftpClient.logout();
		}			
		else {
			_ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
			_ftpClient.enterLocalPassiveMode();
		}
		
		return _isLoggedIn;
	}
}

// --------------------  EOF ------------------------------------
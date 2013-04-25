// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.c24x7.clients.twitter.CTweet;
import com.c24x7.clients.twitter.CTwitterFields;
import com.c24x7.util.CEnv;

/**
 * <p>
 * Define the base class for the processing of social logs..</p>?
 * @author Patrick Nicolas
 */
public class CLogsManager {
	public final static String 	MSG_DELIM 		= "INFO -";
	
	protected String 	_logsDirectoryName = null;
	protected File[] 	_files 			= null;
	protected int 	 	_counter 		= 0;
	protected int    	_fileCounter 	= 0;

	
			/**
			 * <p>
			 * Create a generic logger to browse for messages from social networks
			 * with a default directory for the logs
			 * </p>
			 * @throws IOException
			 */
	public CLogsManager() throws IOException {
		this(CEnv.logsDir);
	}
	
	/**
	 * <p>
	 * Create a generic logger to browse for tweets, buffered in a directory specified by the user.
	 * </p>
	 * @param directoryName name of the directory containing the social logs
	 * @throws IOException if the directory is incorrect
	 */
	public CLogsManager(final String directoryName) throws IOException  {
		_logsDirectoryName = directoryName;
		extractLogsList();
	}
	
	
			/**
			 * <p>
			 * Extracts text messages stored in a directory to create a list
			 * of Social messages of type ASocialItem used in the analysis
			 * </p>
			 * @param tweets  List of Social Messages to extract
			 * @throws IOException
			 */
	public void extract(List<CTweet> tweets) throws IOException {
		for(int j = _fileCounter; j < _files.length; j++) {
			extract(j, tweets);
			_fileCounter = j+1;
		}
	}
	
	
			/**
			 * Accesses the number of social networks messages processed..
			 * @return number of messages
			 */
	public final long getNumMessages() {
		return _counter;
	}
	

	
	/**
	 * <p>
	 * Extracts the content of a specific stored message</p>
	 * @param fileIndex  index of this array of file objects
	 * @param socialMessages current list of message to update
	 * @throws socialnetException  if an I/O error occurs during the extraction of messsages
	 */
	protected void extract(int 			fileIndex, 
						   List<CTweet> tweets) throws IOException {
		
		int msgNum = -1;
		BufferedReader reader = null;
		CTweet msg = null;
		CTwitterFields twitterMap = new CTwitterFields();
		
		try {			
			FileInputStream fis = new FileInputStream(_files[fileIndex]);
			reader = new BufferedReader(new InputStreamReader(fis));
					
					/*
					 * Parse the tweets...
					 */
			String 	line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					if( line.equals(MSG_DELIM)) {
						msgNum = 0;
					}
					msg = twitterMap.set(msgNum++, line);
					
					if(msg != null) {
						tweets.add(msg);
						_counter++;
					}
				}
			}
			reader.close();
		}
		finally {
			if( reader != null ) {
				reader.close();
			}	
		}
	}
	

	

	
	
			// ----------------------
			// Private methods
			// -----------------------

	protected void extractLogsList() throws IOException {
		File inputDir = new File(_logsDirectoryName);
		
		 		// Make sure that the directory of input files exists 
		if( !inputDir.isDirectory()) {
			throw new IOException(_logsDirectoryName + " is not a directory");
		}
		_files = inputDir.listFiles();
	}

}
// ----------------------  EOF ------------------
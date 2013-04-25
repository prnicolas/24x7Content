// Copyright (c) 2010-2011 Patrick Nicolas
package com.c24x7.clients.twitter;


import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.c24x7.util.logs.CLogsManager;


/**
 * <p>Generic message stream generator to simulate interaction with
 * social network. This social stream implements the Social Source interface
 * using stored messages (Files) as input.
 * A buffer file may contains multiple messages and attachments.
 * The methods for this class are thread safe. The list of messages is
 * just appended by the main extracting method.</p>
 * @author Patrick Nicolas
 */
public class CTwitterLoad {
	protected CLogsManager _logs = null;
	protected List<CTweet> _tweets = new ArrayList<CTweet>();
	
	/**
	 * <p>
	 * Create a social stream object to process logs</p>
	 * @param logs array of files that contains raw social messages
	 */
	public CTwitterLoad(final CLogsManager logs) {
		_logs = logs;
	}
	
	public void setLogs(CLogsManager logs) {
		_logs = logs;
	}
	
		
	/**
	 * <p>
	 * Synchronous version of extraction of messages.
	 * Retrieve the content of a social network or source from a buffer
	 * of temporary files for testing and evaluation purpose.</p>
	 * @throws socialnetException
	 */	

	 public List<CTweet> extract() throws IOException {
		 _tweets.clear();
		_logs.extract(_tweets);
		return _tweets;
	 }
	

	 public final long getNumMessages() {
		 return _logs.getNumMessages();
	 }

}

// -----------------------------  EOF ----------------------------
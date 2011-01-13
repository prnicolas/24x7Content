// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.util.logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


		/**
		 * <p>Helper class to keep track of cursor used to down load social messages such 
		 * as twitter or Facebook</p>
		 * @author Patrick Nicolas
		 */

public final class CLogCursor {
	private String 	_logCursorFile = null;
	private long 	_sinceID = -1L;
	
			/**
			 * <p>Create a log cursor to persist on a file
			 * @param logCursorFile file the log cursor persists.
			 */
	public CLogCursor(final String logCursorFile) {
		_logCursorFile = logCursorFile;
		_sinceID = 0L;
	}
	
	public final long get() {
		return _sinceID;
	}
	
	public void set(long cursor) {
		_sinceID = cursor;
	}
	
	public final boolean exists() {
		return (_sinceID != 0L);
	}
	
	
			/**
			 * <p>Save the current log cursor in file</p>
			 * @throws IOException if file cannot be accessed
			 */
	public void save() throws IOException {
		BufferedWriter writer = null;
	
		try {
			FileOutputStream fos = new FileOutputStream(_logCursorFile);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			String idString = String.valueOf(_sinceID);
			writer.write(idString, 0, idString.length());
			writer.close();
		}
		finally {
			if( writer != null ) {
				writer.close();
			}	
		}
	}
	
	
			/**
			 * <p>Load the current log cursor from a file</p>
			 * @throws IOException if file cannot be accessed.
			 */
	public void load() throws IOException {

		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(_logCursorFile);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = reader.readLine();
			Long value = Long.valueOf(line.trim());
			if(value == null) {
				throw new IOException("Incorrect ID in file");
			}
			_sinceID = value.longValue();
		}
		finally {
			if( reader != null ) {
				reader.close();
			}	
		}
	}

}
// ---------------------  EOF ----------------------------------
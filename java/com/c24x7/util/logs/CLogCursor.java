// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.c24x7.util.CEnv;


		/**
		 * <p>Helper class to keep track of cursor used to down load social messages such 
		 * as twitter or Facebook</p>
		 * @author Patrick Nicolas
		 */

public final class CLogCursor {
	protected String 	_logCursorFile = null;
	protected long 	_sinceID = -1L;
	protected int		_rowID = 0;
	
			/**
			 * <p>Create a log cursor to persist on a file
			 * @param logCursorFile file the log cursor persists.
			 */
	public CLogCursor(final String logCursorFile) {
		_logCursorFile = logCursorFile;
		_sinceID = 0L;
	}
	
	public final long getSinceID() {
		return _sinceID;
	}
	
	public void setSinceID(long sinceID) {
		_sinceID = sinceID;
	}
	
	public final int getRowID() {
		return _rowID;
	}
	
	
	public final boolean exists() {
		return (_sinceID != 0L);
	}
	
	
			/**
			 * <p>Save the current log cursor in file</p>
			 * @throws IOException if file cannot be accessed
			 */
	public void save(int rowID) throws IOException {
		BufferedWriter writer = null;
	
		try {
			FileOutputStream fos = new FileOutputStream(_logCursorFile);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			StringBuilder buf = new StringBuilder(String.valueOf(_sinceID));
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(String.valueOf(rowID));
			
			CLogger.info("Write ids: " + buf.toString());
			writer.write(buf.toString(), 0, buf.toString().length());
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
			String[] ids = line.trim().split(CEnv.KEY_VALUE_DELIM);
			if( ids == null ) {
				throw new IOException("Retrieve trends cursors in file");
			}
			
			Long sinceID = Long.valueOf(ids[0]);
			Integer rowID = Integer.valueOf(ids[1]);
			if( sinceID == null || rowID == null) {
				throw new IOException("Incorrect trends cursors");
			}
			_sinceID = sinceID.longValue();
			_rowID = rowID.intValue();
		}
		finally {
			if( reader != null ) {
				reader.close();
			}	
		}
	}

}
// ---------------------  EOF ----------------------------------
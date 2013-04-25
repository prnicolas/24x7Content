//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.semantics.dbpedia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.c24x7.semantics.dbpedia.etl.ADbpediaEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaGeolocEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaImageEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaLgAbstractEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaOntologyEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaRedirectsEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaShAbstractEtl;
import com.c24x7.util.logs.CLogger;




			/**
			 * <p>Utility class that build the dbpedia database from the datasets.</p>
			 * @author Patrick Nicolas
			 * @date 09/28/2011
			 */
public final class CDbpediaLoader {
	private final static long DISPLAY_INTERVAL = 10000L;
	private long _maxNumRecords = Long.MAX_VALUE;
	private long _startIndex = -1L;
	
	public CDbpediaLoader() {
		this(Long.MAX_VALUE);
	}

	public CDbpediaLoader(long maxNumRecords) {
		_maxNumRecords = maxNumRecords;
	}

	
	public void writeDbpediaDatabase() throws IOException {
		writeDbpediaDatabase(new CDbpediaLgAbstractEtl());
		writeDbpediaDatabase(new CDbpediaShAbstractEtl());	
		writeDbpediaDatabase(new CDbpediaRedirectsEtl());
		writeDbpediaDatabase(new CDbpediaOntologyEtl());
		writeDbpediaDatabase(new CDbpediaGeolocEtl());
		writeDbpediaDatabase(new CDbpediaImageEtl());
	}

	public void setStartIndex(int startIndex) {
		_startIndex = startIndex;
	}
	
			/**
			 * <p>Generic method top load the content of a file into dbpedia database</p>
			 * @param extractor ETL object to extract fields to dbpedia data sets and populate database.
			 * @throws IOException if the dbpedia data set files cannot be assessed.
			 */
	public void writeDbpediaDatabase(ADbpediaEtl etl) throws IOException {
		
		BufferedReader reader = null;
		CLogger.info("Load " + etl.getDbpediaFile());
		
		try {
			String contentFile = etl.getDbpediaFile();
			FileInputStream fis = new FileInputStream(contentFile);
			reader = new BufferedReader(new InputStreamReader(fis));
			long counter = 0L, 
				 eCounter =0L;
			String newLine = null;
			long startTime = System.currentTimeMillis();
			boolean started = false;
				
			while ((newLine = reader.readLine()) != null) {
						/*
						 * For local testing purpose, exit after few iterations only..
						 */
				if( isCompleted(counter) ) {
					break;
				}
						/*
						 * Counts the failed attempts to update the database.
						 */
				if( counter > _startIndex && !started) {
					CLogger.info("Start recording @ " + _startIndex);
					started = true;
				}
					
	 			if( started && !etl.map(newLine) ) {
					eCounter++;
				}
				displayProgress(counter++, startTime, 0);
			}
			
			reader.close();
			displayStats(counter++, startTime, eCounter);
			
			etl.reduce();
		}
		
		catch( SQLException e) {
			CLogger.error("Cannot load " + etl.getDbpediaFile() + " " + e.toString());
		}
		
		finally {
			if(reader != null) {
				reader.close();
			}
		}
	}
		
	
					// ----------------------------
					//  Private Supporting Methods
					// ----------------------------

		
	protected void displayProgress(long counter, long startTime, long eCounter) {		
		if(counter % DISPLAY_INTERVAL  ==  0) {
			displayStats(counter, startTime, eCounter);
		}
	}
	
	protected void displayStats(long counter, long startTime, long eCounter) {
		long curTime = System.currentTimeMillis();
		float duration = (curTime - startTime)*0.001F/60.0F;
		float average = counter/duration;
		
		StringBuilder buf = new StringBuilder(String.valueOf(counter));
		buf.append(": ");
		if( eCounter > -1L) {
			buf.append(eCounter);
			buf.append(" errors");
		}
		buf.append(" for");
		buf.append(duration);
		buf.append(" mins. with an average ");
		buf.append( (long)average);
		buf.append( " lines per minute");
			
		CLogger.info(buf.toString());
	}
	
	protected final boolean isCompleted(long counter) {
		return (counter > _maxNumRecords);
	}
	
}

// -------------------------------  EOF --------------------------------------
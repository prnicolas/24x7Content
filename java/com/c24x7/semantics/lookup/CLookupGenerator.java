// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.lookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


import com.c24x7.textanalyzer.stemmer.CPluralStemmer;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.semantics.lookup.loaders.CLookupAliasesLoader;
import com.c24x7.semantics.lookup.loaders.CLookupEntriesLoader;
import com.c24x7.semantics.lookup.loaders.ILookupLoader;


			/**
			 * <p>Cache to access dbpedia tables, implemented as a thread
			 * unsafe (read only) hash table. The key defines the label 
			 * entry in dbpedia table and the character specifies the
			 * actual table in dbpedia database.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 02/11/2012
			 */
public final class CLookupGenerator implements Runnable {
	protected static final long serialVersionUID = -9201271345717846508L;
	
	private static final int FREQUENCY_PROGRESS_DISPLAY = 2500;

	
			/**
			 * <p>Class that defines the lookup records map, implemented
			 * as a HashMap.</p>
			 * @author Patrick Nicolas
			 * @date 02/17/2012
			 */
	public class NLookupRecordsMap extends HashMap<String, CLookupRecord> {
		private static final int	INITIAL_MAP_SIZE	= 8192;
		private static final long serialVersionUID = -8407083597197772966L;
		
			/**
			 * <p>Constructor that initialize the size of the lookup records map.
			 */
		public NLookupRecordsMap() {
			super(INITIAL_MAP_SIZE);
		}
		
			/**
			 * <p>Add a thread safe capabilities to the insertion of 
			 * a new lookup records.
			 */
		public CLookupRecord put(String label, CLookupRecord lookupRecord) {
			displayProgress();
			CLookupRecord record = null;
			String convertedString = null;
			
			int indexSpace = label.indexOf(" ");
			if( indexSpace != -1) {
				String[] terms = label.split(" ");
				StringBuilder buf = new StringBuilder();
			
				int termsLastIndex = terms.length-1;
				for( int k = 0; k < termsLastIndex; k++) {
					convertedString = CStringUtil.allButFirstCharToLowerCase(terms[k]);
					buf.append( convertedString != null ? convertedString : terms[k]);
					buf.append(" ");
				}
				convertedString = CStringUtil.allButFirstCharToLowerCase(terms[termsLastIndex]);
				buf.append( convertedString != null ? convertedString : terms[termsLastIndex]);
				
				convertedString = buf.toString();
			}
			
			else {
				convertedString = CStringUtil.allButFirstCharToLowerCase(label);
				if( convertedString == null) {
					convertedString = label;
				}
			}
			
			String stemVersion = CPluralStemmer.getInstance().stem(convertedString);
			
			synchronized(this) {
				_recordCounter++;
				if(!containsKey(convertedString) && !containsKey(stemVersion) ) {
					record = super.put(convertedString, lookupRecord);
				}
				
			}
			return record;
		}

	}
	
	private class NExtractorThread extends Thread {
		private int _startRowId 	= 0;
		private int _endRowId 		= 0;
		private int _threadIndex 	= 0;
		private int _recordsCount 	= 0;
		private ILookupLoader _extractor = null;
		
			/**
			 * <p>Create an extractor thread to create a lookup map file from 
			 * DBpedia database tables.</p>
			 * @param extractor formatted loader for lookup data.
			 * @param startId id of starting row in DBpedia table
			 * @param endId id of last row in DBpedia table
			 * @param index index of the thread.
			 */
		private NExtractorThread(final ILookupLoader extractor, int startId, int endId, int index) {
			_extractor = extractor;
			_startRowId = startId;
			_endRowId = endId;
			_threadIndex = index;
		}
		
		
			/**
			 * <p>Coroutine for the thread that create a in-memory lookup 
			 * table from  Dbpedia table
			 */
		public void run() {
			for( int k = _startRowId; k <= _endRowId; k++) {
				try {
					if( _extractor.extract(k) ) {
						_recordsCount++;
					}
				}	
				catch( SQLException e) {
					CLogger.error("Cannot extract aliases data in thread " + _threadIndex + ": " + e.toString());
				}
			}
			_extractor.close();
		}
	}	
	
	
	private boolean				_completed 			= false;
	private int 				_recordCounter 		= 0;
	private List<NExtractorThread> 	_extractorsList 	= null;
	private NLookupRecordsMap  	_lookupRecordsMap 	= null;
	private String 				_lookupType 		= null;
	private long				_startTime			= 0L;
	private static Object		lock				= new Object();
	
	
			/**
			 * <p>Create a instance of the Lookup generation. The generation
			 * of the in-memory look up table is multi-threaded.
			 */
	public CLookupGenerator() {
		_extractorsList = new LinkedList<NExtractorThread>();
		_lookupRecordsMap = new NLookupRecordsMap();
	}
	


			/**
			 * <p>Main routine to create a map file for Wikipedia entries.
			 * The map file is then loaded in memory to build a lookup table.
			 * The table of aliases of wikipedia entries  is scanned first, 
			 * before the actual Wikipedia entries table is scanned so the entry 
			 * in the Wikipedia table prevails in case of conflict.</p> 
			 * @param lookupTable type of lookup table to be created.
			 * @return Number of entries in the lookup map file.
			 */
	
	public int createLookupMap(final String lookupType) {
				/*
				 * Retrieve the size of the Wikipedia entries
				 * and Wikipedia aliases table.
				 */
		boolean originalLookup = CLookup.isOriginalLookupType(lookupType);
		_lookupType = lookupType;
		
			/*
			 * Iterates through both the Dbpedia entries and aliases tables
			 */
		long maxAliases_id 	= CDbpediaSql.getInstance().getNumAliases(),
			max_id 			= CDbpediaSql.getInstance().getNumEntries();
		
		if( max_id != -1L && maxAliases_id != -1L) {
			_startTime = System.currentTimeMillis();
			
			final int numAliasesThreads = 2,
					 numEntriesThreads = 2;
			
			int interval 	= (int)((float)max_id/numEntriesThreads),
				startingId 	= 1,
				endingId 	= 1,
				threadIndex = 1;
			
			/*
			 * Build the list of threads to extract lookup data from
			 * the DBpedia aliases table.
			 */
			
			NExtractorThread extractingThread = null;
			ILookupLoader extractor = null;
			
			/*
			 * Create the threads that extract label and state from the 
			 * Wikipedia reference database and create the relevant lookup entry
			 */
			for( int k = 0; k < numEntriesThreads; k++) {
				endingId = (int)((k == numEntriesThreads-1) ? max_id : startingId + interval);
				extractor = new CLookupEntriesLoader(_lookupRecordsMap, originalLookup);
				extractingThread = new NExtractorThread(extractor, startingId, endingId, threadIndex);
				
				/*
				 * Add to the list of extraction threads that block the current
				 * method, before starting the extracting threads.
				 */
				_extractorsList.add(extractingThread);
				extractingThread.start();
				startingId = endingId + 1;
				threadIndex++;
			}
			
			/*
			 * Start this thread that wait for the extracting threads
			 * to complete execution.
			 */
			new Thread(this).start();
			
			/*
			 * Co-routine that waits for the extracting threads to complete.
			 */
			while( !_completed) {
				try {
					Thread.sleep(20000);
				}
				catch( InterruptedException e) {
					CLogger.error("Fail to block waiting for aliases lookup");
				}
			}

			_extractorsList = new LinkedList<NExtractorThread>();
			
			interval 	= (int)((float)maxAliases_id /numAliasesThreads);
			startingId 	= 1;
			endingId 	= 1;

			/*
			 * Create the threads that extract label and state from the 
			 * Wikipedia reference database and create the relevant lookup 
			 * entry associated with the aliases table.
			 */
			for( int k = 0; k < numAliasesThreads; k++) {
				endingId = (int)((k == numAliasesThreads-1) ? maxAliases_id : startingId + interval);
				extractor = new CLookupAliasesLoader(_lookupRecordsMap, originalLookup);
				extractingThread = new NExtractorThread(extractor, startingId, endingId, threadIndex);
				
				/*
				 * Add to the list of extraction threads that block the current
				 * method, before starting the extracting threads.
				 */
				_extractorsList.add(extractingThread);
				extractingThread.start();
	
				startingId = endingId + 1;
				threadIndex++;
			}
			
			new Thread(this).start();
		}
		
		return 1;
	}
	
	
		/**
		 * <p>Implementation to the main controlling thread.</p>
		 */
	public void run() {
		for(NExtractorThread extractor : _extractorsList ) {
			try {
				extractor.join();
			}
			catch( InterruptedException e) {
				CLogger.error("Failed to generate Lookup Map " + e.toString());
			}
		}
		
		if( _completed ) {
			createMapFile();
			createLowerCaseMapFile();
		}
		_completed = true;
	}
	
		
							// ---------------------------
							//  Supporting Private Methods
							// ---------------------------
	
	
	
	
		/**
		 * <p>Convert the content of the lookup table to a Lower case character.</p>
		 * @return number of records in the lookup table.
		 */
	private int createLowerCaseMapFile() {	
		int numRecords = 0;
		BufferedReader reader = null;
		
		try {
			StringBuilder buf = new StringBuilder();
			
			/*
			 * load the lookup table from file into memory.
			 */
			FileInputStream fis = new FileInputStream(_lookupType);
			reader = new BufferedReader(new InputStreamReader(fis));
			String newLine = null;
				
			while ((newLine = reader.readLine()) != null) {	
				newLine = newLine.trim();
				buf.append(newLine.toLowerCase());
				buf.append("\n");
				numRecords++;
			}
			
			reader.close();
			reader = null;
			
			/*
			 * Save the lower case characters version into file
			 */
			int indexLabelsFile = _lookupType.indexOf("labels");
			if( indexLabelsFile != -1) {
				StringBuilder filenameBuf = new StringBuilder(_lookupType.substring(0,indexLabelsFile));
				filenameBuf.append("l");
				filenameBuf.append(_lookupType.substring(indexLabelsFile));
				CFileUtil.write(filenameBuf.toString(), buf.toString());
			}
			else {
				numRecords = 0;
				CLogger.error("Failed to create label lookup table file");
			}
		}
	
		catch( IOException e) {
			CLogger.error("Failed to convert Lookup table to lowercase " + e.toString());
		}
		finally {
			if( reader != null) {
				try {
					reader.close();
				}
				catch( IOException e) {
					CLogger.error("Failed to convert Lookup table to lowercase " + e.toString());
				}
			}
		}
		
		return numRecords;
	}


	private void createMapFile() {
		try {
			StringBuilder buf = new StringBuilder();
			
			CLookupRecord record = null;
			for( String keyword : _lookupRecordsMap.keySet()) {
				record = _lookupRecordsMap.get(keyword);
				buf.append(keyword);
				buf.append(CEnv.KEY_VALUE_DELIM);
				buf.append(record.getType());
				buf.append(CEnv.KEY_VALUE_DELIM);
				buf.append(record.getIdf());
				buf.append("\n");
			}
			
			CFileUtil.write(_lookupType, buf.toString());
		}
		catch( IOException e) {
			CLogger.error("Cannot create dbpedia lookup map " + e.toString());
		}
	}
	
	private void displayProgress() {
		synchronized( lock) {
			if(_recordCounter % FREQUENCY_PROGRESS_DISPLAY == 0) {
				double duration = (System.currentTimeMillis() - _startTime)*0.001;
				System.out.println("Number of records: " + _recordCounter + " - " + String.valueOf(duration) + " secs.");
			}
		}
	}

}

// ------------------------  EOF -------------------------------------------------

// Copyright (c) 2010-2012 Patrick Nicolas
package com.c24x7.util.logs;


import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.Level;

import test.c24x7.nlservices.TAnalyzer;

import com.c24x7.apps.CDbpediaApp;
import com.c24x7.apps.CTaxonomyTrainApp;
import com.c24x7.apps.CTopicTrainApp;
import com.c24x7.util.CEnv;



		/**
		 * <p>
		 * Generic logger class that wraps Apache Log4J logging classes</p>
		 * @author Patrick Nicolas
		 */

public final class CLogger {
	public final static String TOPIC_TRAIN_TRACE =  CTopicTrainApp.class.getName();
	public final static String TAXONOMY_TRAIN_TRACE =  CTaxonomyTrainApp.class.getName();
	public final static String SEMANTIC_SERVICE_TRACE = TAnalyzer.class.getName();
	public final static String DBPEDIA_SERVICE_TRACE = CDbpediaApp.class.getName();
	
		/**
		 * <p>Enumerator for Logging mode.</p>
		 * @author Patrick Nicolas
		 * @date 10/05/2011
		 */
	public static enum MODE {
		NO_LOG, ALL_LOG, INFO_LOG, ERROR_LOG
	}
	
	private static Logger 	logger = null;
	private static boolean	stdOut = false;
	
	protected static final int LENGTH_LOG_NUMBER_STR = 4;
	

	
	public static void setLoggerInfo(final String className) {
		logger = Logger.getLogger(className);
		logger.setLevel(Level.ALL);
		addAppender(CEnv.logsDir + "debug/", null);
	}
	
			/**
			 * <p>Allow any information to be logged.</p>
			 */
	public static void setLoggerInfo() {
		if( logger != null ) {
			logger.setLevel(Level.INFO);
		}
	}	
	
	public static void setStdOut() {
		stdOut = true;
	}
	
			/**
			 * <p>Allows information related to a specific trace to be logged.</p>
			 * @param newTrace
			 */
	public static void setLoggerInfo(int newTrace) {
		if( logger != null ) {
			logger.setLevel(Level.INFO);
		}
	}
	

	public static boolean isLoggerInfo() {
		return (logger != null) ? (logger.getLevel() == Level.INFO) : stdOut;
	}
	
	
	
	public static void addAppender() {
		addAppender(CEnv.logsDir + "debug/", null);
	}
	
	/**
	 * <p>
	 * Generic method to create a new log file with a stamp stamp.</p>
	 * @param dirName directory the logs will be created
	 */
	public static void addAppender(String dirName) {
		addAppender(dirName, null);
	}
	
			/**
			 * <p>
			 * Generic method to append data to an existing file. If a filename is provided then
			 * the log is appended to the existing debug file. If no file name is provided null, then
			 * a new file is created in the directory with a new time stamp.</p>
			 * @param dirName directory the logs will be created
			 * @param fileName name of the file generated
			 */
	public static void addAppender(String dirName, String fileName) {
		FileAppender appender = null;

		try {
				/*
				 * if the logger has been instantiated... 
				 */
			if(logger != null) {
				boolean newFile = (fileName == null);
					/*
					 * If the debug file has not been provided..
					 */
				if(newFile) {
					fileName = createFileName(dirName);
					appender = new FileAppender(new SimpleLayout(), fileName, true);
				}
					/*
					 * otherwise append to an existing log file.
					 */
				else {
					fileName = dirName+fileName;
					File logFile = new File(fileName);
					
					/*
					 * Create the log file if it does not exists.
					 */
					boolean logFileExist = logFile.exists();
					if(!logFileExist) {
						logFile.createNewFile();
					}
					appender = new FileAppender(new SimpleLayout(), fileName, logFileExist);
				}
				
				logger.addAppender(appender);
				if( !newFile) {
					logger.info(createTimeStamp(true));
				}
			}
		}
		catch(Exception e) {
			CLogger.error("Cannot initiate logs:" + e.toString());
		}
	}
	
	
		/**
		 * <p>Write the info to the existing log.</p>
		 * @param s string information to be written into log.
		 */
	public static void info(final String s) {
		if( stdOut ) {
			System.out.println(s);
		}
		if( logger != null && logger.isInfoEnabled() ) {
			logger.info(s);
		}
	}
	
	

	
		/**
		 * <p>Write the info to the existing log.</p>
		 * @param s string information to be written into log.
		 * @param infoTrace specific tracer to display
		 */
	public static void info(final String s, final String className) {
		if(logger.getName() != null && logger.getName().compareTo(className) == 0) {

			if( logger != null && logger.isInfoEnabled() ) {
				logger.info(s);
			}
			System.out.println(s);
		}
	}
	
		/**
		 * <p>Write the error message to the existing log or onto the
		 * standard output if stdOut attribute is set</p>
		 * @param s error message to be written into log.
		 */
	public static void error(final String s) {
		if( logger != null ) {
			logger.error(s);
		}
		System.out.println(s);
	}
	
	/**
	 * <p>Write the warning message to the existing log.</p>
	 * @param s warning message to be written into log.
	 */
	public static void warn(final String s) {
		if( logger != null && logger.isInfoEnabled() ) {
			logger.warn(s);
		}
	}
	
	public static void error(Throwable e) {
		e.printStackTrace();
		
		if( logger == null || stdOut ) {
			System.out.println(e.toString());
		}
		else {
			logger.error(e.toString());
		}
	}
	

			/**
			 * Retrieve the location of the different log files
			 * @return Hash table of the location of all the logs
			 */
	
	@SuppressWarnings("unchecked")
	public static Map<String,String> getLogLocations() {
		Collection<Logger> allLoggers = new ArrayList<Logger>();
		Logger rootLogger = Logger.getRootLogger();
		allLoggers.add(rootLogger);
		
		for (Enumeration<Logger> loggers =
			rootLogger.getLoggerRepository().getCurrentLoggers() ;
			loggers.hasMoreElements() ; ) {
			allLoggers.add(loggers.nextElement());
		}
		
		Set<FileAppender> fileAppenders = new LinkedHashSet<FileAppender>();
	
		for (Logger logger : allLoggers) {	
			for (Enumeration<Appender> appenders = logger.getAllAppenders() ;
		           				appenders.hasMoreElements() ; ) {

				Appender appender = appenders.nextElement();
				if (appender instanceof FileAppender) {
					fileAppenders.add((FileAppender) appender);
				}	
			}
		}
		Map<String, String> locations = new LinkedHashMap<String,String>();
		for (FileAppender appender : fileAppenders) {
			locations.put(appender.getName(), appender.getFile());
		}
		return locations;
	}
	
	
			/**
			 * <p>Create a final name with a time stamp with the default prefix run_</p>
			 * @param dirName absolute directory of the file created
			 * @return absolute path file name
			 */
	public static String createFileName(final String dirName) {
		return createFileName(dirName, "run_", "txt");
	}
	
			/**
			 * <p>Create a final name with a time stamp with a prefix specified by the user</p>
			 * @param dirName absolute directory of the file created
			 * @param prefix  prefix for the relative path names
			 * @param extension file extension or type (.txt, .html, .xml, .json, ..)
			 * @return absolute path file name
			 */
	public static String createFileName(final String dirName, 
										final String prefix, 
										final String extension) {
		
		StringBuilder buf = new StringBuilder(dirName);
		buf.append(prefix);
		
		String logFileNumStr = logFileNumberStr(dirName);
		if( logFileNumStr != null ) {
			buf.append(logFileNumStr);
		}

		buf.append(createTimeStamp(true));
		buf.append(".");
		buf.append(extension);
		return buf.toString();
	}
	
	
					// ----------------
					//  Private Methods
					// ------------------
	
	public static String createTimeStamp(boolean longTimeStamp) {
		Calendar cal = Calendar.getInstance();
		
		StringBuilder buf = new StringBuilder();
		buf.append(cal.get(Calendar.MONTH)+1);
		buf.append("_");
		buf.append(cal.get(Calendar.DAY_OF_MONTH));
		buf.append("_");
		buf.append(cal.get(Calendar.HOUR_OF_DAY));
		if( longTimeStamp ) {
			buf.append("_");
			buf.append(cal.get(Calendar.MINUTE));
			buf.append("_");
			buf.append(cal.get(Calendar.SECOND));
		}
		
		return buf.toString();
	}
		
	
	
	protected static String logFileNumberStr(String logDirName) {	
		String logNumStr = null;
		
		File logDir = new File(logDirName);
		if( logDir.isDirectory()) {
			int numberOfFile = logDir.listFiles().length +1;
			String numberStr = String.valueOf(numberOfFile);
			int numRemainingZero = LENGTH_LOG_NUMBER_STR - numberStr.length();
		
			StringBuilder buf = new StringBuilder();
			for( int j = 0; j < numRemainingZero; j++ ) {
				buf.append("0");
			}
			buf.append(numberStr);
			buf.append("_");
			logNumStr = buf.toString();
		}
		
		return logNumStr;
	}
	

}

// ------------------------  EOF ------------------------------------

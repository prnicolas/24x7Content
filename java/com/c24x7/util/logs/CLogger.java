// Copyright (c) 2010-2011 Patrick Nicolas
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



		/**
		 * <p>
		 * Generic logger class that wraps Apache Log4J logging classes</p>
		 * @author Patrick Nicolas
		 */

public final class CLogger {
		/**
		 * 
		 * @author Patrick
		 *
		 */
	public static enum MODE {
		NO_LOG, ALL_LOG, INFO_LOG, ERROR_LOG
	}
	
	public static Logger 	logger = null;
	public static String 	fileName = null;
	public static boolean	stdOut = false;
	
	private static final int LENGTH_LOG_NUMBER_STR = 4;
	
			/**
			 * <p>
			 * Initialize the logger parameters...</p>
			 * @param appClass class of objects managing the log
			 * @param mode mode of the Java application
			 */
	public static void setLoggerClass(Class<?> appClass, MODE mode) {
		if( mode != MODE.NO_LOG) {
			logger = Logger.getLogger(appClass);
			logger.setLevel(mode != MODE.ERROR_LOG ? Level.ALL : Level.ERROR );
			stdOut = (mode == MODE.INFO_LOG);
		}
	}

	
	public static void setLoggerClassName(String name, MODE mode) {
		if( mode != MODE.NO_LOG) {
			logger = Logger.getLogger(name);
			logger.setLevel(mode != MODE.ERROR_LOG ? Level.ALL : Level.ERROR );
			stdOut = (mode == MODE.INFO_LOG);
		}
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
					logger.info(createTimeStamp());
				}
			}
		}
		catch(Exception e) {
			System.out.println("Cannot initiate logs:" + e.toString());
		}
	}
	
			/**
			 * <p>Write the info to the existing log.</p>
			 * @param s string information to be written into log.
			 */
	public static void info(final String s) {
		if( logger != null && logger.isInfoEnabled() ) {
			logger.info(s);
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

		buf.append(createTimeStamp());
		buf.append(".");
		buf.append(extension);
		return buf.toString();
	}
	
	
					// ----------------
					//  Private Methods
					// ------------------
	
	private static String createTimeStamp() {
		Calendar cal = Calendar.getInstance();
		
		StringBuilder buf = new StringBuilder();
		buf.append(cal.get(Calendar.MONTH)+1);
		buf.append("-");
		buf.append(cal.get(Calendar.DAY_OF_MONTH));
		buf.append("_");
		buf.append(cal.get(Calendar.HOUR_OF_DAY));
		buf.append("-");
		buf.append(cal.get(Calendar.MINUTE));
		buf.append("-");
		buf.append(cal.get(Calendar.SECOND));
		
		return buf.toString();
	}
	
	
	private static String logFileNumberStr(String logDirName) {	
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

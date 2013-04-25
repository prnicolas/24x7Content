// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.InitException;
import com.c24x7.models.topics.CTopicClassifier;
import com.c24x7.semantics.CTaxonomyRoots;
import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.textanalyzer.CNGramsExtractor;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Encapsulate the configuration and login variable for a specific user. </p>
		 * @author Patrick Nicolas
		 * @date 11/05/2010
		 */
public final class CEnv {
	public final static String APP_LABEL 		= "app";
	public final static String FACEBOOK_LABEL 	= "facebook";
	public final static String TWITTER_LABEL 	= "twitter";
	public final static String TRENDS_LABEL 	= "trends";
	
	protected final static String PROJECT_PATH 			= "c:/Users/Patrick/workspace/24x7c/";
	protected final static String LOCAL_OUTPUT_DIR		= "output/";
	protected final static String LOCAL_DEBUG_DIR		= "debug/";
	protected final static String LOCAL_FTP_DIR 		= "input/ftp/";
	protected final static String LOCAL_LOGS_DIR		= "logs/";
	protected final static String LOCAL_TWITTER_DIR 	= "twitter/";
	protected final static String LOCAL_MODELS_DIR 		= "models/";
	protected final static String LOCAL_CONFIG_DIR 		= "config/";
	protected final static String LOCAL_TRAINING_DIR  	= "training/";
	protected final static String LOCAL_DICT_DIR		= "dict/";
	protected final static String LOCAL_CORPUS_DIR		= "dict/";
	protected final static String LOCAL_CONFIG_FILE   	= "config";
	protected final static String LOCAL_DATASETS_DIR   	= "datasets/";

	
	public static String projectDir 	= PROJECT_PATH;
	public static String configDir 		= projectDir + LOCAL_CONFIG_DIR;
	public static String modelsDir  	= projectDir + LOCAL_MODELS_DIR;
	public static String trainingDir 	= projectDir + LOCAL_TRAINING_DIR;
	public static String cacheDir 		= projectDir + "temp/users/";
	public static String dictDir 		= modelsDir + LOCAL_DICT_DIR;
	public static String logsDir    	= projectDir + LOCAL_LOGS_DIR;
	public static String outputDir  	= projectDir + LOCAL_OUTPUT_DIR;
	public static String corpusDir  	= trainingDir + LOCAL_CORPUS_DIR;
	public static String debugDir   	= outputDir + LOCAL_DEBUG_DIR;
	public static String twitterlogsDir = logsDir + LOCAL_TWITTER_DIR;
	public static String localftpDir 	= projectDir + LOCAL_FTP_DIR;
	public static String configFile		= configDir + LOCAL_CONFIG_FILE;
	public static String datasetsDir	= projectDir + LOCAL_DATASETS_DIR;

	public static final float	UNINITIALIZED_FLOAT 	= -1.0F;
	public static final double	UNINITIALIZED_DOUBLE 	= -1.0;
	public static final int		UNINITIALIZED_INT   	= -1;
	
	public static final String	KEY_VALUE_DELIM 			= ":";
	public static final String  FIELD_DELIM 				= ",";
	public static final String 	ENTRIES_DELIM 				= "##";
	public final static String 	ENCODED_ENTRY_DELIM 		= "%23%23";
	public static final String  ENTRY_FIELDS_DELIM 			= "#";
	public final static String 	ENCODED_ENTRY_FIELDS_DELIM 	= "%23";
	public final static String 	TAXONOMY_FIELD_DELIM 		= "/";

	
	protected static Map<String, Map<String, String>>   appConfig = null;
	
		
			/**
			 * <p>Load the configuration file, setup the different directories and 
			 * initialize all configuration variables and data structures used in the application</p>
			 * @return true if successful, false otherwise.
			 */
	public static boolean init() throws InitException { 
		boolean success = true;
		
		if( appConfig == null ) {
			appConfig = load();
				
			setup();
			System.out.println(" \n\n**********************  Configuration ******************** ");
			CNGramsExtractor.init(); 
			CLookup.init(CLookup.EXTENDED);
			CTaxonomyRoots.init();
			CTopicClassifier.init();
			System.out.println(" ********************************************************** ");
		}
		
		return success;
	}
	
	
	
	public final static String get(final String group, final String key) {
		if( key == null ) {
			throw new NullPointerException("Undefined Environment key");
		}		
		
		return appConfig.containsKey(group) ? 
			   appConfig.get(group).get(key) :
			   null;
	}
	
	public final static Map<String, String> getConfiguration(final String group) {
		if( group == null ) {
			throw new NullPointerException("Undefined configuration group");
		}
		return appConfig.get(group); 
	}

	
	
					// ----------------------
					//  Private Methods
					// ----------------------
	
	protected static Map<String, Map<String,String>> load() {
		BufferedReader reader = null;
		String line = null;
		Map<String,  Map<String, String>> config = null;

		try {
		
			FileInputStream fis = new FileInputStream(configFile);
			reader = new BufferedReader(new InputStreamReader(fis));
			String[] keyValues = null;
			Map<String, String> subConfig = null;
			String groupName = null;
			config = new HashMap<String,  Map<String, String>>();
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					if(line.charAt(0) == ':') {
						if( subConfig != null) {
							config.put(groupName, subConfig);
						}
						subConfig = new HashMap<String, String>();
						groupName = line.substring(1);
					}
					else {
						keyValues = line.split(FIELD_DELIM);
						subConfig.put(keyValues[0].trim(), keyValues[1].trim());
					}
				}
			}
			config.put(groupName, subConfig);
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Cannot load configuration file " + configFile);
		}
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) {
					CLogger.error("Cannot load configuration file " + configFile);
				}
			}
		}
		
		return config;
	}
	
	
	

	protected static boolean setup() {
		projectDir 	= get("setup", "project");
		
		if(projectDir != null) {
			configDir 		= projectDir + LOCAL_CONFIG_DIR;
			modelsDir  		= projectDir + LOCAL_MODELS_DIR;
			cacheDir 		= projectDir + "temp/users/";
			dictDir 		= CEnv.modelsDir + LOCAL_DICT_DIR;
			logsDir    		= projectDir + LOCAL_LOGS_DIR;
			outputDir  		= projectDir + LOCAL_OUTPUT_DIR;
			corpusDir  		= modelsDir + LOCAL_CORPUS_DIR;
			debugDir   		= outputDir + LOCAL_DEBUG_DIR;
			twitterlogsDir 	= logsDir + LOCAL_TWITTER_DIR;
			localftpDir 	= projectDir + LOCAL_FTP_DIR;
		}
		
		return (projectDir != null);
	}

	
}

//-------------------  EOF ------------------------------
// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Encapsulate the configuration and login variable for a specific user. </p>
		 * @author Patrick Nicolas
		 * @date 11/05/2010
		 */
public final class CEnv {
	public final static String APP_LABEL 		= "app";
	public final static String USER_LABEL 		= "user";
	
	public final static String PROJECT_DIR 		= "c:\\Users\\Patrick\\workspace\\24x7c\\";
	public final static String INPUT_PATH		= "input\\";
	public final static String OUTPUT_PATH		= "output\\";
	public final static String DEBUG_PATH		= OUTPUT_PATH + "debug\\";
	public final static String LOCAL_FTP_PATH 	= "input\\ftp\\";
	public final static String LOGS_DIR			= "logs\\";
	public final static String TWITTER_LOGS_DIR = LOGS_DIR + "twitter\\";
	public final static String CONFIG_FILE 		= "config.txt";
	public final static int	   DEFAULT_PORT		= 18000;
 
	public static int    port 					= DEFAULT_PORT;
	public static String projectDir 			= PROJECT_DIR;
	public static String configDir 				= projectDir + "config\\";
	public static String userConfigDir 			= configDir  + "users\\";
	public static String cacheDir 				= projectDir + "temp\\users\\";

	public static void init(final String directory, int newport) {
		port = newport;
		projectDir = directory;
		configDir = projectDir + "config\\";
		userConfigDir  = configDir + "users\\";
		cacheDir = projectDir + "temp\\users\\";
	}
	
	private static final String FIELD_DELIM 	= ",";
	private static final String PROP_DELIM 		= ".";
	
	private String							  _userId = null;
	private Map<String, Map<String, String>>  _appConfig = null;
	private Map<String, Map<String, String>>  _userConfig = null;
	private Map<String, Map<String, Map<String, String>>> _config = null;

	public CEnv() {
		this("demo");
	}
	


			/**
			 * <p>Create an environment for a specific user.</p>
			 * @param userId user unique ID
			 */
	public CEnv(final String userId) {
		_userId = userId;
		_appConfig = load(configDir + CONFIG_FILE);
		_userConfig = load(userConfigDir + userId);
		
		_config = new HashMap<String, Map<String, Map<String, String>>>();
		_config.put(APP_LABEL, _appConfig);
		_config.put(USER_LABEL, _userConfig);
	}
	
	public final String get(final String config, final String key) {
		if( key == null ) {
			throw new NullPointerException("Undefined Environment key");
		}		
		int index = key.indexOf(PROP_DELIM);
		
		return _config.containsKey(config) ? 
			   _config.get(config).get(key.substring(0, index)).get(key.substring(index+1)) :
			   null;
	}
	
	public final Map<String, String> getConfiguration(final String config, final String group) {
		if( group == null ) {
			throw new NullPointerException("Undefined configuration group");
		}
		return _config.containsKey(config) ? 
			  _config.get(config).get(group) :
			  null;
	}

	
	
	public final Map<String, Map<String, String>> getConfig(final String configName) {
		return _config.get(configName);
	}
	

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("Configuration\n");
		buf.append(getConfigString(APP_LABEL));
		buf.append("\n");
		buf.append(getConfigString(USER_LABEL));

		return buf.toString();
	}
	
	
	private String getConfigString(final String name) {
		Map<String, String> subConfig = null;
		Map<String, Map<String, String>> config = getConfig(name);
		String configDescription = null;
		
		if( config != null) {
			StringBuilder buf = new StringBuilder("\nConfiguration: " + name);
		
			for( String attr : config.keySet()) {
				subConfig = config.get(attr);
				buf.append("\nGroup=");
				buf.append(attr);
			
				for( String key : subConfig.keySet()) {
					buf.append("\nKey=");
					buf.append(key);
					buf.append(", value=");
					buf.append(subConfig.get(key));
				}
			}
			configDescription = buf.toString();
		}
		
		return configDescription;
	}

	
	
					// ----------------------
					//  Private Methods
					// ----------------------
	
	private Map<String, Map<String,String>> load(final String configFile) {
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
	
}

//-------------------  EOF ------------------------------
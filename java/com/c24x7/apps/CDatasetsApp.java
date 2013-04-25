// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;



				/**
				 * <p>Command line application that extracts statistics
				 * or search for specific items in Dbpedia datasets..
				 * CMD: com.c24x7.apps.CTaxonomyExtractorUtilApp [operation] [scope] [pattern]</p>
				 * @author Patrick Nicolas
				 * @date 10/25/2011
				 */
public final class CDatasetsApp {
	protected static final long INTERVAL_DISPLAY = 25000000L;
	protected static Map<String, String> datasetsMap = null;

	static {
		datasetsMap = new HashMap<String, String>();
		
		datasetsMap.put("geoloc", CEnv.datasetsDir + "dbpedia/geo_coordinates_en.nt");
		datasetsMap.put("ontology", CEnv.datasetsDir + "dbpedia/instance_types_en.nt");
		datasetsMap.put("redirect", CEnv.datasetsDir + "dbpedia/redirects_en.nt");
		datasetsMap.put("shabstract", CEnv.datasetsDir + "dbpedia/short_abstracts_en.nt");
		datasetsMap.put("lgabstract", CEnv.datasetsDir + "dbpedia/long_abstracts_en.nt");
		datasetsMap.put("categories", CEnv.datasetsDir + "dbpedia/article_categories_en.nt");
		datasetsMap.put("image", CEnv.datasetsDir + "dbpedia/image_en.nt");
		datasetsMap.put("yago", CEnv.datasetsDir + "dbpedia/yago_links.nt");
	}
	
	public static void main(String[] args) {
		CLogger.setLoggerInfo();
		
		StringBuilder buf = new StringBuilder("CDbpediaUtilApp");
		int k = 0;
		for( k = 0; k < args.length; k++) {
			if( args[k] == null) {
				break;
			}
			buf.append(" ");
			buf.append(args[k]);
		}
		CLogger.info(buf.toString());
		
		if( args[0] == null || args[0].compareTo("-help") == 0) {
			printHelp();
		}
		else {
			String operation = args[0],
			       scope = null,
			       pattern = null;
			int pos = -1;
			
			if(args[1] != null ) {
				scope = args[1];
				if( args.length > 2 && args[2] != null ) {
					StringBuilder patternBuf = new StringBuilder();
					for( k = 2; k < args.length-1; k++) {
						if( args[k] == null) {
							break;
						}
						patternBuf.append(args[k]);
						patternBuf.append(" ");
					}
					pos = Integer.parseInt(args[args.length-1]);
					pattern = patternBuf.toString().trim();
				}
			}
			if(operation.compareTo("-count") == 0) {
				count(scope);
			}
			else if( operation.compareTo("-find") == 0) {
				find(scope, pattern, pos);
			}
			else if( operation.compareTo("-list") == 0) {
				list(scope, pattern);
			}
		}
	}
	
					// --------------------------
					// Private supporting methods
					// --------------------------
	
	protected static void printHelp() {
		CLogger.info("CTaxonomyExtractorUtilApp operation scope pattern");
		CLogger.info("   operation: -help -count -find, -list");
		CLogger.info("   scope: all, geoloc, ontology, shabstract, lgabstract, image, yago");
		CLogger.info("   pattern: (part of speech the operation is performed for)");
	}
	
	protected static void count(final String scope) {
		if( scope != null ) {
			if( datasetsMap.containsKey(scope)) {
				CLogger.info("Count for " + scope + " is " + CFileUtil.countLines(datasetsMap.get(scope), INTERVAL_DISPLAY));
			}
		}
		else {
			for(String scopeEl : datasetsMap.keySet()) {
				count(scopeEl);
			}
		}
	}
	
	protected static void find(final String scope, final String pattern) {
		find(scope, pattern, -1);
	}
	
	protected static void find(final String scope, final String pattern, int pos) {		
		if( scope != null && pattern != null) {
			List<String> records = null;
			
			if( datasetsMap.containsKey(scope)) {
				CLogger.info("Search for " + pattern + " in " + scope);
				long[] count = new long[2];
				records = CFileUtil.find(datasetsMap.get(scope), pattern, pos, count);
				for( String line: records) {
					CLogger.info(records.size() >0  ? pattern + " is found in " + scope + " rank=" + count[0] + " of a total of " + count[1] + "\nLine: " + line : pattern + " not found");
				}
			}
		}
		else {
			for(String scopeEl : datasetsMap.keySet()) {
				count(scopeEl);
			}
		}
	}
	
	protected static void list(final String scope, final String pattern) {
		
		if( datasetsMap.containsKey(scope)) {
			final String inputFile = datasetsMap.get(scope);
		
			Map<String, Object> map= new HashMap<String, Object>();
			BufferedReader reader = null;
			
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
				String line = null;
				String label = null;
				
				while((line = reader.readLine()) != null) {
					line = line.trim();
					int indexPatternStart = line.indexOf(pattern);
					if( indexPatternStart != -1) {
						int indexPatternEnd = line.indexOf(">", indexPatternStart);
						if( indexPatternEnd != -1) {
							label = line.substring(indexPatternStart+pattern.length(), indexPatternEnd);
							map.put(label, null);
						}
					}
				}
				StringBuilder buf = new StringBuilder();
				for( String key : map.keySet()) {
					buf.append(key);
					buf.append("\n");
				}
				
				StringBuilder outputFile = new StringBuilder(CEnv.configDir);
				outputFile.append(scope);
				outputFile.append(".lst");
				CFileUtil.write(outputFile.toString(), buf.toString());
			}
			catch( IOException e) {
				CLogger.error(e.toString());
			}
			finally {
				if( reader != null) {
					try {
						reader.close();
					}
					catch( IOException e) {
						CLogger.error(e.toString());
					}
				}
			}
		
		}
	}
}

// -------------------------------  EOF ----------------------------------

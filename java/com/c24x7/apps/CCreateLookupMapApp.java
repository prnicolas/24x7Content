// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;


import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.semantics.lookup.CLookupGenerator;
import com.c24x7.textanalyzer.tfidf.CIdfVector;
import com.c24x7.util.logs.CLogger;




			/**
			 * <p>Command line application that creates a relative Term Frequency File. The
			 * client has the option to include one, or more NGram into the relative frequency
			 * vector (or map)></p>
			 * @author Patrick Nicolas
			 * @date  12/08/2011
			 */

public final class CCreateLookupMapApp {

	public static void main(String[] args) {			
		if( args != null && args.length > 0) {
			
				/*
				 * Update the Semantic database with IDF values.
				 */
			if(args[0].compareTo("-idf")==0) {
				long numRecords = CIdfVector.writeDatabase();		
				System.out.println("IDF initialization done for " + numRecords);
			}
			
				/*
				 * Create a lookup map file with lower case keywords
				 * of the command line is defined as -lookup -l or original
				 * keywords if -lookup -o
				 */
			else if( args[0].compareTo("-lookup")==0){
				final String lookupType = (args.length > 1 && args[1].compareTo("extended")==0) ?
											CLookup.EXTENDED : 
											CLookup.ORIGINAL;
				
				CLookupGenerator generator = new CLookupGenerator();
				generator.createLookupMap(lookupType);
			}
							
			else {
				CLogger.info("Command line arguments:\nCCreateLookupMapApp [arg]  arg: -idf or -lookup -l/-o");
			}
		}
		else {
			CLogger.info("Command line arguments:\nCCreateLookupMapApp [arg]  arg: -idf or -lookup -l/-o");
		}
	}
}

// ----------------  EOF --------------------------------------------

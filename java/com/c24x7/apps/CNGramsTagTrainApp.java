// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;


import com.c24x7.exception.InitException;
import com.c24x7.textanalyzer.CTaggedNGramsExtractor;
import com.c24x7.models.tags.CTagsClassifier;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;

		/**
		 * <p>Application to train a model to extract NGrams from a documents.</p>
		 * @author Patrick Nicolas
		 * @date 01/12/2012 
		 */
public final class CNGramsTagTrainApp {
	
	public static void main(String[] args) {

		if( args.length > 0 && args[0] != null) {
			if( args[0].compareTo("-train")==0) {
				try {
					train();
				}
				catch( InitException e) {
					CLogger.error(e.toString());
				}
			}
			if( args[0].compareTo("-validate")==0) {
				validate();
			}
			else {
				CLogger.error("Error: Command line for NGramsTag Training is CNGramsTagTrainApp [-train/-validate]");
			}
		}
		else {
			CLogger.error("Error: Command line for NGramsTag Training is CNGramsTagTrainApp [-train/-validate]");
		}
	}	
	
	protected static void train() throws InitException {
		CLogger.setLoggerInfo();
		CLogger.info("Train N-Grams Model");
		
		if(CEnv.init()) {
			CTagsClassifier nGramClassifier = new CTagsClassifier();
			
			int numRecords = nGramClassifier.train(5000, 98000);
			CLogger.info(numRecords + " records used in training");
		}
	}
	
	private static void validate() {
		try {
			CTaggedNGramsExtractor.init();
			CTagsClassifier nGramClassifier = new CTagsClassifier();
			nGramClassifier.validate();
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
	}
}
// -----------------------  eof ----------------------------------
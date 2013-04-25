// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;

import com.c24x7.semantics.dbpedia.etl.CDbpediaWordnetEtl;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Command line to extract WordNet Hypernyms and create
		 * taxonomy lineage for Wikipedia entries which have a corresponding
		 * Synset.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 01/22/2012
		 */
public final class CWordnetOntologyApp {
	
	public static void main(String[] args) {
		CLogger.setLoggerInfo();
		int numRecords = CDbpediaWordnetEtl.getWordnetOntologyLists();
		CLogger.info(String.valueOf(numRecords) + " ontology records have been retrieved");
	}

}

// ---------------------------------  EOF ------------------------------------------------

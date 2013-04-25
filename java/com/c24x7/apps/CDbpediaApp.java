// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


import com.c24x7.exception.InitException;
import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.nlservices.CAbstractSemanticService;
import com.c24x7.semantics.dbpedia.CDbpediaLoader;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.semantics.dbpedia.etl.CDatasetExtractor;
import com.c24x7.semantics.dbpedia.etl.ADbpediaEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaCategoriesEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaGeolocEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaImageEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaLabelsEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaLgAbstractEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaOntologyEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaRedirectsEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaShAbstractEtl;
import com.c24x7.semantics.dbpedia.etl.CDbpediaWordnetEtl;
import com.c24x7.util.CEnv;
import com.c24x7.util.CProfile;
import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Command line application that generates the terms matching map file from the 
			 * original dbpedia knowledge base..</p> 
			 * @author Patrick Nicolas
			 * @data 10/02/2011
			 */
public final class CDbpediaApp {

	protected static Map<String, ADbpediaEtl> operationsMap = null;
	
	static {
		operationsMap = new HashMap<String, ADbpediaEtl>();
		operationsMap.put("-loadLgAbstract", new CDbpediaLgAbstractEtl());
		operationsMap.put("-loadShAbstract", new CDbpediaShAbstractEtl());
		operationsMap.put("-loadRedirects", new CDbpediaRedirectsEtl());
		operationsMap.put("-loadCategories", new CDbpediaCategoriesEtl());
		operationsMap.put("-loadOntology", new CDbpediaOntologyEtl());
		operationsMap.put("-loadGeoLoc", new CDbpediaGeolocEtl());
		operationsMap.put("-loadImage", new CDbpediaImageEtl());
		operationsMap.put("-loadWordnet", new CDbpediaWordnetEtl());
		operationsMap.put("-loadWordnetList", new CDbpediaWordnetEtl(true));
		operationsMap.put("-loadMissingLabels", new CDbpediaLabelsEtl());
	}
	
	
	public static void main(String[] args) {
	//	CLogger.setLoggerInfo(CLogger.DBPEDIA_SERVICE_TRACE);
		CProfile.getInstance().time("Start Operation");
	
		if( args[0] == null || args[0].compareTo("-help") == 0) {
			printHelp();
		}
		else {
			if( operationsMap.containsKey(args[0])) {
				executeLoad(args);
			}
			else if( args[0].compareTo("-createTaxonomy")==0) {
				try {
					executeCreateTaxonomy(args);
				}
				catch( SemanticAnalysisException e) {
					CLogger.error(e.toString());
				}
				catch( InitException e) {
					CLogger.error(e.toString());
				}

			}
			else if( args[0].compareTo("-countRecords") == 0) {
				executeCount();
			}
 		}
		CProfile.getInstance().time("\nEnd Operation\n");
	}
	
	
	
							// ----------------------
							// Private Supporting Methods
							// ---------------------------
	
	
	private static void executeLoad(final String[] args) {
		try {
			CDbpediaLoader dbpediaManager = (args.length > 1 && args[1] != null) ?
	                new CDbpediaLoader(Long.parseLong(args[1])) :
	                new CDbpediaLoader();
	
			dbpediaManager.writeDbpediaDatabase(operationsMap.get(args[0]));
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
	}
	
	
	private static void executeCreateTaxonomy(final String[] args) throws SemanticAnalysisException, InitException  {
		if(args.length < 1) {
			CLogger.error("DBpedia Query is improperly formatted ");
			printHelp();
		}
		else {
				/*
				 * Initialize the different cached tables.
				 */
			if(CEnv.init() ) {
				
				/*
				 * Create a SQL access class for Dbpedia
				 */
				CDbpediaSql dbpediaSql = CDbpediaSql.getInstance();
				
					/*
					 * Initialize the statements for the query and
					 * updated requests to DBpedia database.
					 */
				String[] fields = new String[] {
					"label", "lgabstract", "taxonomy", "sub_taxonomy"
				};
				
				
				dbpediaSql.setQuery(fields, "wnet is null and id");
				
				String[] updateFields = new String[] {
					"taxonomy"	
				};
				dbpediaSql.setUpdate(updateFields, null);
				
				
				/*
				 * Create a semantic service for analyzing abstracts.
				 */
				CAbstractSemanticService semanticService = new CAbstractSemanticService(3);
				
				String[] results = null;
				
				int numEntries = dbpediaSql.getNumEntries();
				for( int id = 680000; id < numEntries; id++) {
					if( id % 2500 == 0) { 
						System.out.println(id);
					}
					
					try {
						results = dbpediaSql.executeQuery(id);
						if( results != null) {
							String[] categoriesLineages = null;
							if( results[results.length-1] != null) {
								categoriesLineages = results[results.length-1].split("#");
							}
							
								/*
								 * If the record has a taxonomy lineages associated to 
								 * categories., then used them to infer the actual taxonomy
								 * lineage of the Wikipedia entry or label.
								 */
							if( categoriesLineages != null) {
								try {
									semanticService.setTaxonomyLineages(results[2], categoriesLineages);
								}
								catch( ArrayIndexOutOfBoundsException e) {
									CLogger.error("out of bounds for " + results[0] + " " + e.toString());
								}
								
								if( semanticService.execute(results[1], results[0]) != null) {
									String[] taxonomyLineages = semanticService.getNewTaxonomyLineages();
									StringBuilder buf = new StringBuilder();
									
									int lastTaxonomyLineageIndex = taxonomyLineages.length-1;
									for(int j = 0; j < lastTaxonomyLineageIndex; j++) {
										buf.append(taxonomyLineages[j]);
										buf.append("#");
									}
									buf.append(taxonomyLineages[lastTaxonomyLineageIndex]);
									
									/*
									 * Insert the taxonomy lineages into Dbpedia table.
									 */
									dbpediaSql.executeUpdate(new String[] { buf.toString()}, id);
								}
							}
						}
					}
					catch( SQLException e) {
						CLogger.error(e.toString());
					}
				}
			}
		}
	}
	
	
	private static void executeCount() {
		CDatasetExtractor extractor = new CDatasetExtractor();
		long count = extractor.countRecords(new CDbpediaShAbstractEtl().getDbpediaFile(), null);
		CLogger.info("Count: " + count);
	}
	
	
	private static void printHelp() {
		CLogger.info("Command line arguments:");
		CLogger.info("\n-createTokensMap: generate token map file.");
		CLogger.info("\n-loadOntology: load ontology category from the dbpedia data sets");
		CLogger.info("\n-loadShAbstract: Load short abstract from dbpedia data sets");
		CLogger.info("\n-loadLgAbstract: Load long abstract from dbpedia data sets");
		CLogger.info("\n-loadPersonGroups: Populate the person groups affiliation from all dbpedia Yago extended data sets.");
		CLogger.info("\n-loadGeoLoc: Load the geo location and types from the dbpedia data sets.");
		CLogger.info("\n-loadImage: Load the thumbnail image from the dbpedia data sets.");
		CLogger.info("\n-loadImages: Load the different alternative images from Flickr data sets.");
		CLogger.info("\n-all: Load all the Dbpedia data sets.");
	}
}

// -------------------------  EOF ----------------------------------------------
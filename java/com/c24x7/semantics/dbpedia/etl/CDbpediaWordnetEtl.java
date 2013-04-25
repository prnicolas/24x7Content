// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.semantics.wordnet.CWordNet;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.collections.CDirectedGraph;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>Extraction of WordNet information (hypernyms)<br>
		 * This inner class that manages the creation of an entry in the 
		 * DBpedia database using WordNet ontology.</p>
		 * @author Patrick Nicolas
		 * @date 07/22/2011
		 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
		 */
public final class CDbpediaWordnetEtl extends ADbpediaEtl {
	protected static final String DBPEDIA_FILE 		= CEnv.datasetsDir + "dbpedia/long_abstracts_en.nt";
	protected static final String WORDNET_ONTOLOGY 	= CEnv.datasetsDir + "dbpedia/wordnet_ontology";

	protected static final int 	FIELD_LIMIT 		= 4095;
	protected static final int 	MIN_SEGMENT_INDEX	= 4;
	
	protected static final String ONTOLOGY_LISTS_FILE 	= CEnv.configDir + "ontology_lists";
	protected static final String ONTOLOGY_SEGMENTS4 	= CEnv.configDir + "ontology_seg4";
	protected static final String ONTOLOGY_SEGMENTS5 	= CEnv.configDir + "ontology_seg5";
	protected static final String ONTOLOGY_SEGMENTS6 	= CEnv.configDir + "ontology_seg6";
	protected static final String ONTOLOGY_SEGMENTS_2 	= CEnv.configDir + "ontology_seg_2";
	protected static final String ONTOLOGY_SEGMENTS_3 	= CEnv.configDir + "ontology_seg_3";
	protected static final String ONTOLOGY_SEGMENTS_4 	= CEnv.configDir + "ontology_seg_4";
	protected static final String ONTOLOGY_SEGMENTS_ANY_3 = CEnv.configDir + "ontology_3";
	protected static final String ONTOLOGY_SEGMENTS_ANY_4 = CEnv.configDir + "ontology_4";
	protected static final String ONTOLOGY_SEGMENTS_ANY_5 = CEnv.configDir + "ontology_5";

	private static final String ADD_ONTOLOGY_STATEMENT 	= "UPDATE 24x7c.dbpedia SET wordnet=? WHERE label=?;";
	private static final String CHECK_ENTRY_STATEMENT 	= "SELECT id, wordnet FROM 24x7c.dbpedia WHERE label=?;";
	private static final String ADD_ENTRY_STATEMENT 	= "INSERT INTO 24x7c.dbpedia (label, wordnet) VALUES(? ,?);";

	private List<String> 			_ontologiesList 	= null;
	private Map<String, String> 	_lemmaMap 			= null;
	private boolean 	 			_createListOnly 	= false;
	
	protected CSqlPreparedStmt _preparedStmtAddOntology = null;
	protected CSqlPreparedStmt _preparedStmtCheckEntry 	= null;
	protected CSqlPreparedStmt _preparedStmtAddEntry 	= null;
	
	public CDbpediaWordnetEtl() {
		this(false);
	}
	
	
	public CDbpediaWordnetEtl(boolean createListOnly) {
		_createListOnly = createListOnly;
		_extractor = new CDatasetExtractor();
	}
	
		/**
		 * <p>Retrieve the name of the file that contains the dump of dbpedia artifact.</p>
		 * @return name of the file containing this specific dbpedia artifact.
		 */
	public String getDbpediaFile() {
		return DBPEDIA_FILE;
	}
	
			/**
			 * <p>Execute the SQL statement associated with this field extracted from the dbpedia data sets.<br>
			 * First field to be extracted is the long abstract field.</p>
			 * @see com.c24x7.nlservices.finders.etl.CDbpediaEtl.NAEtl#executeEtl(java.lang.String, java.sql.Statement)
			 * @param newLine line extracted from the dbpedia data set
			 * @param stmt SQL statement used to update the table
			 */
	public boolean map(String newLine) {
		return (_createListOnly) ? writeList(newLine) : writeDatabase(newLine);
	}
	
	
	public boolean reduce() {
		boolean succeed = false;
		StringBuilder buf = new StringBuilder();
		
		if( _createListOnly ) {
			for( String ontology : _ontologiesList) {
				buf.append(ontology);
				buf.append("\n");
			}
			try {
				CFileUtil.write(WORDNET_ONTOLOGY, buf.toString());
				succeed = true;
			}
			catch (IOException e) {
				CLogger.error("Cannot create a file of WordNet ontologies list");
			}
		}
		
		else {
			if( _preparedStmtAddOntology == null) {
				_preparedStmtAddOntology = new CSqlPreparedStmt(ADD_ONTOLOGY_STATEMENT);
			}
			if( _preparedStmtCheckEntry != null) {
				_preparedStmtCheckEntry = new CSqlPreparedStmt(CHECK_ENTRY_STATEMENT);
			}
			if( _preparedStmtAddEntry != null) {
				_preparedStmtAddEntry = new CSqlPreparedStmt(ADD_ENTRY_STATEMENT);
			}
			
			succeed = updateDatabaseWithLemma();
			_preparedStmtAddOntology.close();
			_preparedStmtCheckEntry.close();
			_preparedStmtAddEntry.close();
		}
		
		return succeed;
	}
	
	
	
	public static int getWordnetOntologyLists() {
			
		final int DISPLAY_PROGRESS_INTERVAL = 10000;
		
		int numRecords = 0;
		long numEntries = CDbpediaSql.getInstance().getNumEntries();

		if( numEntries != -1) {
			Map<String[], Object> ontologyMap = new HashMap<String[], Object>();
			Map<String, Object> thirdClassMap = new HashMap<String, Object>();
			Map<String, Object> fourthClassMap = new HashMap<String, Object>();
			Map<String, Object> threeFromRootClassMap = new HashMap<String, Object>();
			Map<String, Object> any4FromRootClassMap = new HashMap<String, Object>();
			Map<String, Object> any5FromRootClassMap = new HashMap<String, Object>();
			
			CSqlPreparedStmt pStmt = new CSqlPreparedStmt("SELECT wordnet FROM 24x7c.dbpedia WHERE id=?;");

			ResultSet rs = null;
			String taxonomyStr = null;
			String[] taxonomies = null;
			String[] classes = null;
				
			for( long k = 1L; k < numEntries; k++) {
				try {
					taxonomyStr = null;
					pStmt.set(1, k);
					rs = pStmt.query();
					
					if( rs.next() ) {
						taxonomyStr = rs.getString("wordnet");
					}
					
					int classIndex = -1;
					
					if( taxonomyStr.length() > 2) {
						taxonomyStr = CStringUtil.decodeLatin1(taxonomyStr);
						if( taxonomyStr != null ) {
							taxonomies = taxonomyStr.split(CEnv.ENTRIES_DELIM );
							for( String taxonomy : taxonomies) {
								classes = taxonomy.split(CEnv.TAXONOMY_FIELD_DELIM);
								
								if( classes.length > 4) {
							
									thirdClassMap.put(classes[3], null);
									fourthClassMap.put(classes[4], null);
									threeFromRootClassMap.put(classes[classes.length-4], null);	
									
									classIndex = classes.length-4;
									if(classIndex < 4) {
										classIndex = 4;
									}
									any4FromRootClassMap.put(classes[classIndex], null);
									classIndex = classes.length-4;
									if(classIndex < 3) {
										classIndex = 3;
									}
									any5FromRootClassMap.put(classes[classIndex], null);
								}
							}
						}
					}
				}
				catch( SQLException e) {
					CLogger.error("Cannot analyzer Wordnet ontology " + e.toString());
				}
				if( k % DISPLAY_PROGRESS_INTERVAL == 0) {
					CLogger.info(String.valueOf(k) + " records");
				}
			}
			numRecords = ontologyMap.size();
			pStmt.close();
			writeOntologyElement(thirdClassMap, ONTOLOGY_SEGMENTS_ANY_3);
			writeOntologyElement(fourthClassMap, ONTOLOGY_SEGMENTS4);
			writeOntologyElement(threeFromRootClassMap, ONTOLOGY_SEGMENTS_3);
			writeOntologyElement(any4FromRootClassMap, ONTOLOGY_SEGMENTS_ANY_4);
			writeOntologyElement(any5FromRootClassMap, ONTOLOGY_SEGMENTS_ANY_5);
			
			CDirectedGraph graph = new CDirectedGraph("entity");
			for( String[] cls : ontologyMap.keySet()) {
				graph.addVertices(cls);
			}

			try  {
				CFileUtil.write(ONTOLOGY_LISTS_FILE, graph.toString());
			}
			catch (IOException e) {
				CLogger.error("Cannot store ontology lists " + e.toString());
			}
		}
		
		return numRecords;
	}
	
	

	
	
						// ---------------------------
						//  Private Supporting Methods
						// ---------------------------
	
	private boolean writeDatabase(String newLine) {
		boolean success = false;
		
				/*
				 * If the label was successfully extracted, then
				 */
		String label = _extractor.extractLabel(newLine);
			
		if( label != null ) {
			label = validate(label);
			
			if( label != null) {
				Map<String, Object> hypernymsMap  = CWordNet.getInstance().getTaxonomy(label);
				
				if( _lemmaMap == null ) {
					_lemmaMap = new HashMap<String, String>();
				}
					/*
					 * Extract the list of hypernyms if any are found..
					 */
				if( hypernymsMap.size() > 0) {
					List<String> lemmaList = new LinkedList<String>();
					
					StringBuilder ontologyBuf = new StringBuilder();
					
					for(String hypernym : hypernymsMap.keySet() ) {
						if(hypernymsMap.get(hypernym) == null) {
							if(hypernym.compareTo(label) != 0) {
								hypernym = validate(hypernym);
								if( hypernym != null) {
									lemmaList.add(hypernym);
								}
							}
						}
						else {
							ontologyBuf.append(hypernym);
							ontologyBuf.append(CEnv.ENTRIES_DELIM);
						}
					}
					
					String ontologyStr = ontologyBuf.substring(0,ontologyBuf.length()-2).toString();	
					for( String lemma : lemmaList ) {
						_lemmaMap.put(lemma, ontologyStr);
					}
					
					if( ontologyStr.length() > 8) {
						String encodedWordnetOntology = CStringUtil.encodeLatin1(ontologyStr);
						
						if( encodedWordnetOntology.length() >= FIELD_LIMIT) {
							encodedWordnetOntology = encodedWordnetOntology.substring(0, FIELD_LIMIT);
						}
						String encodedLabel = CStringUtil.encodeLatin1(label);
							
						try {
							_preparedStmtAddOntology.set(1, encodedWordnetOntology);
							_preparedStmtAddOntology.set(2, encodedLabel);
							_preparedStmtAddOntology.update();
						}
						catch( SQLException e) { 
							CLogger.error("Cannot update wordnet for " + encodedLabel + " " + e.toString());
						}
					}
				}
			}
		}

		return success;
	}
	
	
	private boolean updateDatabaseWithLemma() {
		boolean success = false;
		
		String ontology = null;
		for( String lemma : _lemmaMap.keySet() ) {
			ontology = _lemmaMap.get(lemma);
			
			try {
				String encodedLemma = CStringUtil.encodeLatin1(lemma);
				_preparedStmtCheckEntry.set(1, encodedLemma);
				ResultSet rs =  _preparedStmtCheckEntry.query();
				
				String wordnet = null;
				if( rs.next() ) {
					wordnet = rs.getString("wordnet");
					if( wordnet == null || wordnet.length() < 3) {
						_preparedStmtAddOntology.set(1, CStringUtil.encodeLatin1(ontology));
						_preparedStmtAddOntology.set(2, encodedLemma);
						_preparedStmtAddOntology.update();
						success = true;
					}
				}
				else {
					_preparedStmtAddEntry.set(1, encodedLemma);
					_preparedStmtAddEntry.set(2, CStringUtil.encodeLatin1(ontology));
					_preparedStmtAddEntry.insert();
					success = true;
				}
			}
			catch (SQLException e) {
				CLogger.error("Cannot update Database With Lemma " + lemma + " " + e.toString());
			}
		}
		
		return success;
	}
	
	
	
	private boolean writeList(String newLine) {
		boolean success = false;
				/*
				 * If the label was successfully extracted, then extracts
				 * the hypernyms..
				 */
		String label = _extractor.extractLabel(newLine);
		
		if( _ontologiesList == null) {
			_ontologiesList = new LinkedList<String>();
		}
			
		if( label != null ) {
			Map<String, Object> hypernymsMap = CWordNet.getInstance().getTaxonomy(label);
				
				/*
				 * Extract the list of hypernyms if any are found..
				 */
			if( hypernymsMap.size() > 0) {
				for(String hypernym : hypernymsMap.keySet() ) {
					if( hypernymsMap.get(hypernym) != null) {
						_ontologiesList.add(hypernym);
					}
				}
			}
		}
		else {
			CLogger.error(newLine + " cannot be decoded");
		}
		return success;
	}
	
	
	private static void writeOntologyElement(final Map<String, Object> map, final String fileName) {
		StringBuilder buf = new StringBuilder();
		for( String element : map.keySet()) {
			buf.append("\n");
			buf.append(element);
		}
		buf.append("\n##\n");
		buf.append(String.valueOf(map.size()));
		
		try  {
			CFileUtil.write(fileName, buf.toString());
		}
		catch (IOException e) {
			CLogger.error("Cannot store ontology lists " + e.toString());
		}
	}
	
	
}

// ----------------------------- EOF ------------------------------
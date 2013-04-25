// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Dbpedia extraction Step 4:<br>
		 * Class to extract ontology information (type and subtype) from dbpedia.org data set and 
		 * add to the dbpedia table content</p>
		 * @author Patrick Nicolas
		 * @date 10/15/2011
		 * @see ADbpediaEtl
		 */
public final class CDbpediaOntologyEtl extends ADbpediaEtl {
	public static String DBPEDIA_FILE = CEnv.datasetsDir + "/dbpedia/instance_types_en.nt";
	public static String DBPEDIA_ONTOLOGY_MARKER = "<http://dbpedia.org/ontology/";
	public static String  DBPEDIA_ONTOLOGIES_LIST = CEnv.configDir + "dbpedia_ontologies_list";
	public static final String FIELD_DELIM = "#";	
	
	protected String 		  _prevLabel = " ";
	protected StringBuilder _ontoBuffer = null;
	protected Map<String, Object> _dbpediaOntologyMap = null;
	
	public CDbpediaOntologyEtl() {
		super();
		_dbpediaOntologyMap  = new HashMap<String, Object>();
		_extractor = new CDatasetExtractor(new CDatasetExtractor.NHyphenResConverter(), 
										   new CDatasetExtractor.AResConverter());
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
			 * @see com.c24x7.nlservices.finders.etl.CTaxonomyExtractorEtl.NAEtl#executeEtl(java.lang.String, java.sql.Statement)
			 * @param newLine line extracted from the dbpedia data set
			 * @param stmt SQL statement used to update the table
			 */
	public boolean map(String newLine) {
		
		boolean success = false;
		String label = _extractor.extractLabel(newLine);
		
		if( label != null ) {
			String content = extractOntology(newLine, DBPEDIA_ONTOLOGY_MARKER);			
			
			if( content != null ) {
					/*
					 * If this is not a new content then add to the Ontology column.
					 */
				if( _prevLabel.compareTo(label) == 0) {
					_ontoBuffer.append(FIELD_DELIM);
					_ontoBuffer.append(content);
				}
				
					/*
					 * If this is a new label entry, then process the previous set of
					 * ontologies categories..
					 */
				else {
				
					if( _ontoBuffer != null ) {
						List<String> categories = CStringUtil.split(_ontoBuffer.toString(), FIELD_DELIM);
						
						if(categories != null) {
							String subtype = null;
							if( categories.size() > 0) {
								int lastOntology = categories.size()-1;
								StringBuilder typeBuf = new StringBuilder();
								for( int j = lastOntology; j >= 0; j--) {
									subtype = categories.get(j);
									subtype = CDatasetExtractor.AResConverter.convertCompound(subtype);
									typeBuf.append(subtype);
									if( j > 0) {
										typeBuf.append("/");
									}
								}
								
								if( typeBuf.length() > 2) {
									_dbpediaOntologyMap.put(typeBuf.toString(), null);
								}
								/*
								StringBuilder sqlUpdate = new StringBuilder("UPDATE 24x7c.dbpedia SET ontology=\'");
								sqlUpdate.append(CStringUtil.encodeLatin1(typeBuf.toString()));
								sqlUpdate.append("\' WHERE label=\'");
								sqlUpdate.append(CStringUtil.encodeLatin1(_prevLabel));
								sqlUpdate.append("\';");
								try {
									success = stmt.execute(sqlUpdate.toString());
								}
								catch( SQLException e) { 
									CLogger.error(e.toString());
								}
								*/
							}
						}
					}
					_prevLabel = label;
					_ontoBuffer = new StringBuilder(content);
				}
			}
		}
		return success;
	}
	
	public boolean reduce() {
		boolean succeed = false;
		StringBuilder buf = new StringBuilder();
	
		for( String ontology : _dbpediaOntologyMap.keySet()) {
			buf.append(ontology);
			buf.append("\n");
		}
		buf.append("##\n");
		buf.append(String.valueOf(_dbpediaOntologyMap.size()));
		
		try  {
			CFileUtil.write(DBPEDIA_ONTOLOGIES_LIST, buf.toString());
			succeed = true;
		}
		catch (IOException e) {
			CLogger.error("Cannot store ontology lists " + e.toString());
		}
		
		return succeed;
	}
	

	protected final String extractOntology(final String newLine, String ontologyMarker) {
		String content = null;
		int indexStartOntoStr = newLine.indexOf(ontologyMarker);
		if( indexStartOntoStr != -1) {
			String ontoStr = newLine.substring(indexStartOntoStr + ontologyMarker.length());
			
			int indexEndOntoStr = ontoStr.indexOf(">");
			if( indexEndOntoStr != -1) {
				content = ontoStr.substring(0, indexEndOntoStr);
			}
		}
		
		return content;
	}

}


// -------------------------- EOF -----------------------------

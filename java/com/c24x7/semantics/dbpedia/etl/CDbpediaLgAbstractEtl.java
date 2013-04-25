// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.c24x7.semantics.dbpedia.etl.CDatasetExtractor;
import com.c24x7.util.CEnv;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>Extraction of long abstract from dbpedia Datasets.<br>
		 * This inner class that manages the creation of an entry in the 
		 * DBpedia database and the initialization of long abstract from  the Wikipedia data set.</p>
		 * @author Patrick Nicolas
		 * @date 07/22/2011
		 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
		 */
public class CDbpediaLgAbstractEtl extends ADbpediaEtl {
	protected static final int MAX_FIELD_SIZE = 13996;
	protected static final String DBPEDIA_FILE = CEnv.datasetsDir + "dbpedia/long_abstracts_en.nt";
	private static final String SELECT_DBPEDIA_LABEL = "SELECT id FROM 24x7c.dbpedia WHERE label=?;";
	private static final String ADD_DBPEDIA_LABEL = "INSERT INTO 24x7c.dbpedia (label, lgabstract) VALUES(?,?);";

	
	private CSqlPreparedStmt _preparedStmtEntry = null;
	private CSqlPreparedStmt _preparedStmtAdd = null;
	
	
	public CDbpediaLgAbstractEtl() {
		super();
		_extractor = new CDatasetExtractor();
		_preparedStmtEntry = new CSqlPreparedStmt(SELECT_DBPEDIA_LABEL);
		_preparedStmtAdd = new CSqlPreparedStmt(ADD_DBPEDIA_LABEL);
	}
	
	   /**
		* <p>Retrieve the name of the file that contains dbpedia artifact to be updated</p>
		* @return name of the file containing this dump of long or extended abstract.
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
		 * @throws SQLException if the database table cannot be properly updated..
		 */
	public boolean map(String newLine) {
		
		boolean success = false;
		String label = _extractor.extractLabel(newLine);
				/*
				 * If the label was successfully extracted, then
				 */
		if( label != null) {
			label = validate(label);
			
			if( label != null) {
				label = CStringUtil.encodeLatin1(label);
				try {
					_preparedStmtEntry.set(1, label);
					ResultSet rs = _preparedStmtEntry.query();
					
						/*
						 * If no entry already exists for this label...
						 */
					if(!rs.next()) { 
						
								/*
								 * extract the value or payload for the abstract, if the operation
								 * consists of updating the database..
								 */
						String lgAbstract = extractAbstract(newLine);
								/*
								 * Insert label and abstract into the table..
								 */
						if( lgAbstract != null ) {
					
								
							lgAbstract =  CStringUtil.encodeLatin1(lgAbstract);
							if( lgAbstract.length() > MAX_FIELD_SIZE) {
								lgAbstract = lgAbstract.substring(0, MAX_FIELD_SIZE);
							}
							
							_preparedStmtAdd.set(1,label);
							_preparedStmtAdd.set(2, CStringUtil.encodeLatin1(lgAbstract));
							_preparedStmtAdd.insert();	
						}
					}
				}
				catch(SQLException e) {
					CLogger.error("Cannot update long abstract with " + label + " " + e.toString());
					success = false;
				}
			}
		}
		return success;
	}
	
	
	
	public boolean reduce() {		
		_preparedStmtEntry.close();
		_preparedStmtAdd.close();
		return true;
	}
	
	
						// ------------------------
						// Private Supporting Methods
						// ---------------------------
	
	protected String extractAbstract(final String newLine) {
		String abstractStr = null;
		
		int indexStartQuotes = newLine.indexOf("\"");
		if( indexStartQuotes != -1) {
			int indexEndQuotes = newLine.indexOf("\"@", indexStartQuotes+1);
			if( indexEndQuotes != -1 ) {
				abstractStr = newLine.substring(indexStartQuotes+1, indexEndQuotes);
			}
		}

		return abstractStr;
	}

}

// ---------------------- EOF --------------------------------

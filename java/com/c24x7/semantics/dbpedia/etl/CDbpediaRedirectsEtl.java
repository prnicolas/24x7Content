// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>Extraction of redirects labels (or alias) from dbpedia dataset<br>
		 * This class create the redirect or aliases table for the dbpedia data sets..</p>
		 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
		 * @author Patrick Nicolas
		 * @date 10/31/2011
		 */
public final class CDbpediaRedirectsEtl extends ADbpediaEtl {
	protected static final String DBPEDIA_FILE 		= CEnv.datasetsDir + "/dbpedia/redirects_en.nt";
	protected static final String ERROR_FILE 			= CEnv.outputDir + "debug/error_redirects";
	protected static final String GET_ENTRY_RECORD 	= "SELECT id FROM 24x7c.dbpedia WHERE label=?";
	protected static final String CREATE_ALIAS_RECORD = "INSERT INTO 24x7c.dbpedia_aliases (label, resourceid, contexts) VALUES (?,?,?);";
	protected static final int 	FIELD_LABEL_LIMIT 	= 383;
	
	
	protected Map<String, String> _errors = null;
	protected CSqlPreparedStmt _preparedStmtAlias = null;
	protected CSqlPreparedStmt _preparedStmtEntry = null;

	public CDbpediaRedirectsEtl() {
		super();			
		_preparedStmtAlias = new CSqlPreparedStmt(CREATE_ALIAS_RECORD);
		_preparedStmtEntry = new CSqlPreparedStmt(GET_ENTRY_RECORD);
		_extractor = new CDatasetExtractor( new CDatasetExtractor.NHybridResConverter(), new CDatasetExtractor.NHybridResConverter());
		_errors = new HashMap<String, String>();

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
		 */
	public boolean map(String newLine) {
		boolean success = false;
				/*
				 * If the label was successfully extracted, then
				 */
		String entry = _extractor.extractResource(newLine, true);
		String aliasEntry = _extractor.extractLabel(newLine);
		
		if(aliasEntry != null && entry != null) {
			String decodedAlias = CStringUtil.decodeLatin1(aliasEntry);
			String decodedEntry  = CStringUtil.decodeLatin1(entry);
			
			if( decodedAlias != null && decodedAlias.length() > 2) {
				
				int indexOfParenthesis = decodedAlias.indexOf("(");
				if( indexOfParenthesis > 1) {
					decodedAlias = decodedAlias.substring(0, indexOfParenthesis-1);
				}
	
					/*
					 * We exclude the label aliases with have less that 3
					 * characters and start with 'The'.
					 */
				String alias = null;
				if( decodedAlias.length() > 2 && !decodedAlias.startsWith("The")) {				
					String context = null;
					int indexSeparator = decodedAlias.indexOf("/");
					if( indexSeparator != -1) {
						alias = decodedAlias.substring(0, indexSeparator).trim();
						context = decodedAlias.substring(indexSeparator+1).trim();
					}
					else {
						alias = decodedAlias;
					}
						
					if(context == null) {
						context = decodedEntry;
					}
					createAliasEntry(alias, decodedEntry, context);
				}
			}
			else {
				_errors.put(aliasEntry, entry);
			}
		}
		
		return success;
	}
		

	public boolean reduce() {

		if( _errors.size() > 0) {
			StringBuilder buf = new StringBuilder("alias:label\n");
			for( String key : _errors.keySet()) {
				buf.append("\n");
				buf.append(key);
				buf.append(CEnv.KEY_VALUE_DELIM);
				buf.append(_errors.get(key));
			}
			try {
				CFileUtil.write(ERROR_FILE, buf.toString());
			} 
			catch(IOException e) {
				CLogger.error(e.toString());
			}
		}
		
		return true;
	}
	
	
	protected void createAliasEntry(String decodedAlias, String decodedEntry, String context) {
			/*
			 * Make sure we found the label and it is not a duplicate
			 */
		if( decodedEntry != null && decodedEntry.compareTo(decodedAlias) != 0) {
	
			try {
				String encodedEntry = CStringUtil.encodeLatin1(decodedEntry);
				
				_preparedStmtEntry.set(1, encodedEntry);
				ResultSet rs = _preparedStmtEntry.query();
				int labelId = -1;
				
				if( rs.next() ) {
					labelId = rs.getInt("id");
				}
				if( labelId != -1L) {
					String encodedAlias = CStringUtil.encodeLatin1(decodedAlias);
					if( encodedAlias.length() < FIELD_LABEL_LIMIT) { 
						_preparedStmtAlias.set(1, encodedAlias);
						_preparedStmtAlias.set(2, labelId);
						_preparedStmtAlias.set(3, context);
						
						_preparedStmtAlias.insert();
					}
				}
				else {
					CLogger.info(decodedAlias + " could not be entered");
					_errors.put(decodedAlias, decodedEntry);
				}
			}
			catch( SQLException e) {
				_errors.put(decodedAlias, decodedEntry);
				CLogger.error("cannot add " + decodedAlias + " as redirect " + e.toString());
			}
		}
	}
}

// ----------------------  EOF -----------------------------------

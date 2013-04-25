// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Utility class to execute requests against the Wikipedia Reference database 
		 * using either the row id or the indexed label.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/10/2012
		 */
public final class CDbpediaSql {	
	private static final int DEFAULT_DISPLAY_INTERVAL 	= 250;
	
	private static CDbpediaSql 		instance = null;
	private static Map<String, Object> 	databaseFields = null;
	
	private String[] _fields 			= null;
	private int		 _displayInterval 	= DEFAULT_DISPLAY_INTERVAL;
	private	int 	 _numEntries 		= -1;
	private int 	 _numAliases		= -1;
	
	private CSqlPreparedStmt _pStmt			= null;
	private CSqlPreparedStmt _pUpdateStmt	= null;
	
		/**
		 * <p>Retrieve the singleton for querying dbpedia tables.</p>
		 * @return single instance of the CDbpediaSql class.
		 */
	public static CDbpediaSql getInstance() {
		if( instance == null) {
			databaseFields = new HashMap<String, Object>();
			databaseFields.put("label", null);
			databaseFields.put("lgabstract", null);
			databaseFields.put("categories", null);
			databaseFields.put("taxonomy", null);
			databaseFields.put("sub_taxonomy", null);
			databaseFields.put("idf", null);
			databaseFields.put("id", null);
			
			instance = new CDbpediaSql();
		}
		
		return instance;
	}
	
			/**
			 * <p>Extract the number entries in the Wikipedia database.</p>
			 * @return the number of entries in the Wikipedia database
			 */
	public final int getNumEntries(){
		if(_numEntries == -1) {
			try {
				_numEntries = getTableLastIndex("24x7c.dbpedia");
			}
			catch( SQLException e) {
				CLogger.error("Cannot retrieve the number of Dbpedia entries");
			}
		}
		return _numEntries;
	}
		
			/**
			 * <p>Extract the number entries in the Wikipedia aliases database.</p>
			 * @return the number of entries in the Wikipedia aliases database
			 */
	public final int getNumAliases() {
		if(_numAliases == -1) {
			try {
				_numAliases = getTableLastIndex("24x7c.dbpedia_aliases");
			}
			catch( SQLException e) {
				CLogger.error("Cannot retrieve the number of Dbpedia aliases");
			}
		}
		return _numAliases;
	}
		

			/**
			 * <p>Close all database connections used in manipulating data in
			 * the Wikipedia reference tables.
			 */
	public void close() {
		if( _pUpdateStmt != null) {
			_pUpdateStmt.close();
			_pUpdateStmt = null;
		}
		if( _pStmt != null) {
			_pStmt.close();
			_pStmt = null;
		}
	}


	
	/**
	 * <p>Set the parameters of the prepared (compiled) update statement..</p>
	 * @param array of column names used in the update request
	 * @throws IllegalArgumentException if the database fields are undefined or incorrect
	 */
	public void setUpdate(final String[] fields) {
		setUpdate(fields, null);
	}

		/**
		 * <p>Set the parameters of the prepared (compiled) update statement..</p>
		 * @param array of column names used in the update request
		 * @param condition condition to be added on the update request (WHERE condition)
		 * @throws IllegalArgumentException if the database fields are undefined or incorrect
		 */
	public void setUpdate(final String[] fields, final String condition) {
		if( fields == null) {
			throw new IllegalArgumentException("Wikipedia database fields undefined");
		}

		if( _pUpdateStmt != null) {
			_pUpdateStmt.close();
			_pUpdateStmt = null;
		}
		
		StringBuilder buf = new StringBuilder("UPDATE 24x7c.dbpedia set ");
		
		int lastFieldsIndex = fields.length-1;
		for(int k = 0; k < lastFieldsIndex; k++)  {
			buf.append(fields[k]);
			buf.append(" =?, ");
		}
		buf.append(fields[lastFieldsIndex]);
		buf.append(" =?");
		buf.append(" WHERE ");
		if(condition != null) {
			buf.append(condition);
			buf.append("=?;");
		}
		else {
			buf.append("id=?;");
		}
		
		_pUpdateStmt = new CSqlPreparedStmt(buf.toString());
	}
	
	
	/**
	 * <p>Set the parameters of the prepared (compiled) query statement..</p>
	 * @param array of column names used in the query 
	 * @throws IllegalArgumentException if the database fields are undefined or incorrect
	 */
	public void setQuery(final String[] fields) {
		setQuery(fields, null);
	}
	
	
			/**
			 * <p>Set the parameters of the prepared (compiled) query statement..</p>
			 * @param array of column names used in the query 
			 * @param condition condition to be added on the query (WHERE condition)
			 * @throws IllegalArgumentException if the database fields are undefined or incorrect
			 */
	public void setQuery(final String[] fields, final String condition) {
		if( fields == null) {
			throw new IllegalArgumentException("Wikipedia database fields undefined");
		}
			
		for( String field : fields) {
			if(!databaseFields.containsKey(field)) {
				throw new IllegalArgumentException("Wikipedia database fields undefined");
			}
		}
	
		if( _pStmt != null) {
			_pStmt.close();
			_pStmt = null;
		}
		
		_fields = fields;
		StringBuilder pStmtBuf = new StringBuilder("SELECT ");
		
		int lastFieldIndex = fields.length -1;
		for( int k = 0; k < lastFieldIndex; k++) {
			pStmtBuf.append(fields[k]);
			pStmtBuf.append(CEnv.FIELD_DELIM);
		}
		pStmtBuf.append(fields[lastFieldIndex]);		
		pStmtBuf.append(" FROM 24x7c.dbpedia WHERE ");
		
		if(condition != null) {
			pStmtBuf.append(condition);
			pStmtBuf.append("=?;");
		}
		else {
			pStmtBuf.append("id=?;");
		}
		
		_pStmt = new CSqlPreparedStmt(pStmtBuf.toString());
	}
	
	
	

	
		/**
		 * <p>Set up the interval as the number of database rows between 
		 * execution progress display.</p>
		 * @param interval number of database rows between each progress display
		 */
	public void setDisplayInterval(int interval) {
		_displayInterval = interval;
	}
	
	

	
	/**
	 * <p>Execute an update on a dbpedia record with a specific id.
	 * @param fields values of the fields to be updated in the row id.
	 * @param id row id in the dbpedia table
	 * @return array of columns values extracted for the table
	 * @throws SQLException if the database is unavailable of the request is incorrectly formatted
	 */
	public void executeUpdate(String[] fields, int id) throws SQLException {
		for(int k = 0; k < fields.length; k++) {
			fields[k] = CStringUtil.encodeLatin1(fields[k]);
			_pUpdateStmt.set(k+1, fields[k]);
		}
		
		_pUpdateStmt.set(fields.length+1, id);
		_pUpdateStmt.update();
	}
	
	
	
			/**
			 * <p>Execute the query for the dbpedia record with a specific id.
			 * @param id row id in the dbpedia table
			 * @return array of columns values extracted for the table
			 * @throws SQLException if the database is unavailable of the query is incorrectly formatted
			 */
	public String[] executeQuery(int id) throws SQLException {
		String[] record = null;

		_pStmt.set(1, id);		
		ResultSet rs = _pStmt.query();
	
		if( rs.next() ) {
			record = new String[_fields.length];
	
			for( int k = 0; k < _fields.length; k++) {
				record[k] = rs.getString(_fields[k]);
				if( record[k] != null) {
					record[k] = CStringUtil.decodeLatin1(record[k]);
				}
			}
		}
		
		return record;
	}

	
	
			/**
			 * <p>Create a list of fields extracted from Wikipedia reference
			 * database by selecting the index of the first entry and the 
			 * number of database rows to query.</p>
			 * @param startIndex index of the first entry in the Wikipedia database
			 * @param numRows	number of rows or entries in the Wikipedia database
			 * @return list of fields extracted from the database.
			 * @throws SQLException if Wikipedia reference database is unavailable or the query failes.
			 */
	public List<String[]> execute(int startIndex, int endIndex) throws SQLException {
		if(endIndex < startIndex) {
			throw new IllegalArgumentException("Incorrect Dbpedia row indices start: " + startIndex + " and end " + endIndex);
		}

		
		List<String[]> recordsList = new ArrayList<String[]>();
		String[] record = null;
			/*
			 * walks through the database to extract labeled N-GRAM, content (or abstract) and
			 * the pre-existence of a taxonomy.
			 */
		for( int id = startIndex; id < endIndex; id++) {
			_pStmt.set(1, id);		
			ResultSet rs = _pStmt.query();
			record = new String[_fields.length];
	
			if( rs.next() ) {
				for( int k = 0; k < _fields.length; k++) {
					record[k] = rs.getString(_fields[k]);
					if( record[k] != null) {
						record[k] = CStringUtil.decodeLatin1(record[k]);
					}
				}
				recordsList.add(record);
			}
			
			if( id - startIndex % _displayInterval++ == 0) {
				CLogger.info(_displayInterval + " database queries");
			}
		}
			
		_pStmt.close();
		return recordsList;
	}
	
	
		/**
		 * <p>Create a list of fields extracted from Wikipedia reference
		 * database by selecting the index of the first entry and the 
		 * number of database rows to query.</p>
		 * @return list of fields extracted from the database.
		 * @throws SQLException if Wikipedia reference database is unavailable or the query failes.
		 */
	public List<String[]> execute() throws SQLException {
		int numEntries = getNumEntries();
		
		return execute(1, numEntries);
	}

					
	
							// ------------------
							// Private Methods
							// -----------------
	
	/**
	 * <p>Create a Dbpedia query instance for the default fields 'label', 'abstract', 'wordnet' </p>
	 * @param database columns to query
	 * @throws IllegalArgumentExceptin if the database fields are undefined or incorrect
	 */
	private CDbpediaSql() {	}

				
	
	private static int getTableLastIndex(String tableName) throws SQLException {
		
		int max_id = -1;
		StringBuilder buf = new StringBuilder("SELECT max(id) FROM ");
		buf.append(tableName);
		buf.append(";");
		
		CSqlPreparedStmt pstmt = new CSqlPreparedStmt(buf.toString());

		ResultSet rs = pstmt.query();
		if( rs.next() ) {
			max_id = rs.getInt("max(id)");
		}
		pstmt.close();
		
		return max_id;
	}
}

// ---------------------------  EOF -------------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.util.CEnv;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.string.CStringUtil;


			/**
			 * <p>Connection pool for extracting taxonomy lineages from Wikipedia Reference Database..</p>
			 * @author Patrick Nicolas
			 * @date 05/14/2012
			 */
public final class CTaxonomyConnectionsPool {
	public final static int MAX_NUM_CONNECTIONS = 64;
	public final static int ENTRY_TABLE = 0;
	public final static int ALIAS_TABLE = 1;

	private static CTaxonomyConnectionsPool connectionsPool = null;

			/**
			 * <p>Retrieve the singleton of taxonomy connection pool. The connection
			 * pool is shared between multiple clients of threads.</p>
			 * @return reference to the singleton
			 */
	public static CTaxonomyConnectionsPool getInstance() {
		if( connectionsPool == null) {
			connectionsPool = new CTaxonomyConnectionsPool();
		}
		return connectionsPool;
	}
	
	
	
				/**
				 * <p>Generic class to extract taxonomy lineages from
				 * Wikipedia reference database.</p>
				 * @author Patrick Nicolas         24x7c 
				 * @date June 4, 2012 2:03:27 PM
				 */
	public static abstract class NTaxonomiesConn {
		protected CSqlPreparedStmt[] 	_sqlPreparedStmts 	= null;
		
		
			/**
			 * <p>Create a connection object for retrieving taxonomy
			 * information from the Wikipedia reference database.
			 * @param entryStmt SQL prepared statement for the accessing the entry table
			 * @param aliasStmt SQL prepared statement for the accessing the aliases table
			 */
		public NTaxonomiesConn(final String entryStmt, final String aliasStmt) {
			_sqlPreparedStmts = new CSqlPreparedStmt[2];
			_sqlPreparedStmts[ENTRY_TABLE] = new CSqlPreparedStmt(entryStmt);
			_sqlPreparedStmts[ALIAS_TABLE]  = new CSqlPreparedStmt(aliasStmt);

		}
		
		
			/**
			 * <p>Retrieve the JDBC prepared statement associated with
			 * this connection pool</p>
			 * @param tableIndex index or identifier that specifies the DBpedia table used to retrieve the taxonomy lineages
			 * @throw IllegalArgumentException if the identifier of the table is incorrect
			 * @return prepared statement object for a query associated to a specific table.
			 */
		public final CSqlPreparedStmt getPreparedStmt(int tableIndex) {
			if( tableIndex != ENTRY_TABLE && tableIndex != ALIAS_TABLE) {
				throw new IllegalArgumentException("Incorrect index for taxonomy connections pool");
			}
			return _sqlPreparedStmts[tableIndex];
		}
		

			/**
			 * <p>Close all JDBC connections and statements used in retrieving
			 * taxonomy lineages from Wikipedia data base.</p>
			 */
		public void close() {
			for( int k = 0; k <= ALIAS_TABLE; k++) {
				if( _sqlPreparedStmts[k] != null ) {
					_sqlPreparedStmts[k].close();
				}
			}
		}
		
		/**
		 * <p>Retrieve an array of taxonomy lineages for a defined entry.</p>
		 * @param rs Result set from the query to dbpedia.
		 */
		public abstract String[] retrieve(final ResultSet rs) throws SQLException;	
	}
	
	
	/**
	 * <p>Nested class that extracts the taxonomy of a Wikipedia
	 * entry (or label).</p>
	 * @author Patrick Nicolas         24x7c 
	 * @date June 4, 2012 1:56:52 PM
	 */
	public static class NLabelsTaxonomiesConn extends NTaxonomiesConn {
		private final static String[] SELECT_TAXONOMY_SOURCES = 
			new String[] { 
				"SELECT taxonomy FROM 24x7c.dbpedia WHERE label=?;",
				"SELECT db.taxonomy FROM 24x7c.dbpedia db JOIN  24x7c.dbpedia_aliases da ON db.id = da.resourceid WHERE da.label=?;"
			};

				
			/**
			 * <p>Create an instance of the class that extract the
			 * taxonomy lineages from Wikipedia labels.</p>
			 */
		public NLabelsTaxonomiesConn() {
			super(SELECT_TAXONOMY_SOURCES[ENTRY_TABLE], SELECT_TAXONOMY_SOURCES[ALIAS_TABLE]);
		}
		
			
		/**
		 * <p>Retrieve an array of taxonomy lineages for a defined entry</p>
		 * @param rs Result set from the query to dbpedia.
		 * @return a set of taxonomy lineages associated a N-Gram
		 */
		@Override
		public String[] retrieve(final ResultSet rs) throws SQLException {
			String taxonomyRecord = rs.getString("taxonomy");
			String decodedTaxonomyRecord = null;
			String[] taxonomiesLineagesArray = null;
			
			if( taxonomyRecord != null && taxonomyRecord.length() > 2) {
				decodedTaxonomyRecord = CStringUtil.decodeLatin1(taxonomyRecord);
	
				if( decodedTaxonomyRecord != null) {
					taxonomiesLineagesArray = decodedTaxonomyRecord.split(CEnv.ENTRY_FIELDS_DELIM);
				}
			}
			
			return taxonomiesLineagesArray;
		}

	}
	
		/**
		 * <p>Nested class that extracts the taxonomy of a Wikipedia
		 * entry (or label) as well as the taxonomy lineages associated
		 * with the categories of this entry.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date June 4, 2012 1:56:52 PM
		 */
	public static class NLabelsAndCatTaxonomiesConn extends NTaxonomiesConn {
		private final static String[] SELECT_TAXONOMY_SOURCES = 
			new String[] { 
				"SELECT taxonomy, sub_taxonomy FROM 24x7c.dbpedia WHERE label=?;",
				"SELECT db.taxonomy, db.sub_taxonomy FROM 24x7c.dbpedia db JOIN  24x7c.dbpedia_aliases da ON db.id = da.resourceid WHERE da.label=?;"
			};

				
		
		/**
		 * <p>Create an instance of the class that extract the
		 * taxonomy lineages from Wikipedia labels as well as the
		 * taxonomy lineages of the categories associated to this label</p>
		 */
		public NLabelsAndCatTaxonomiesConn() {
			super(SELECT_TAXONOMY_SOURCES[ENTRY_TABLE], SELECT_TAXONOMY_SOURCES[ALIAS_TABLE]);
		}

		
			/**
			 * <p>Retrieve an array of taxonomy lineages for a defined entry
			 * as well as the lineages extracted from the categories associated
			 * with this entry.</p>
			 * @param rs Result set from the query to dbpedia.
			 */
		@Override
		public String[] retrieve(final ResultSet rs) throws SQLException {
			String[] TaxonomyLineagesSet = null;
			
			String taxonomyRecord = rs.getString("taxonomy");
			String subTaxonomyRecord = rs.getString("sub_taxonomy");
			String decodedTaxonomyRecord = null;
			String[] labelTaxonomyLineages = null;
			
			/*
			 * Extract the taxonomy lineages associated
			 * with this label.
			 */
			if( taxonomyRecord != null && taxonomyRecord.length() > 2) {
				decodedTaxonomyRecord = CStringUtil.decodeLatin1(taxonomyRecord);
				if( decodedTaxonomyRecord != null) {
					labelTaxonomyLineages = decodedTaxonomyRecord.split(CEnv.ENTRY_FIELDS_DELIM);
				}
			}
			
			/*
			 * Extracts the taxonomy lineages associated with the
			 * categories of this label.
			 */
			if( subTaxonomyRecord != null && subTaxonomyRecord.length() > 2) {
				decodedTaxonomyRecord =  CStringUtil.decodeLatin1(subTaxonomyRecord);
				
				if( decodedTaxonomyRecord != null) {
					String[] catTaxonomiesLineages = decodedTaxonomyRecord.split(CEnv.ENTRY_FIELDS_DELIM);
					int labelTaxonomyLineagesLen = (labelTaxonomyLineages != null) ? labelTaxonomyLineages.length : 0;
					int catTaxonomiesLineagesLen = (catTaxonomiesLineages != null) ? catTaxonomiesLineages.length : 0;
					int taxonomiesLineagesLen = labelTaxonomyLineagesLen + catTaxonomiesLineagesLen;
					
					if( taxonomiesLineagesLen > 0) {
						Map<String, Object> taxonomiesLineagesMap = new HashMap<String, Object>();
						
						
						int k = 0;
						if(labelTaxonomyLineages != null) {
							for( ; k < labelTaxonomyLineagesLen; k++) {
								taxonomiesLineagesMap.put(labelTaxonomyLineages[k], null);
							}
						}
						if( catTaxonomiesLineagesLen > 0) {
							for( int j = 0; k < taxonomiesLineagesLen; k++, j++) {
								taxonomiesLineagesMap.put(catTaxonomiesLineages[j], null);
							}
						}
						TaxonomyLineagesSet = taxonomiesLineagesMap.keySet().toArray(new String[0]);
					} 
				}
			}
			
			return TaxonomyLineagesSet;
		}
	}
	
	
	
	private List<NTaxonomiesConn> _connectionsPool = null;

	
	public void close() {
		if( _connectionsPool.size() > 0) {
			for( NTaxonomiesConn taxonomyConnection : _connectionsPool) {
				taxonomyConnection.close();
			}
		}
	}

	
		/**
		 * <p>Retrieve the connection and statement that retrieve
		 * the taxonomy information for a Wikipedia entry.</p>
		 * @return new connection to the Wikipedia reference database
		 */
	public NTaxonomiesConn getLabelsConnection() throws SemanticAnalysisException {
		if(_connectionsPool.size() > MAX_NUM_CONNECTIONS) {
			throw new SemanticAnalysisException("Connections pool exceeds " + String.valueOf(MAX_NUM_CONNECTIONS));
		}
		
		NTaxonomiesConn tConnection = new NLabelsTaxonomiesConn();
		_connectionsPool.add(tConnection);
		
		return tConnection;
	}
	
		/**
		 * <p>Retrieve the connection and statement that retrieve
		 * the taxonomy information for a Wikipedia entry and
		 * its categories</p>
		 * @return new connection to the Wikipedia reference database
		 */
	public NTaxonomiesConn getLabelsAndCatsConnection() throws SemanticAnalysisException {
		if(_connectionsPool.size() > MAX_NUM_CONNECTIONS) {
			throw new SemanticAnalysisException("Connections pool exceeds " + String.valueOf(MAX_NUM_CONNECTIONS));
		}

		NTaxonomiesConn tConnection = new NLabelsAndCatTaxonomiesConn();
		_connectionsPool.add(tConnection);
		
		return tConnection;
	}
	
	
	
	
						// -------------------------
						//  Private Methods
						// ------------------
	
	private CTaxonomyConnectionsPool() {
		_connectionsPool = new LinkedList<NTaxonomiesConn>();
	}

}

// ------------------------------  eof -----------------------------------

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.db;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.util.db.CSqlPreparedStmt;


			/**
			 * <p>Class that implements a simple connection pools management.</p>
			 * @author Patrick Nicolas
			 * @date 05/14/2012
			 */
public final class CDbConnectionsPool {

	public enum EDATA_TYPE {
		TAXONOMY,
	};
		
	private static CDbConnectionsPool connectionsPool = null;
	
		/**
		 * <p>Retrieve the database connections pool singleton.</p>
		 * @return database connections pool singleton
		 */
	public static CDbConnectionsPool getInstance() {
		if( connectionsPool == null) {
			connectionsPool = new CDbConnectionsPool();
		}
		return connectionsPool;
	}
	

		/**
		 * <p>Generic interface for database connections.</p>
		 * @author Patrick Nicolas
		 * @date 05/14/2012
		 */
	public interface IConnection {
		/**
		 * <p>Close database connections created in the constructor
		 * of this particular type of connection (data type).</p>
		 */
		public void close();
	}
	
	
	/**
	 * <p>Generic interface for database connections that query, update and insert
	 * taxonomy information.</p>
	 * @author Patrick Nicolas
	 * @date 05/14/2012
	 */

	public class NTaxonomyConnection implements IConnection {
		private final static String SELECT_ONTOLOGY_FROM_ENTRY 	= "SELECT wordnet FROM 24x7c.dbpedia WHERE label=?;";
		private final static String SELECT_ONTOLOGY_FROM_ALIAS 	= "SELECT db.wordnet FROM 24x7c.dbpedia db JOIN  24x7c.dbpedia_aliases da ON db.id = da.resourceid WHERE da.label=?;";

		private CSqlPreparedStmt 	_sqlEntryPreparedStmt 	= null;
		private CSqlPreparedStmt 	_sqlAliasPreparedStmt 	= null;
		
		public NTaxonomyConnection() {
			_sqlEntryPreparedStmt = new CSqlPreparedStmt(SELECT_ONTOLOGY_FROM_ENTRY);
			_sqlAliasPreparedStmt = new CSqlPreparedStmt(SELECT_ONTOLOGY_FROM_ALIAS);
		}
		
		
		public final CSqlPreparedStmt getSqlEntryPreparedStmt() {
			return _sqlEntryPreparedStmt;
		}
		
		public final CSqlPreparedStmt getSqlAliasPreparedStmt() {
			return _sqlAliasPreparedStmt;
		}
		
		/**
		 * <p>Close database connections that query and update taxonomy data.</p>
		 */
		@Override
		public void close() {
			if(_sqlEntryPreparedStmt != null) {
				_sqlEntryPreparedStmt.close();
			}
			if( _sqlAliasPreparedStmt != null) {
				_sqlAliasPreparedStmt.close();
			}
		}
	}
	

	private Map<EDATA_TYPE, List<IConnection>> _connectionsMap = null;
	
	/**
	 * <p>Close all the database connections of any type from the pool.</p>
	 */
	public void close() {
		if( _connectionsMap.size() > 0) {
			for( List<IConnection> connectionsList : _connectionsMap.values()) {
				if( connectionsList.size() > 0) {
					for( IConnection connection : connectionsList) {
						connection.close();
					}
				}
			}
		}
	}

	
	/**
	 * <p>Retrieve a connection of a specific data type.</p>
	 * @param dat
	 * @return
	 */
	public IConnection getConnection(EDATA_TYPE dataType) {
		List<IConnection> connectionsList = _connectionsMap.get(dataType);
		
		IConnection connection = null;
		if(dataType == EDATA_TYPE.TAXONOMY) {
			connection = new NTaxonomyConnection();
		}
		connectionsList.add(connection);		
		return connection;
	}
	
	
						// -------------------------
						//  Private Methods
						// ------------------
	
	private CDbConnectionsPool() {
		_connectionsMap = new HashMap<EDATA_TYPE, List<IConnection>>();
		_connectionsMap.put(EDATA_TYPE.TAXONOMY, new LinkedList<IConnection>());
	}

}

// ------------------------------  eof -----------------------------------

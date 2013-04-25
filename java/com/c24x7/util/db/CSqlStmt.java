/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.c24x7.util.logs.CLogger;



		/**
		 * <p>Generic class that wraps a JDBC Statement. 
		 * The constructor create and initialize both the connection and
		 * the statement. The connection is to be closed, manual by a call
		 * to the close method.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date June 3, 2012 5:37:30 PM
		 */

public final class CSqlStmt extends ASqlRequest {
	protected Statement _stmt = null;
	protected String 	_query = null;
	
		/**
		 * Create an instance of a statement and JDBC connection.
		 */
	public CSqlStmt(){
		super();
		try {
			setConnection();
			_stmt = _con.createStatement();
		}
		catch( SQLException e) {
			close();
			CLogger.error(e.toString());
		}
	}
	
	
	public CSqlStmt(final String query){
		this();
		set(query);
	}
	
	
		/**
		 * <p>Set the query string for this Statement object.</p>
		 * @param query SQL query string.
		 */
	public void set(final String query) {
		_query = query;
	}
	
		/**
		 * <p>Execute a query for this statement object.</p>
		 * @throws A SQLException if either the query or the connection fails
		 * @return results set.
		 */
	public ResultSet query() throws SQLException {
		return ( _stmt != null) ? _stmt.executeQuery(_query) : null;
	}
	
		/**
		 * <p>Execute an update for this statement object.</p<
		 * @throws A SQLException if either the query or the connection fails
		 * @return number of database table records updated.
		 */
	public int update() throws SQLException {
		return (_stmt != null) ? _stmt.executeUpdate(_query) : -1;
	}
	
	
	/**
	 * <p>Execute an INSERT statement object.</p<
	 * @throws A SQLException if either the query or the connection fails
	 * @return true if the insertion is committed, false otherwise
	 */
	public boolean insert() throws SQLException {
		return ( _stmt != null) ? _stmt.execute(_query) : false;
	}
	
	
	
		/**
		 * <p>Close the JDBC statement and connection.</p> 
		 */
	public void close() {
		try {
			if( _stmt != null) {
				_stmt.close();
				_stmt = null;
			}
			if( _con != null) {
				_con.close();
			}
		}
		catch(SQLException e) {
			CLogger.error("Cannot close database connection " + e.toString());
		}
	}
	
}

// ------------------------------  EOF -------------------------

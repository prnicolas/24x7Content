/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.util.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.c24x7.util.logs.CLogger;


	/**
	 * <p>Generic class that wraps a JDBC prepared statement statement.
	 * The constructor create and initialize both the connection and
	 * the statement. The connection is to be closed, manual by a call
	 * to the close method.
	 * </p>
	 * @author Patrick Nicolas         24x7c 
	 * @date June 3, 2012 5:37:30 PM
	 */
public class CSqlPreparedStmt extends ASqlRequest {
	protected PreparedStatement _pstmt = null;
	
	
		/**
		 * <p>Create the connection for a prepared statement. The prepared statement is
		 * defined a run time.</p>
		 */
	public CSqlPreparedStmt() {
		super();
		setConnection();
	}
	
		/**
		 * <p>Create a connection and a prepared statement with a defined statement string.</p>
		 * @param preparedStmt string defining the prepared statement
		 */
	public CSqlPreparedStmt(final String preparedStmt) {
		super();
		setConnection();
		setStmt(preparedStmt);
	}

		/**
		 * <p>Initialize the prepared statement for this connection.</p>
		 * @param preparedStmt string defining the prepared statement
		 */
	public void setStmt(final String preparedStmt) {
		try {
			_pstmt = _con.prepareStatement(preparedStmt);
		}
		catch( SQLException e) {
			close();
			CLogger.error(e.toString());
		}
	}
	
	/**
	 * <p>Initialize the prepared statement parameter with a characters string
	 * as a specified index.</p>
	 * @param index index in the prepared statement parameter
	 * @param field value of string to be set for the prepared statement
	 * @throws SQLException if the connection or the prepared statement fails.
	 */

	public void set(int index, final String field) throws SQLException {
		if( _pstmt != null ) {
			_pstmt.setString(index, field);
		}
	}
	
	/**
	 * <p>Initialize the prepared statement parameter with a integer
	 * as a specified index.</p>
	 * @param index index in the prepared statement parameter
	 * @param field value of float point variable to be set for the prepared statement
	 * @throws SQLException if the connection or the prepared statement fails.
	 */
	public void set(int index, final float field) throws SQLException {
		if( _pstmt != null ) {
			_pstmt.setFloat(index, field);
		}
	}
	
		/**
		 * <p>Initialize the prepared statement parameter with a integer
		 * as a specified index.</p>
		 * @param index index in the prepared statement parameter
		 * @param field value of integer to be set for the prepared statement
		 * @throws SQLException if the connection or the prepared statement fails.
		 */
	public void set(int index, final int field) throws SQLException {
		if( _pstmt != null ) {
			_pstmt.setInt(index, field);
		}
	}
	
	
	/**
	 * <p>Execute a query for this statement object.</p>
	 * @throws A SQLException if either the query or the connection fails
	 * @return results set.
	 */
	public ResultSet query() throws SQLException {
		if( _pstmt == null) {
			throw new SQLException("Prepared statement is undefined");
		}
		return _pstmt.executeQuery();
	}
	
	/**
	 * <p>Execute an update for this prepared statement object.</p<
	 * @throws A SQLException if either the query or the connection fails
	 * @return number of database table records updated.
	 */
	public int update() throws SQLException {
		if( _pstmt == null) {
			throw new SQLException("Prepared statement is undefined");
		}

		return _pstmt.executeUpdate();
	}

	/**
	 * <p>Execute an INSERT prepared statement object.</p<
	 * @throws A SQLException if either the query or the connection fails
	 * @return true if the insertion is committed, false otherwise
	 */
	public boolean insert() throws SQLException {
		if( _pstmt == null) {
			throw new SQLException("Prepared statement is undefined");
		}

		return _pstmt.execute();
	}
	
	
		/**
		 * <p>Close the JDBC prepared statement and the connection.</p>
		 */
	public void close() {
		try {
			if( _pstmt != null) {
				_pstmt.close();
				_pstmt = null;
			}
			if( _con != null) {
				_con.close();
			}
		}

		catch(SQLException e) {
			CLogger.error("Cannot close database connection " + e.toString());
		}
	}
	
	
	@Override
	public String toString() {
		String description = null;
		if(_pstmt != null) {
			description = _pstmt.toString();
		}
		
		return description;
	}
}

// ------------------------------------  EOF ------------------------------------------
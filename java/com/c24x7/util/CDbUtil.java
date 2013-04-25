// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


			/**
			 * <p>Utility class to evaluate and manage database connectivity,
			 * access and performance.</p>
			 * @author Patrick Nicolas
			 * @date 08/21/2011
			 */

public final class CDbUtil {
	protected static final String JDBC_DRIVER_NAME 	= "com.mysql.jdbc.Driver";
	protected static final String JDBC_URL  		= "jdbc:mysql://localhost:3306/mysql";
	protected static final String JDBC_ROLE 		= "root";
	protected static final String JDBC_PWD 		 	= "jbk010659";

	protected String  _pwd 	= JDBC_PWD;
	protected String  _role	= JDBC_ROLE;
	protected String  _url 	= JDBC_URL;
	protected Statement	_stmt   = null;
	
	public CDbUtil() {
		this(JDBC_URL, JDBC_ROLE, JDBC_PWD);
	}
	
	public CDbUtil(  final String role, final String pwd) {
		this(JDBC_URL, role, pwd);
	}
	
	public CDbUtil(final String url, final String role, final String pwd) {
		_url = url;
		_role = role;
		_pwd = pwd;
	}
	
	public Statement create()  {
		if( _stmt == null ) {

			try {
				//Register the JDBC driver for MySQL.
				Class.forName(JDBC_DRIVER_NAME);
			
				//Define URL of database server...
				Connection con = DriverManager.getConnection(_url, _role, _pwd);

				//Get a Statement object
				_stmt = con.createStatement();
			}
			catch( ClassNotFoundException e) {
				CLogger.error("Database driver not found: " + e.toString());
			}
			catch(SQLException e) {
				CLogger.error("Database not available: " + e.toString());
			}
		}
		return _stmt;
	}
	

	
	public final Statement getStmt() {
		return _stmt;
	}
	
	public void close() {
		if( _stmt != null) {
			try {
				_stmt.close();
				_stmt = null;
			}
			catch(SQLException e) {
				CLogger.error("Cannot close database connection " + e.toString());
			}
		}
	}
	
	
	public static String getUpdateStmt(final String tableName,
            						   final String condName,
            						   final String condValue, 
            						   final String columnName,
            						   final String columnValue)  throws SQLException, UnsupportedEncodingException  {

		StringBuilder buf = new StringBuilder("UPDATE ");
		buf.append(tableName);
		buf.append(" SET ");
		buf.append(columnName);
		buf.append("=\'");
		buf.append(CStringUtil.encodeLatin1(columnValue));
		buf.append("\' WHERE ");
		buf.append(condName);
		buf.append("=\'");
		buf.append(condValue);
		buf.append("\';");
		return buf.toString();
	}
	
	
	public static String getUpdateStmt(final String tableName,
									final String idName,
									final long idValue,
			                        final String conditionName,
			                        final String conditionValue)  throws SQLException, UnsupportedEncodingException  {
		
		StringBuilder buf = new StringBuilder("UPDATE ");
		buf.append(tableName);
		buf.append(" SET ");
		buf.append(idName);
		buf.append("=");;
		buf.append(idValue);			
		buf.append(" WHERE ");
		buf.append(conditionName);
		buf.append("=\'");
		buf.append(conditionValue);
		buf.append("\';");
		
		return buf.toString();
	}

	
	
	
	
	public static String getInsertStmt(final String tableName,
			  					final String[] colNames,
			  					final String[] colValues)  throws SQLException, UnsupportedEncodingException  {

		StringBuilder buf = new StringBuilder("INSERT INTO ");
		buf.append(tableName);
		buf.append(" (");
		
		int k = 0;
		for( String colName : colNames) {
			buf.append(colName);
			if( ++k < colNames.length ) {
				buf.append(", ");
			}
		}
		
		buf.append(") VALUES (\'");
		k = 0;
		for( String colVal : colValues) {
			buf.append(CStringUtil.encodeLatin1(colVal));
			if( ++k < colValues.length ) {
				buf.append("\', \'");
			}
		}
		buf.append("\');");
		return buf.toString();
	}

	
	public static String getSelectStmt(final String tableName,
									   final String conditionName, 
									   final String conditionValue, 
									   final String fields) {
		StringBuilder buf = new StringBuilder("SELECT ");
		buf.append(fields);
		buf.append(" FROM ");
		buf.append(tableName);
		buf.append(" WHERE ");
		buf.append(conditionName);
		buf.append("=\'");
		buf.append(CStringUtil.encodeLatin1(conditionValue));
		buf.append("\';");
		
		return buf.toString();
	}
	
	
	public static long getMaxId(final String tableName, Statement stmt) {
		ResultSet rs = null;
		long maxId = -1L;

		try {
			StringBuilder buf = new StringBuilder("SELECT MAX(id) FROM ");
			buf.append(tableName);
			buf.append(";");
			
			rs = stmt.executeQuery(buf.toString());
			while( rs.next() ) {
				maxId = rs.getInt("MAX(id)");
			}
		}
		catch( SQLException e) {
			CLogger.error("Could not extract the maximum id for dbpedia");
		}
		
		return maxId;
	}
}

// ---------------------------  EOF ----------------------------------
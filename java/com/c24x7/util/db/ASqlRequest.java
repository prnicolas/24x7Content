/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */

 package com.c24x7.util.db;

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
public abstract class ASqlRequest {
	protected static final String JDBC_DRIVER_NAME 	= "com.mysql.jdbc.Driver";
	protected static final String JDBC_URL  		= "jdbc:mysql://127.0.1:3306/mysql";
	protected static final String JDBC_ROLE 		= "root";
	protected static final String JDBC_PWD 		 	= "jbk010659";

	protected static String  pwd	= JDBC_PWD;
	protected static String  role 	= JDBC_ROLE;
	protected static String  url 	= JDBC_URL;
	protected Connection _con = null;
	
	public ASqlRequest() { }
	

	protected void setConnection() {
		try {
			//Register the JDBC driver for MySQL.
			Class.forName(JDBC_DRIVER_NAME);
		
			//Define URL of database server...
			_con = DriverManager.getConnection(url, role, pwd);
		}
		catch( ClassNotFoundException e) {
			CLogger.error("Database driver not found: " + e.toString());
		}
		catch(SQLException e) {
			CLogger.error("Database not available: " + e.toString());
		}
	}
	
	
	abstract public ResultSet query() throws SQLException;
	
	abstract public int update() throws SQLException;

	abstract public boolean insert() throws SQLException;
	
	
	abstract public void close();

	
	
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
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.c24x7.util.CDbUtil;
import com.c24x7.util.CEnv;
import com.c24x7.util.string.CStringUtil;


		/**
		* <p>Dbpedia extraction step 2:<br>
		* Inner class that manages the generation of index to extract the short abstract from the long abstract
		* extracted from Wikipedia data sets..</p>
		* @see com.c24x7.nlservices.namefinders.NISQLStatement
		* @author Patrick Nicolas
		* @date 07/22/2011
		*/
public final class CDbpediaShAbstractEtl extends CDbpediaLgAbstractEtl {
	protected static String DBPEDIA_FILE = CEnv.configDir + "/dbpedia/short_abstracts_en.nt";
	
	public CDbpediaShAbstractEtl() {
		super();
	}
			/**
			* <p>Retrieve the name of the file that contains the dump of dbpedia to insert.</p>
			* @return name of the file containing this specific dbpedia short abstract
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
		return (_extractor.extractLabel(newLine) != null);
			
		/*
		if( label != null ) {
			String content = extractAbstract(newLine);
			try {
				success = (content != null && executeSqlUpdate(label, content));
			}
			catch (SQLException e) {
				success = false;
				CLogger.error("Cannot add long abstract " + e.toString());
			}
		}
		*/
	}
	
	
	protected boolean executeSqlUpdate(Statement stmt, 
						             final String label,
						             final String shAbstract) throws SQLException {
		
		boolean succeed = false;
				/*
				 * Extract the long version of the abstract from the existing database
				 */
		String sqlQuery = CDbUtil.getSelectStmt("24x7c.dbpedia", "label", CStringUtil.encodeLatin1(label), "id, lgabstract");
		ResultSet rs = stmt.executeQuery(sqlQuery);
		
		long id = -1L;
		String lgAbstract = null;
		while(rs.next()) {
			id = rs.getInt("id");
			lgAbstract = rs.getString("lgabstract");
		}
		
		if(lgAbstract != null ) {
					/*
					 * Extract the local reference 
					 */
			lgAbstract = CStringUtil.decodeLatin1(lgAbstract);
			int indexShAbstract = lgAbstract.indexOf(shAbstract);
					/*
					 * Update the index used to extract short abstract from the long abstract.
					 */
			if( indexShAbstract != -1) {
				StringBuilder buf = new StringBuilder("UPDATE 24x7c.dbpedia SET abstractindx=");
				buf.append(indexShAbstract+ shAbstract.length());
				buf.append(" WHERE id=");
				buf.append(id);
				buf.append(";");
				succeed = stmt.execute(buf.toString());
			}
		}
		return succeed;
	}
}

// --------------------------  EOF ---------------------------------
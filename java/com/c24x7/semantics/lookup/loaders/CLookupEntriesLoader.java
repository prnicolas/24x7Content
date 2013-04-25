// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.lookup.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.semantics.lookup.CLookupGenerator.NLookupRecordsMap;
import com.c24x7.semantics.lookup.CLookupRecord;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Inner class that generates a lookup entry (type, idf value) for
		 * a term extracted from the Wikipedia entries database.<p>
		 * 
		 * @see com.c24x7.semantics.lookup.loaders.ILookupLoader
		 * @author Patrick Nicolas
		 * @date 11/15/2011
		 */

public class CLookupEntriesLoader implements ILookupLoader {
	private final static String SELECT_DBPEDIA_LABEL = "SELECT label,idf,taxonomy,sub_taxonomy,wnet FROM 24x7c.dbpedia WHERE id=?";

	private CSqlPreparedStmt 	_pStmt 				= null;
	private NLookupRecordsMap	_lookupRecordsMap 	= null;
	private boolean				_wordNetOnly 		= true;
	
	
		/**
		 * <p>Create a loader for generating the lookup table 
		 * from the original entries in Wikipedia reference database.</p>
		 * @param lookupRecordsMap lookup map to be updated.
		 */
	public CLookupEntriesLoader(NLookupRecordsMap lookupRecordsMap) {
		this(lookupRecordsMap, true);
	}
	
	
		/**
		 * <p>Create a loader instance to populate the lookup table for
		 * Wikipedia reference database entries.</p>
		 * @param lookupRecordsMap lookup table structure to create
		 * @param wordNetOnly flag to specify that only the Wikipedia entry with associated WordNet definition can be loaded if true.
		 */
	public CLookupEntriesLoader(NLookupRecordsMap lookupRecordsMap, boolean wordNetOnly) { 
		if( lookupRecordsMap == null) {
			throw new IllegalArgumentException("Cannot load Wikipedia aliases into a look up table");
		}
		
		_pStmt = new CSqlPreparedStmt(SELECT_DBPEDIA_LABEL);
		_lookupRecordsMap = lookupRecordsMap;
		_wordNetOnly = wordNetOnly;
	} 
	
	/**
	 * <p>Extract the lookup data (entry type, idf value) from 
	 * the table of Wikipedia entries.</p>
	 * 
	 * @param id  row id in the database table
	 * @return true if extraction succeeds, false otherwise
	 * @throws SQLException if the query to the database failed.
	 */
	@Override
	public boolean extract(long id) throws SQLException {
		boolean succeed = false;
		
		_pStmt.set(1, id);
		ResultSet rs = _pStmt.query();
		
		String 	keyword 		= null,
		   		taxonomy 	= null,
		   		sub_taxonomy = null;
		
		double idf = UNKNOWN_NGRAM_IDF;
		int wordnetFlag = -1;
		
		if( rs.next() ) {
			keyword = rs.getString("label");
			idf = rs.getDouble("idf");
			taxonomy = rs.getString("taxonomy");
			sub_taxonomy = rs.getString("sub_taxonomy");
			wordnetFlag = rs.getInt("wnet");
		}
		
			/*
			 * If the entry is valid with at least a label..
			 */
		if( keyword != null && (!_wordNetOnly || wordnetFlag == 1)) {
			keyword = CStringUtil.decodeLatin1(keyword);
			
			if( keyword.length() > 2) {
				keyword = keyword.trim();
				String[] numberTerms = keyword.split(" ");
				
				/*
				 * We restrict the size of N-Gram to be looked up
				 * in the cached table to 4.
				 */
				if( (numberTerms.length <= MAX_NUM_TERMS_PER_ENTRY) &&
				    ((taxonomy != null && taxonomy.length()> 2) || 
					 (sub_taxonomy != null && sub_taxonomy.length() > 2)) ) {
					
					char type = Character.isUpperCase(keyword.charAt(0)) ? CLookup.DPBEDIA_ENTRY_UPPER_CASE : CLookup.DBPEDIA_ENTRY;
					CLookupRecord lookupRecord = new CLookupRecord(type, (float)idf);
					_lookupRecordsMap.put(keyword, lookupRecord);
					succeed = true;
				}
			}
		}
		
		return succeed;
	}
	
	/**
	 * <p>Close all database connections used in the extraction
	 * of lookup data and inverse document frequency.</p>
	 */
	@Override
	public void close() {
		_pStmt.close();
	}
}

// ------------------------  EOF -----------------------------------------------
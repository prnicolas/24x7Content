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
		 * a term extracted from the Wikipedia aliases database.<p>
		 * 
		 * @see com.c24x7.semantics.lookup.loaders.ILookupLoader
		 * @author Patrick Nicolas
		 * @date 11/15/2011
		 */
					
public final class CLookupAliasesLoader implements ILookupLoader {
	private final static String SELECT_DBPEDIA_ALIAS_LABEL 	= "SELECT label,resourceid,idf FROM 24x7c.dbpedia_aliases WHERE id=?";
	private final static String SELECT_DBPEDIA_ONTOLOGY 	= "SELECT taxonomy,sub_taxonomy,wnet FROM 24x7c.dbpedia WHERE id=?";
	
	private CSqlPreparedStmt 	_pOntoStmt 			= null;
	private CSqlPreparedStmt 	_pStmt 				= null;
	private NLookupRecordsMap	_lookupRecordsMap 	= null;
	private boolean				_wordnetTaxonomyOnly = true;
	
	
		/**
		 * <p>Create a loader instance to populate the lookup table for
		 * Wikipedia reference aliases database entries.</p>
		 * @param lookupRecordsMap lookup table structure to create
		 * @param wordnetTaxonomyOnly  load the entries that have Wordnet taxonomy defined only
		 */
	public CLookupAliasesLoader(NLookupRecordsMap lookupRecordsMap, boolean	wordnetTaxonomyOnly) {
		if( lookupRecordsMap == null) {
			throw new IllegalArgumentException("Cannot load Wikipedia aliases into a look up table");
		}
		
		_pStmt = new CSqlPreparedStmt(SELECT_DBPEDIA_ALIAS_LABEL);
		_pOntoStmt = new CSqlPreparedStmt(SELECT_DBPEDIA_ONTOLOGY);
		_lookupRecordsMap = lookupRecordsMap;
		_wordnetTaxonomyOnly = wordnetTaxonomyOnly;
	}
	
		
	/**
	 * <p>Extract the lookup data (entry type, idf value) from the table of Wikipedia aliases.</p>
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
		
		String keyword = null;
		long resourceid = -1L;
		double idf = UNKNOWN_NGRAM_IDF;
		
		if( rs.next() ) {
			keyword = rs.getString("label");
			resourceid = rs.getInt("resourceid");
			idf = rs.getDouble("idf");
		}
		
		if( keyword != null && resourceid > 0) {
			keyword = CStringUtil.decodeLatin1(keyword);
			if( keyword.length() > 2) {
				keyword = keyword.trim();
				String[] numberTerms = keyword.split(" ");
				
				/*
				 * Test if the entry associated with the alias.resourceid == entry.id
				 * has a taxonomy or a sub_taxonomy defined ...
				 */
				if( numberTerms.length <= ILookupLoader.MAX_NUM_TERMS_PER_ENTRY && 
				   hasTaxonomyFromId(resourceid)) {
		
					char type = Character.isUpperCase(keyword.charAt(0)) ? CLookup.DPBEDIA_ENTRY_ALIAS_UPPER_CASE : CLookup.DPBEDIA_ENTRY_ALIAS;

					CLookupRecord lookupRecord = new CLookupRecord(type, (float)idf);		
					_lookupRecordsMap.put( keyword, lookupRecord);
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
		_pOntoStmt.close();
	}
	
				// ----------------------------
				//  Private Supporting Methods
				// -----------------------------
	
	private boolean hasTaxonomyFromId(long id) throws SQLException {
		_pOntoStmt.set(1, id);
		String taxonomy = null,
		       sub_taxonomy = null;
		int wordnetTaxonomyOnlyFlag = -1;
		
		ResultSet rs = _pOntoStmt.query();						
		if( rs.next() ) {
			taxonomy = rs.getString("taxonomy");
			sub_taxonomy = rs.getString("sub_taxonomy");
			wordnetTaxonomyOnlyFlag = rs.getInt("wnet");
		}
		
		return (!_wordnetTaxonomyOnly || wordnetTaxonomyOnlyFlag ==1) && 
			   ((taxonomy != null && taxonomy.length() > 2) || (sub_taxonomy != null && sub_taxonomy.length() > 2));
	}
}

//--------------------------  EOF ----------------------------------

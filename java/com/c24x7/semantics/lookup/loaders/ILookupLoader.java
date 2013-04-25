/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.semantics.lookup.loaders;

import java.sql.SQLException;


		/**
		 * <p>Generic interface to extract inverse document frequency (IDF)
		 * values from the database.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 11/26/2011
		 */
public interface ILookupLoader {
	public static final int 	MAX_NUM_TERMS_PER_ENTRY = 4;
	public static final float 	UNKNOWN_NGRAM_IDF		= 0.965F;
	
	/**
	 * <p>Extract the lookup data (entry type, idf value) from 
	 * the database.</p>
	 * 
	 * @param id  row id in the database table
	 * @return true if extraction succeeds, false otherwise
	 * @throws SQLException if the query to the database failed.
	 */
	public boolean extract(long id) throws SQLException;
	
	/**
	 * <p>Close all database connections used in the extraction
	 * of lookup data and inverse document frequency.</p>
	 */
	public void close();
}

// ------------------------------------------ EOF ------------------------------

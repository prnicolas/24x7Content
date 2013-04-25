//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.semantics.lookup;

import com.c24x7.util.CEnv;



			/**
			 * <p>Class that defines the lookup record, containing the inverse document
			 * frequency, a type of record. The label of the lookup is extracted at run time.
			 * A lookup record is maintained  in memory (cached) for computation efficiency. 
			 * The record is composed of type {Entry or Alias table, with/without associated taxonomy),
			 * a IDF value and Wikipedia label.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 02/12/2012
			 */
public final class CLookupRecord {
	private char  	_type = CLookup.DBPEDIA_UNDEFINED;
	private float 	_idf  = -1.0F;
	private String 	_label = null;
	
		/**
		 * <p>Constructor to create a lookup record with a define entry type
		 * and an inverse document frequency (IDF) </p>
		 * @param type type of the entry in Wikipedia for this record
		 * @param idf inverse document frequency associated with this record.
		 */
	public CLookupRecord(char type, float idf) {
		_type = type;
		_idf = idf;
	}
	
	public final char getType() {
		return _type;
	}
	
	public final float getIdf() {
		return _idf;
	}
	
	public void setLabel(final String label) {
		_label = label;
	}
	
	public final String getLabel() {
		return _label;
	}
	
		/**
		 * <p>Test if the first character of the original Wikipedia label was upper case.</p>
		 * @return true if the label had a upper case,, false otherwise
		 */

	public boolean isFirstCharUpperCase() {
		return 	_type == CLookup.DPBEDIA_ENTRY_UPPER_CASE || 
				_type == CLookup.DPBEDIA_ENTRY_ALIAS_UPPER_CASE;
	}
		
	
	@Override 
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.valueOf(_type));
		buf.append(CEnv.KEY_VALUE_DELIM);
		buf.append(_idf);
		
		return buf.toString();
	}
}

// ------------------------------ EOF -------------------------------------
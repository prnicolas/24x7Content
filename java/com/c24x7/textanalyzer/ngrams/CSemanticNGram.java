// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.ngrams;


import com.c24x7.semantics.lookup.CLookupRecord;
import com.c24x7.semantics.lookup.CLookup;



		/**
		 * <p>Class that defines a semantic record that contains a label, 
		 * a inverse document frequency, IDF and the number of occurrences in the
		 * document or document document and a state(with or without ontology, 
		 * alias or actual Wikipedia entry.)  </p>
		 * @author Patrick Nicolas
		 * @date 02/12/2012
		 */
public final class CSemanticNGram {
	private char 		_state 			= CLookup.DBPEDIA_UNDEFINED;
	private float 		_idf 			= 0.0F;
	private String 		_label 			= null;
	private boolean 	_is1Gram 		= true;
	private String		_original		= null;
	private int			_count 			= 1;

	
		/**
		 * <p>Create a record that contains a Wikipedia label, a state (with or
		 * without ontology, alias entry.), a relative term frequency and
		 * the number of occurrences of the label in a document.</p>
		 * @param record Wikipedia record contains in the lookup table
		 * @param label label of the N-Gram 
		 * @param numOccurrences number of occurrences of this label (N-Gram) in the document
		 */
	public CSemanticNGram(final CLookupRecord record, final String label) {
		_state = record.getType();
		_idf = record.getIdf();
		_label = label;		
	}
	
	public void setOriginal(final String originalLabel) {
		_original = originalLabel;
	}
	

	
	
	public void incrCount() {
		_count++;
	}
	
	public void incrCount(int incr) {
		_count += incr;
	}
	
	public int getCount() {
		return _count;
	}
		
		/**
		 * <p>Access the state of this semantic record.</p>
		 * @return state of the record.
		 */
	public final char getState() {
		return _state;
	}
	
		/**
		 * <p>Access the label of this semantic record (or N-Gram).</p>
		 * @return label of the record 
		 */
	public final String getLabel() {
		return _label;
	}
	
	
	public float getIdf() {
		return _idf;
	}
	
	public void is1Gram(boolean is1Gram) {
		_is1Gram = is1Gram;
	}
	
	public boolean is1Gram() {
		return _is1Gram;
	}
	

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_label);
		buf.append(": State=");
		buf.append(String.valueOf(_state));
		buf.append(", idf=");
		buf.append(String.valueOf(_idf));
		buf.append(", cnt=");
		buf.append(String.valueOf(_count));
		
		return buf.toString();
	}

}

// -----------------------------------  EOF -----------------------

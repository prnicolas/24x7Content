package com.c24x7.textanalyzer.ngrams;

import java.util.HashMap;

import com.c24x7.semantics.lookup.CLookupRecord;




		/**
		 * <p>Hash table of semantic N-Grams, validated against a semantic
		 * data source such as Wikipedia or Freebase.</p>
		 * @author Patrick Nicolas
		 * @date 05/12/2012
		 */
public final class CSemanticNGramsMap extends HashMap<String, CSemanticNGram>{

	private static final long serialVersionUID = 3230311881557837249L;
	
	private int _maxFrequency = 0;
	
	public int getMaxFrequency() {
		return _maxFrequency;
	}
	
	
		/**
		 * <p>Create a Semantic N-Gram and add it to the current map of semantic
		 * N-Grams associated with this document or document document.</p>
		 * @param lookupRecord lookup record extracted from Dbpedia
		 * @param sentenceIndex index of the sentence the N-Gram belong to.
		 * @return a new Semantic N_Gram.
		 */
	public CSemanticNGram put(final CLookupRecord lookupRecord, int sentenceIndex) {
		
		CSemanticNGram semNGram = null;
		final String label = lookupRecord.getLabel();
		
		if(containsKey(label)) {
			semNGram = get(label);
			semNGram.incrCount();
			
			if(semNGram.getCount() > _maxFrequency) {
				_maxFrequency = semNGram.getCount();
			}
		}
			
		else {
			semNGram = new CSemanticNGram(lookupRecord, label);
			super.put(label, semNGram);
		}

			
		return semNGram;
	}

	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(CSemanticNGram semNGram : values()) {
			buf.append(semNGram.toString());
			buf.append("\n");
		}
		
		return buf.toString();
	}
}

// ------------------------  EOF -------------------------------------------

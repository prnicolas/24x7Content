package com.c24x7.models;

import java.util.HashMap;

import com.c24x7.semantics.lookup.CLookupRecord;


public class CTaxonomyObjectsMap extends HashMap<String, CTaxonomyObject> {

	private static final long serialVersionUID = 4937028821387906651L; 

	private int _maxFrequency = 0;
	
	public int getMaxFrequency() {
		return _maxFrequency;
	}


		/**
		 * <p>Create a Semantic N-Gram and add it to the current map of semantic
		 * N-Grams associated with this document or document document.</p>
		 * @param lookupRecord lookup record extracted from Dbpedia
		 * @param sentenceIndex index of the sentence the N-Gram belong to.
		 * @return a new Semantic N-Gram as a taxonomy instance or object
		 */
	public CTaxonomyObject put(final CLookupRecord lookupRecord, int sentenceIndex) {
		
		CTaxonomyObject taxonomyObject = null;
		final String label = lookupRecord.getLabel();
		
		/*
		 * If either the original or lower case version
		 * of the semantic N-Gram label is not already
		 * registered in the taxonomyObjects map..
		 */
		if( containsKey(label)) {
			taxonomyObject = get(label);
			taxonomyObject.incrCount();
			
			if(taxonomyObject.getCount() > _maxFrequency) {
				_maxFrequency = taxonomyObject.getCount();
			}
		}
			
		else {
			String labelLc = label.toLowerCase();
			if( !containsKey(labelLc)) {
				taxonomyObject = new CTaxonomyObject(lookupRecord, label);
				super.put(label, taxonomyObject);
			}
		}
		
			/*
			 * If the index of the sentence has been recorded, add
			 * it as an attribute of the Semantic N-Gram.
			 */
		if( taxonomyObject != null && sentenceIndex >= 0) {
			taxonomyObject.addSentenceIndex(sentenceIndex);
		}
			
		return taxonomyObject;
	}

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(CTaxonomyObject taxonomyInstance : values()) {
			buf.append(taxonomyInstance.toString());
			buf.append(" ");
		}
		
		return buf.toString();
	}


}

// -----------------------------  EOF --------------------------------------------
package com.c24x7.models;

import java.util.HashMap;


import com.c24x7.textanalyzer.ngrams.CSemanticNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedNGram;



public final class CTaxonomyInstancesMap extends HashMap<String, CTaxonomyInstance> {

	private static final long serialVersionUID = 1097941948501133462L;

	public String addNGram(final CSemanticNGram semRecord, final CTaggedNGram nGram) {
		if( semRecord == null) {
			throw new IllegalArgumentException("Cannot add undefined semantic record to the taxonomyInstances map");
		}
		CTaxonomyInstance taxonomyInstance = new CTaxonomyInstance(semRecord, nGram);
		super.put(taxonomyInstance.getLabel(), taxonomyInstance);
		return taxonomyInstance.getLabel();
	}
	
	
	
	
	public String addInstance(final CSemanticNGram semRecord, CText document) {
		if( semRecord == null) {
			throw new IllegalArgumentException("Cannot add undefined semantic record to the taxonomyInstances map");
		}
		
		String result = null;
		String label = semRecord.getLabel();
		
		if( !containsKey(label)) {
			/*
			 * If either the original or lower case version
			 * of the semantic N-Gram label is not already
			 * registered in the taxonomyInstances map..
			 */
			String labelLc = label.toLowerCase();
			if( !containsKey(labelLc)) {
				CTaxonomyInstance newTaxonomyInstance = new CTaxonomyInstance(semRecord);
				super.put(label, newTaxonomyInstance);
			}
			else {
				result = label;
			}
		}
		else {
			result = label;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(CTaxonomyInstance taxonomyInstance : values()) {
			buf.append(taxonomyInstance.toString());
			buf.append(" ");
		}
		
		return buf.toString();
	}
}

// -------------------------  EOF --------------------------------------
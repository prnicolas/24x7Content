// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.tags;

import java.util.HashMap;
import java.util.Map;

import com.c24x7.util.CIntMap;


			/**
			 * <p>Class used to collect statistics on the frequency of occurrence of
			 * term tags in corpus (Wikipedia).The list of tags can be specified </p>
			 * @author Patrick Nicolas
			 * @date 03/11/2012
			 */
public final class CTagsStats extends HashMap<String, CIntMap> {

	private static final long serialVersionUID = -6239220264053059515L;
	private Map<String, Object> _tagsMap = new HashMap<String, Object>();
	
			/**
			 * <p>Creates a Tag statistics object used by the N-Gram tag classifier
			 * for the supervised learning of the N-Gram tag model.</p>
			 * @param tagsList list of tags for which statistics have to be collected
			 * @throws IllegalArgumentException if the list of tags is undefined.
			 */
	public CTagsStats(final String[] tagsList) {
		if(tagsList == null) {
			throw new IllegalArgumentException("Cannot create statistics for undefined tags");
		}
		_tagsMap  = new HashMap<String, Object>();
		for(String tag : tagsList) {
			_tagsMap.put(tag, null);
		}
	}
	
		/**
		 * <P>Add a new observation of a tag within the corpus.</p>
		 * @param tag tag of the 1-Gram word extracted from the corpus
		 * @param term 1-Gram word extracted from the corpus
		 * @return update table of terms-frequency associated with a specific tags.
		 */
	public CIntMap put(final String tag, final String term) {
		CIntMap termsMap = null;
		
		if( _tagsMap.containsKey(tag)) {
			if( containsKey(tag)) {
				termsMap = get(tag);
				termsMap.put(term);
			}
			else {
				termsMap = new CIntMap();
				termsMap.put(term);
				put(tag, termsMap);
			}
		}
		return termsMap;
	}
	
	
		/**
		 * <p>Create a textual representation of this object, displaying
		 * tag and their associated terms, frequencies.</p>
		 * @return list of tags and associated terms collected from the reference corpus.
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		CIntMap termsFrequencies = null;
		for( String tag : keySet()) {
			buf.append(tag);
			buf.append("\n");
			termsFrequencies = get(tag);
			buf.append(termsFrequencies);
		}
		
		return buf.toString();
	}
}

// -------------------------- EOF ------------------------------------------
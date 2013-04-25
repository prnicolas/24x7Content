package com.c24x7.semantics.lookup;

import java.io.IOException;

import com.c24x7.exception.InitException;
import com.c24x7.textanalyzer.ngrams.CTaggedWord;
import com.c24x7.util.string.CStringUtil;



public final class CTaggedWordLookup extends CLookup {

	private static final long serialVersionUID = -2130167092059932161L;

	protected static CTaggedWordLookup instance = null;

	
		/**
		 * <p>Initialize the lookup table in memory by loading
		 * the data from file.</p>
		 * @return true if the lookup table is properly loaded, false otherwise.
		 */
	public static void init(final String lookupType) throws InitException {
		if( instance == null) {
			instance = new CTaggedWordLookup();
			try {
				instance.load(lookupType);
			}
			catch( IOException e) {
				throw new InitException(e.toString());
			}
		}
	}
	
	
		/**
		 * <p>Retrieve the singleton lookup object.</p>
		 * @return unique, single instance of the Lookup class
		 */
	public static CTaggedWordLookup getInstance() {
		return instance;
	}

	
		/**
		 * <p>Retrieve the semantic record (from the lookup memory table) for a NGram with 
		 * a specific label and tag of type NNP/NNPS or NN/NNS.</p>
		 * @param nGramlabel label of the NGram to be searched for in the lookup memory map
		 * @param isNNP  flag that specifies whether the NGram contains a proper noun (NNP or NNPS tags).
		 * @return a Lookup record if the memory lookup table contains the label, null otherwise.
		 */
	public CLookupRecord getLookupRecord(String nGramlabel, boolean isNNP) {
		if(!isNNP ) {
			nGramlabel = CStringUtil.convertFirstCharToUpperCase(nGramlabel);
		}
		return (containsKey(nGramlabel) ? get(nGramlabel) : null);
	}
	
	
	
			/**
			 * <p>Retrieve the record from the lookup memory map/table for a single tagged word.</p>
			 * @param taggedWord taggedWord to be search in the lookup memory map
			 * @return a Lookup record if the memory lookup table contains the label, null otherwise.
			 */
	public CLookupRecord getLookupRecord(final CTaggedWord taggedWord) {
		String label = taggedWord.getWord();
		
		/*
		 * If the taggedWord tag is unknown (type NN, NNS, NNP or NNPS) then
		 * search for both upper case and lower case versions.
		 * 	If the tagged word is not a proper noun (type NNP or NNPS) then
		 * look for the word with a lower case 1st character..
		 */
		if( (taggedWord.isTagUnknown() && !containsKey(label)) || !taggedWord.isNNP()) {
			label = CStringUtil.convertFirstCharToUpperCase(label);
		}
		return get(label);	
	}



	
	private CTaggedWordLookup() { 
		super();
	}
}

// ------------------------------------  EOF --------------------------------------------------
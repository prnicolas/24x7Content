package com.c24x7.textanalyzer.ngrams;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.semantics.lookup.CLookupRecord;
import com.c24x7.semantics.lookup.CTaggedWordLookup;
import com.c24x7.textanalyzer.tfidf.CTfVector;
import com.c24x7.util.string.CStringUtil;



public class CTaggedNGram  {
	public static final int ANY 	 		= 0;
	public static final int CONTAINS_NOUN 	= 1;
	public static final int CONTAINS_NNP 	= 2;
	
	public static final int   MAX_NUM_SEMANTIC_RECORDS 		= 2;
		
	protected String 		_label 			= null;
	protected String		_stem			= null;
	protected int			_rank 			= 0;


	private int					_boosting		= CTaggedWord.NO_BOOST;
	private List<CTaggedWord[]> _taggedWordsList = null;
	private CTaggedWord[] 		_taggedWords 	= null;


		/**
		 * <p>Create a 1-Gram from a single tagged word as defined as a <token, tag> pair</p>
		 * @param taggedWord tagged word to define a 1-Gram
		 * @throws IllegalArgumentException if input is undefined.
		 */
	public CTaggedNGram(final CTaggedWord taggedWord) {
		super();
		if( taggedWord == null) {
			throw new IllegalArgumentException("Cannot create N-Gram from undefined tagged words");
		}
	
		_label = taggedWord.getWord();
		_rank = taggedWord.getRank();
		_boosting = taggedWord.getBoosting();
		_stem = taggedWord.getStem();
		_taggedWords = new CTaggedWord[] { taggedWord };
	}
	
	
		
	public CTaggedNGram(final List<CTaggedWord[]> taggedWordsList) {	
		boolean boosted = false;
		
		for(CTaggedWord[] taggedWords : taggedWordsList) {
			for( int k = 0; k < taggedWords.length; k++) {
				if( taggedWords[k].isBoosted()) {
					_boosting = taggedWords[k].getBoosting();
					boosted = true;
					break;
				}
			}
			if( boosted ) {
				break;
			}	
		}
		_taggedWordsList = taggedWordsList;
		_taggedWords = _taggedWordsList.get(_taggedWordsList.size()-1);
		setLabel();
	}
	
	
		/**
		 * <p>Create a N-Gram from a single non tagged word.</p>
		 * @param label non tagged word
		 */
	public CTaggedNGram(final String label) {
		_label = label;
	}
		
	public final int getBoosting() {
		return _boosting;
	}
	
	
	
		/**
		 * <p>Fast implementation to conversion of the label of this N-Gram to lower case.</p>
		 * @return lower case version of the label of this N-Gram
		 */
	public String toLowerCase() {
		return CStringUtil.convertFirstCharToLowerCase(_label);
	}
	
	
	
	public final String[] getTerms() {
		return _label.split(" ");
	}
	
	public final int getRank() {
		return _rank;
	}
	
	
			/**
			 * <p>Retrieve the label of this N-Gram.</p>
			 * @return label of the N-Gram
			 */
	public final String getLabel() {
		return _label;
	}


	
	
	private static int getLabels(final CTaggedWord[] taggedWords, String[] labels) {
		StringBuilder firstTermsBuf = new StringBuilder();
			
		int rank = ANY;
		int lastIndex = taggedWords.length-1;
		for(int j = 0; j < lastIndex; j++) {
			if( rank <  taggedWords[j].getRank()) {
				rank = taggedWords[j].getRank();
			}
			firstTermsBuf.append(taggedWords[j].getWord());
			firstTermsBuf.append(" ");
		}
		
			/*
			 * add the original N-Gram
			 */
		StringBuilder labelBuf = new StringBuilder(firstTermsBuf.toString());
		labelBuf.append(taggedWords[lastIndex].getWord());
		labels[0] = labelBuf.toString();
		
			/*
			 * add the stemmed N-Gram
			 */
		String stem = taggedWords[lastIndex].getStem();
		if( stem != null) {
			labelBuf = new StringBuilder(firstTermsBuf.toString());
			labelBuf.append(taggedWords[lastIndex].getStem());
			labels[1] = labelBuf.toString();
		}
			
		return rank;
	}
	

	private void setLabel() {		
		int lastIndex = _taggedWords.length-1;
		CTaggedWord lastTaggedWord = _taggedWords[lastIndex];

		StringBuilder buf = new StringBuilder();
		_rank = ANY;
		for(int j = 0; j < lastIndex; j++) {
			if( _rank < _taggedWords[j].getRank() ) {
				_rank = _taggedWords[j].getRank();
			}
			buf.append(_taggedWords[j].getWord());
			buf.append(" ");
		}
		
		if( _rank < lastTaggedWord.getRank() ) {
			_rank = lastTaggedWord.getRank();
		}
		buf.append(lastTaggedWord.getWord());
		_label = buf.toString();
		
		String lastTaggedWordStem = lastTaggedWord.getStem();
		if(lastTaggedWordStem != null) {
			buf = new StringBuilder();
			for(int j = 0; j < lastIndex; j++) {
				buf.append(_taggedWords[j].getWord());
				buf.append(" ");
			}
			buf.append(lastTaggedWordStem);
			_stem = buf.toString();
		}
		
	}

	
	
	private CSemanticNGram get1GramSemanticRecord(	final CTaggedWord taggedWord, 
													final Map<String, Object> exclusionMap)   {
		
		CSemanticNGram record = null;
			/*
			 * Try to find a semantic definition for this 1-Gram
			 */
		if( (exclusionMap == null || isValid(taggedWord, exclusionMap)) ) {
			String label = taggedWord.getWord();
			CLookupRecord lookupRecord = CTaggedWordLookup.getInstance().getLookupRecord(taggedWord);
			if( lookupRecord != null) {
				record = new CSemanticNGram(lookupRecord, label);
			}
			
				/*
				 * If the original word does not have a semantic definition, 
				 * try the stemmed version...
				 */
			else {
				label = taggedWord.getStem();
				if( label != null) {
					lookupRecord = CTaggedWordLookup.getInstance().getLookupRecord(label, taggedWord.isNNP());
					if( lookupRecord != null) {
						record = new CSemanticNGram(lookupRecord, label);
					}
				}
			}
		}
		return record;
	}
	
	
	
	private static boolean isValid(final CTaggedWord taggedWord, final Map<String, Object> exclusionMap)  {
		boolean notExcluded = !exclusionMap.containsKey(taggedWord.getWord().toLowerCase());
		
		if( notExcluded && 
			taggedWord.isNN() && 
			taggedWord.getStem() != null && 
			exclusionMap.containsKey(taggedWord.getStem().toLowerCase()) ) {
			
			notExcluded = false;
		}
		return notExcluded;
	}
	
		/**
		 * <p>Extract the Semantic N-Gram that correspond to a keyword. We assume that
		 * keyword as defined as proper nouns with first character as upper case.
		 * @param firstCharUpperCaseKeyword keyword for which to retrieve a semantic record
		 * @return a semantic record if it exists, null otherwise.
		 */
	
	public static CSemanticNGram getLookupRecord(final String firstCharUpperCaseKeyword) {
		CSemanticNGram record = null;
		
		CLookupRecord lookupRecord = CTaggedWordLookup.getInstance().getLookupRecord(firstCharUpperCaseKeyword, true);
		
		/*
		 * If the keyword with a first character upper case has no semantic
		 * definition, then try the lower case version of the keyword
		 */
		if( lookupRecord == null) {
			String labelLc = CStringUtil.convertFirstCharToLowerCase(firstCharUpperCaseKeyword);
			lookupRecord = CTaggedWordLookup.getInstance().getLookupRecord(labelLc, true);
		}
		
		if( lookupRecord != null) {
			record = new CSemanticNGram(lookupRecord, firstCharUpperCaseKeyword);
			String[] terms = firstCharUpperCaseKeyword.split(" ");
			record.is1Gram(terms.length ==1);
		}
	
		return record;
	}

	
			/**
			 * <p>Extracts the semantic record or definition of this N-Gram. The semantic
			 * match is performed for nouns only.</p>
			 * @return semantic record if one is found in the reference database, null otherwise
			 */
	public List<CSemanticNGram> extractSemRecord(final Map<String, Object> exclusionList) {
		List<CSemanticNGram> semanticRecords = null;
		
			/*
			 * We are considering only N-Grams which contains at 
			 * least one noun (type NN or NNP).
			 */
		if( _rank >= CONTAINS_NOUN) {
			
			CSemanticNGram semanticRecord = null;			
			
			
				/*
				 * Walk down the stack from the larger sub NGram to the smallest.
				 */
			if( _taggedWordsList != null) {
				final int lastTaggedWordsIndex = _taggedWordsList.size()-1;
				CTaggedWord[] subNGram = null;
		
				for( int k = lastTaggedWordsIndex; k >=0; k--) {
					subNGram = _taggedWordsList.get(k);				
					semanticRecord = getNGramSemanticRecord(subNGram, exclusionList);
					if( semanticRecord != null) {
						if( semanticRecords == null) {
							semanticRecords = new LinkedList<CSemanticNGram>();
						}
						semanticRecords.add(semanticRecord);
						
						/*
						 * Exit if we 
						 */
						if(semanticRecords.size() > MAX_NUM_SEMANTIC_RECORDS) {
							break;
						}
					}
				}
			}
			
			else {
				semanticRecord = get1GramSemanticRecord(_taggedWords[0], exclusionList);
				if( semanticRecord != null) {
					if( semanticRecords == null) {
						semanticRecords = new LinkedList<CSemanticNGram>();
					}
					semanticRecords.add(semanticRecord);
				}
			}
		}
		
		return semanticRecords;
	}

	
	public int findLowerCaseOccurrences(final String term) {
		int numOccurrences = 0;
		if( _label.toLowerCase().compareTo(term) == 0 ) {
			numOccurrences++;
		}
		
		if( _taggedWordsList != null) {
			for( CTaggedWord[] taggedWords : _taggedWordsList) {
				for( CTaggedWord taggedWord : taggedWords) {
					if(taggedWord.getWord().toLowerCase().compareTo(term) == 0) {
						numOccurrences++;
					}
				}
			}
		}
			
		return numOccurrences;
	}


		/**
		 * <p>Update the relative frequency of this N-Gram in the analyzed document. If
		 * the N-Gram has a different stem, then update the frequency of the stem as well.</p>
		 * @param tfVector relative frequency vector for the relevant N-Grams in the document.
		 */
	public void update(CTfVector tfVector) {
		CTaggedWord taggedWord =  _taggedWords[0];
		
		if( taggedWord.isTagUnknown()  && taggedWord.isNN())  {
			_label = CStringUtil.convertFirstCharToLowerCase(_label);
		}
		tfVector.put(_label, _stem);
	}

	
		/**
		 * <p>Textual representation of this N-Gram created for debugging purpose.</p>
		 * return string description of the components of the N-Gram.
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("NGram: ");
		buf.append(getLabel());
		buf.append("(");
		buf.append(_rank);
		buf.append(")");
		buf.append("=> ");
				/*
				 * load the sub N-Grams of specific size as 
				 * defined by the array of the tagged words it contains. 
				 */
	
		if( _taggedWordsList != null) {
			for( CTaggedWord[] taggedWords : _taggedWordsList) {		
				for( CTaggedWord taggedWord : taggedWords) {
					buf.append(taggedWord.getWord());
					buf.append(" ");
				}
				buf.append("/");
			}
			buf.append("\n");
		}
	
		return buf.toString();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static boolean isValid(final String label, final Map<String, Object> exclusionMap)  {
		return (exclusionMap == null ||
			   (exclusionMap != null &&  !exclusionMap.containsKey(label.toLowerCase())));
	}



	private CSemanticNGram getNGramSemanticRecord(	final CTaggedWord[] 		taggedWords, 
													final Map<String, Object> 	exclusionMap)  {
		CSemanticNGram record = null;
		
		/*
		 * If this is an 1-Gram, no need to create a new label.
		 */
		if( taggedWords.length == 1) {
			if( (exclusionMap == null || isValid(taggedWords[0], exclusionMap)) ) {
				record = get1GramSemanticRecord(taggedWords[0], exclusionMap);
				if( record != null) {
					record.is1Gram(true);
				}
			}
		}
		
		/*
		 * We collect all the semantic definition of N-Grams that
		 * share the same order in the stack.
		 */
		else {
			String[] labels = new String[2];
			int rank = getLabels(taggedWords, labels);
			
			for( int k = 0; k < 2; k++) {
					
				if(  labels[k] != null && isValid(labels[k], exclusionMap)) {
					CLookupRecord lookupRecord = CTaggedWordLookup.getInstance().getLookupRecord(labels[k], (rank == CTaggedNGram.CONTAINS_NNP));
					if( lookupRecord != null) {
						record = new CSemanticNGram(lookupRecord, labels[k]);
						if( k == 1) {
							record.setOriginal(labels[0]);
						}
						record.is1Gram(taggedWords.length==1);
						
						_label = labels[k];
						break;
					}
				}
			}
		}
		
		return record;
	}

	
}
// ------------------------- EOF -------------------------------------
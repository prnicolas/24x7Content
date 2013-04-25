// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.ngrams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.models.CTaxonomyInstance;
import com.c24x7.textanalyzer.ngrams.CTaggedWord.ETAG_TYPES;
import com.c24x7.textanalyzer.tfidf.CTfVector;
import com.c24x7.util.CEnv;



			/**
			 * <p>Class that manages the N-Gram properties (tag, relative frequency,
			 * and components. This class is responsible also to keep track of the 
			 * frequency of the words contained in all the N-Grams.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/22/2012
			 */
public final class CTaggedNGramsMap extends HashMap<String, CTaggedNGram> {
	private static final long serialVersionUID = 5253739756209843570L;
	
	private static final String[] validTags = {
		"NNPNNP", "NNPOANNP", "NNPOATHNNP"
	};

	
	private static boolean isValidTag(final String tag) {
		boolean isValid = false;
		for(String validTag : validTags) {
			if(tag.indexOf(validTag) != -1) {
				isValid = true;
				break;
			}
		}
		return isValid;
	}
	
	private CTfVector _tfVector = null;



		/**
		 * <p>Create an instance of an empty N-Grams map.</p>
		 */
	public CTaggedNGramsMap() {
		_tfVector = new CTfVector();
	}
	
	
	
		/**
		 * <p>Add a list of NGrams to this map and the compute the relative frequency
		 * of all the terms contained in each of the N-Gram.</p>
		 * 
		 * @param nGramsList List of N-Grams to be added to the map
		 * @param termsList List of terms for which the document frequency has to be computed.
		 */
	public void put(final List<CTaggedNGram> nGramsList, List<CTaggedWord> termsList) {
		if(termsList == null || nGramsList == null) {
			throw new IllegalArgumentException("Cannot add undefined N-Grams to the map");
		}
		
		CTaggedWord firstTaggedWord = termsList.get(0);
		if( nGramsList.size() > 1) {
			
			/*
			 * If this is the first tagged word in a sentence and
			 * the original tag is marked as undefined, estimate the tag..
			 */
			if( firstTaggedWord.isTagUnknown()) {
				StringBuilder nGramTagsBuf = new StringBuilder();
				for(CTaggedWord term : termsList) {
					nGramTagsBuf.append(term.getTagType().getTag());
				}
				
				/*
				 * if the first N-Gram in the sentence has a valid NNP 
				 * tag, then reset the tag of the first tagged word as
				 * NNP, otherwise reset it to NN by default and convert
				 * the tagged word into lower case characters.
				 */
				if(isValidTag(nGramTagsBuf.toString())) {
					firstTaggedWord.setTagType(ETAG_TYPES.NNP);
				}
				else {
					firstTaggedWord.setTagType(ETAG_TYPES.NN);
					firstTaggedWord.convertToLowerCase();
				}
			}
		}

			/*
			 * Update the vector of relative term (1-Gram) frequencies
			 */
		for(CTaggedWord term : termsList) {
			term.updateTfVector(_tfVector);
		}
		
			/*
			 * Add the N-Grams extracted from this current bag of words
			 * the overall document N-Grams map.
			 */
		for( CTaggedNGram nGram : nGramsList) {
			nGram.update(_tfVector);
			if( !containsKey(nGram.getLabel())) {
				super.put(nGram.getLabel(), nGram);
			}
		}
	}
	
	
		/**
		 * <p>Update the frequency of a 1-Gram.</p>
		 * @param label label of the 1-Gram
		 * @param is1Gram the frequency if updated if true, no action otherwise.
		 */
	public void updateFrequency(final String label, boolean is1Gram) {
		if(!is1Gram) {
			_tfVector.put(label);
		}
	}
	

	
	
		/**
		 * <p>Get the relative term frequency of a N-Gram within a specific document.</p>
		 * @param nGram nGram extracted from the document.
		 * @return number of occurrences of the N-Gram in the document.
		 */
	public int getFrequency(final String nGramLabel) {
		
		return (_tfVector.containsKey(nGramLabel) ? 
				_tfVector.get(nGramLabel).intValue() : 
				CEnv.UNINITIALIZED_INT);
	}
	
	
			/**
			 * <p>Normalize the frequency of each N-Gram in the map (or document) using
			 * the maximum frequency of any term in the document. The map (or list) of
			 * taxonomyInstance nouns (semantically valid N-Gram) for the document is updated.</p>
			 * @param taxonomyInstanceMap map of semantic N-Grams to be updated.
			 */
	public void normalize(Map<String, CTaxonomyInstance> taxonomyInstanceMap) {		
		double inverseMaxFrequency = 1.0/((double)_tfVector.getMaxFrequency());
			
		CTaxonomyInstance taxonomyInstance = null;
		double relF = CEnv.UNINITIALIZED_INT;
		
		for(String nGramLabel : taxonomyInstanceMap.keySet()) {
	
			taxonomyInstance = taxonomyInstanceMap.get(nGramLabel);
			String[] terms = nGramLabel.split(" ");
			
				/*
				 * If this NGram has more then one term (1+Gram) then extract the number 
				 * of occurrences for each term contained in the N-Gram and compute
				 * the maximum number of occurrences.
				 */
			relF = CEnv.UNINITIALIZED_FLOAT;
			if( terms != null && terms.length > 1 ) {
				int numNGramOccurrences = getFrequency(nGramLabel);
				
				int numTermOccurrences = 0;
				int maxNumTermOccurrences = 0;
				
				for(String term : terms) {
					numTermOccurrences = getFrequency(term);
					
					if( numTermOccurrences != CEnv.UNINITIALIZED_INT) {				
						if( maxNumTermOccurrences < numTermOccurrences) {
							maxNumTermOccurrences = numTermOccurrences;
						}
					}
				}	
				
					/*
					 * If the N-Gram has been already extracted from the document, 
					 * then compute its relative frequency. Statistics may be collected
					 * for training a classifier if the list of NGrams stats have been
					 * provided in the constructor.
					 */
				relF = taxonomyInstance.computeTf(numNGramOccurrences, maxNumTermOccurrences, inverseMaxFrequency);
			}	
			
				/*
				 * If this is a 1-Gram, then compute the relative frequency 
				 * by normalizing with the maximum frequency of any terms in the document.
				 */
			else {
				int nGramNumOccurrences = getFrequency(nGramLabel);
				if( nGramNumOccurrences == CEnv.UNINITIALIZED_INT) {
					nGramNumOccurrences = 1;
				}
				relF = taxonomyInstance.computeTf(nGramNumOccurrences, 0, inverseMaxFrequency);
			}
			
			if( relF != CEnv.UNINITIALIZED_DOUBLE) {
				taxonomyInstance.computeWeight(relF);
			}
		}
	}
	
	
	public String printNGrams() {
		StringBuilder buf = new StringBuilder("N-Grams Map:\n");
		for( CTaggedNGram nGram : values()) {
			if( nGram != null && nGram.getLabel() != null) {
				buf.append(nGram.toString());
				buf.append("\n");
			}
		}
		
		return buf.toString();
	}
	
	public String printTermsFrequencies() {
		StringBuilder buf = new StringBuilder("Terms Frequencies:\n");
		buf.append(_tfVector.toString());

		return buf.toString();
	}

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("N-Grams Map:\n");
		for( CTaggedNGram nGram : values()) {
			if( nGram != null && nGram.getLabel() != null) {
				buf.append(nGram.toString());
				buf.append("\n");
			}
		}
		buf.append("Terms Frequencies:\n");
		buf.append(_tfVector.toString());
		
		return buf.toString();
	}

}

// --------------------------------- EOF ---------------------------------------

// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer;

import java.util.Arrays;

import java.util.LinkedList;
import java.util.List;

import com.c24x7.models.tags.CTagsModel;
import com.c24x7.textanalyzer.ngrams.CTaggedNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedWord;
import com.c24x7.textanalyzer.ngrams.CTaggedWord.ETAG_TYPES;



		/**
		 * <p>Class that extracts and builds a list of semantically valid 
		 * taxonomyInstance nouns, from an existing set of N-GRAM which contains
		 * terms with JJ, NN* and NNP tags.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/16/2012
		 */
public final class CChunker {
	
	
		/**
		 * <p>Instantiates a List of N-Gram chunks from an existing
		 * list of N-Grams extracted from a document.</p>
		 * @param nGramsList list of original tagged N-Grams
		 */
	public CChunker() { 	}
	
	
		/**
		 * <p>Extract a stack of tagged N-Grams from a collection of tagged word according
		 * to the N-Gram tag model.</p>
		 * @param wordsCollection collection of tagged word composing the N-Gram
		 * @return A stack of Tagged N-Grams extracted from the collection of consecutive tagged words.
		 */
	
	public List<CTaggedNGram> extract(final List<CTaggedWord> termsList) {		
		List<CTaggedNGram> nGramsList = new LinkedList<CTaggedNGram>();
		
		List<CTaggedWord> filteredTaggedWordList = filterTaggedWords(termsList, nGramsList);
		
		if( filteredTaggedWordList.size() > 0) {
			chunk( filteredTaggedWordList, nGramsList);
		}

		return nGramsList;
	}
	
	
		

					// --------------------------
					// Private Supporting Methods
					// -------------------------
	
			/**
			 * Extract N-Grams from this collection of tagged words with N <= 4
			 * We prioritize the selection of N-Gram according to the number
			 * of terms (or tags) it contains in descending order. For instance,
			 * a 3-Gram has a higher priority than a 2-Gram in the semantic 
			 * description.
			 */
			
	private void chunk(final List<CTaggedWord> taggedWordsList, List<CTaggedNGram> nGramsList) {
		
			/*
			 * N-Grams with more than 4 terms are sliced into chunks
			 * of 4-Grams
			 */
		if( taggedWordsList.size() > 1) {
			
			CTaggedWord[] taggedWords = taggedWordsList.toArray(new CTaggedWord[0]);
			CTaggedWord[] taggedWordsCollection = null;
			int startingIndex = 0;
			CTaggedNGram nGram = null;
			
			for( int nGramSize = 1; nGramSize <= taggedWords.length; nGramSize++) {
				startingIndex = (nGramSize <= CTagsModel.MAX_NGRAM_SIZE) ? 0 : nGramSize - CTagsModel.MAX_NGRAM_SIZE;
				
				taggedWordsCollection = new CTaggedWord[nGramSize - startingIndex];
				for( int k = startingIndex, j = 0; k < nGramSize; k++, j++) {
					taggedWordsCollection[j] = taggedWords[k];
				}
				
				nGram = generateNGram(taggedWordsCollection);
				if(nGram != null) {
					nGramsList.add(nGram);
				}
			}
		}
		
			/*
			 * If this is a 1-Gram (single tagged word N-Gram)
			 */
		else if( taggedWordsList.size() == 1){
			CTaggedWord oneTaggedWord = taggedWordsList.get(0);
			CTaggedNGram nGram = new CTaggedNGram(oneTaggedWord);
			nGramsList.add(nGram);
		}
	}
			

	
	
					
			/**
			 * <p>Generate NGram from a list of CTaggerTerms which are terms
			 * with an associated tag of type Nouns, Adjective and Conjunction..</p>
			 * @param ngramsMap map of NGrams extracted from a document.
			 * @param termsList terms list (or list of tagged terms) from which the NGrams needs to be extracted.
			 */
	
	private CTaggedNGram generateNGram(final CTaggedWord[] taggedWordsCollection) {
		CTaggedNGram nGram = null;
		Integer tagRank = null;
			/*
			 * If this a 1-Gram (one term only)
			 */		
 		if(taggedWordsCollection.length == 1) {
 			CTaggedWord taggedWord = taggedWordsCollection[0];
 			
			tagRank = CTagsModel.getInstance().rank(taggedWord.getTag());
			if( tagRank != null) {
				nGram = new CTaggedNGram(taggedWord);
			}
		}
 			/*
 			 * Extracts N-Grams with more than one terms.
 			 */
		else {
			List<CTaggedWord[]> taggedWordsArrayList = new LinkedList<CTaggedWord[]>();
			int lastTagIndex = taggedWordsCollection.length-1;
			CTaggedWord[] taggedWordArray = null;
			String subTag = null;
			
			for( int k = lastTagIndex; k >= 0; k--) {
				
				subTag = generateSubTag(k, lastTagIndex, taggedWordsCollection);
				tagRank = CTagsModel.getInstance().rank(subTag, lastTagIndex-k);
				
				/*
				 * if the type of the tags of words contained in the Sub N-Gram 
				 * are defined as valid (N-Gram tag type classifier, CTagsModel)
				 * then add the sequence of tagged words into the containing N-Gram
				 */
				if( tagRank != null) {
					taggedWordArray = (k == 0) ? taggedWordsCollection : Arrays.copyOfRange(taggedWordsCollection, k, taggedWordsCollection.length);
					if( taggedWordArray != null && taggedWordArray.length > 0) {
						taggedWordsArrayList.add(taggedWordArray);
					}
				}
			}
			
				/*
				 * Create a N-Gram with the stack of tagged words arrays.
				 */
			if( taggedWordsArrayList.size() > 0) {
				nGram = new CTaggedNGram(taggedWordsArrayList);
			}
		}
 		
 		return nGram;
	}

	
	private static List<CTaggedWord> filterTaggedWords(final List<CTaggedWord> termsList, List<CTaggedNGram> nGramsList) {
		List<CTaggedWord> filteredTaggedWordList = new LinkedList<CTaggedWord>();

		if( termsList.size() == 1) {
			CTaggedWord taggedWord = termsList.get(0);
			if( CTagsModel.getInstance().isValid(taggedWord.getTag())) {
				nGramsList.add( new CTaggedNGram(taggedWord));
			}
		}
		
		else {
			int lastTaggedWordIndex =  termsList.size()-1;
			int index = 0;
	
			for(CTaggedWord taggedWord : termsList ) {
				if(index ==0) {
					if( taggedWord.isValidFirstWord()) {
						filteredTaggedWordList.add(taggedWord);
					}
				}
				else if( index == lastTaggedWordIndex) {
					if( taggedWord.isValidLastWord()) {
						filteredTaggedWordList.add(taggedWord);
					}
				}
				else {
					filteredTaggedWordList.add(taggedWord);
				}
				index++;
			}
		}
		
		return filteredTaggedWordList;
	}

	
		
	private static String generateSubTag(int firstTagIndex, int lastTagIndex, CTaggedWord[] taggedWords) {
		StringBuilder buf = new StringBuilder();
		ETAG_TYPES tagType = null;
		
		for( int k = firstTagIndex; k < lastTagIndex; k++) {
			tagType = taggedWords[k].getTagType();
			buf.append(tagType);
			buf.append(" ");
		}
		tagType = taggedWords[lastTagIndex].getTagType();
		buf.append(tagType);
		
		return buf.toString();
	}

}

// ---------------------------  EOF -------------------------------------------------
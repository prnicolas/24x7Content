//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.textanalyzer;


import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import com.c24x7.exception.InitException;
import com.c24x7.models.CTaxonomyInstance;
import com.c24x7.models.CTaxonomyInstancesMap;
import com.c24x7.models.CText;
import com.c24x7.models.ngrams.CNGramsModel;
import com.c24x7.models.tags.CTagsModel;

import com.c24x7.textanalyzer.filters.CCharsFilter;
import com.c24x7.textanalyzer.filters.AFilter;
import com.c24x7.textanalyzer.CChunker;

import com.c24x7.textanalyzer.ngrams.CTaggedNGramsMap;
import com.c24x7.textanalyzer.ngrams.CSemanticNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedWord;
import com.c24x7.textanalyzer.ngrams.CTaggedWord.ETAG_TYPES;
import com.c24x7.textanalyzer.CTagger;
import com.c24x7.textanalyzer.CTagger.NSentenceTokens;
import com.c24x7.util.string.CStringUtil;





			/**
			 * <p>Natural Language Processing class that <b>extract sentences, tokens,
			 * term-frequency vector and N-Grams from a document. Sentences, tokens and
			 * N-Grams are extracted using a generative classifier.<br>
			 * The N-Grams are generated through the following processing sequence<br>
			 * 1. extracts the sentences from content<br> 
			 * 2. extracts the tokens (or words) from each sentence<br>
			 * 3. extracts the tag for each token<br>
			 * 4. filter out words according to their tags (NNP, NN, JJ, OA....)
			 * 5. aggregate terms in chunks using probability distribution for sequence of tags<br>
			 * 6. extract semantic relevant N-Gram against Wikipedia reference database
			 * 7. generate the normalized term frequency vector for 1-Gram and N-Grams<br>
			 * </p>
			 * @author Patrick Nicolas
			 * @date 04/21/2011
			 */

public final class CTaggedNGramsExtractor {
		private static Map<String, Object> _isaAttributesMap = null;
	
	
		/**
		 * <p>Initialize the key parameters of N-Gram extractors during the
		 * launch of the application.</p> 
		 * @return true if the initialization is successful, false otherwise.
		 */
	public static void init() throws InitException {
		_isaAttributesMap = new HashMap<String, Object>();
		_isaAttributesMap.put("is", null);
		_isaAttributesMap.put("are", null);
		_isaAttributesMap.put("was", null);
		
		CTagger.init();
		CTaggedWord.init();
		CTagsModel.init();
		CNGramsModel.init();
	}
	
	
	
		/**
		 * <p>Comparator for Integer that excludes equal terms. Contrary to the
		 * default comparator used in TreeSet and TreeMap this comparator
		 * return 1 for greater or equals and -1 for less operators.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 01/15/2012
		 */
	protected static class NCompositeComparator implements Comparator<String> {
		
		Map<String, CTaxonomyInstance> _map = null;
		
		protected NCompositeComparator(Map<String, CTaxonomyInstance> map) {
			super();
			_map = map;
		}
		
		/**
		 * <p>compare two Composite using their computed, normalized weights.
		 * This method is called for the ranking of the key taxonomyInstance nouns 
		 * in a document.</p>
		 * @param key1 content of the taxonomyInstance noun to compare from
		 * @param key2 content of the taxonomyInstance noun to compare to
		 */
		
		@Override
		public int compare(String key1, String key2) {
			CTaxonomyInstance c1 = _map.get(key1);
			CTaxonomyInstance c2 = _map.get(key2);
			return (c1.getWeight() < c2.getWeight()) ? 1 : -1;
		}
	}
	
	
	private AFilter 			_charValidationRule = null;
	private CTaggedNGramsMap			_nGramsMap 			= null;
	private boolean				_luhnWeight			= false;
	private	int 				_luhnPosition 		= CTaggedWord.NO_BOOST;
	private Map<String, Object>	_excludedNGramsMap	= null;
	
	
		/**
		 * <p>Default constructor for the extraction of N-Grams from document.</p>
		 */
	public CTaggedNGramsExtractor() {	
		this(false, false);
	}
	
		/**
		 * <p>User-defined constructor for the extraction of N-Grams from document.</p>
		 * @param luhnWeight apply the Luhn Weight on the first sentence of the document if true
		 * @param exclusion allows the exclusion of title as N-Gram is true.
		 */
	public CTaggedNGramsExtractor(boolean luhnWeight, boolean exclusion) {	
		_charValidationRule = new CCharsFilter();
		_luhnWeight = luhnWeight;
		
		if( exclusion ) {
			_excludedNGramsMap = new HashMap<String, Object>();
		}
	}
	
	
		/**
		 * <p>Access the map of N-Grams generated for this document.This method should
		 * be used for debugging purpose only.</p>
		 * @return map of 1-Gram and N-Gram contained in a document.
		 */
	public final CTaggedNGramsMap getNGramsMap() {
		return _nGramsMap;
	}
	
	
		/**
		 * <p>Extract the most relevant N-Grams of a document document
		 * and a list of keywords extracted from additional sources. This 
		 * method is invoked during the training of the Taxonomy classifier. 
		 * The N-Grams are defined as a sequence of terms of tags NNP,
		 * NN(s), JJ and the terms 'the', 'of' and 'and'.</p>
		 * @param document document document to be analyzed..
		 * @return true if successful, false otherwise.
		 */
	public boolean extract(CText document, final String inputText) {
		return extract(document, inputText, null);
	}
	
		

	
		/**
		 * <p>Extract the most relevant N-Grams of a document document
		 * and a list of keywords extracted from additional sources. This 
		 * method is invoked during the training of the Taxonomy classifier. 
		 * The N-Grams are defined as a sequence of terms of tags NNP,
		 * NN(s), JJ and the terms 'the', 'of' and 'and'.</p>
		 * @param document document document to be analyzed..
		 * @return true if successful, false otherwise.
		 */
	public boolean extract(CText document, final String inputText, final List<String> keywordsList) {	
		if( document == null ) {
			throw new IllegalArgumentException("Cannot N-Grams from undefined document");
		}

		boolean success = false;
	
			/*
			 * Reset the sentence (or Luhn) counter to 0 if Luhn algorithm for
			 * computing relative frequency of terms according to their 
			 * position, is used.
			 */
		_nGramsMap = new CTaggedNGramsMap();
		

			/*
			 * Create N-Grams using a probabilistic distribution of 
			 * tags for Wikipedia entries
			 */
		createNGrams(document, inputText);				
			/*
			 * Extract semantic taxonomyInstance nouns
			 */
		
		CTaxonomyInstancesMap taxonomyInstancesMap = new CTaxonomyInstancesMap();
		createComposites(taxonomyInstancesMap);
		
		if(keywordsList != null) {
			updateComposites(taxonomyInstancesMap, keywordsList);
		}
		
			/*
			 * Normalize term frequencies and taxonomy classes weights
			 */
		_nGramsMap.normalize(taxonomyInstancesMap);
		if( taxonomyInstancesMap != null ) {
			success = prioritizeComposites(document, taxonomyInstancesMap);
		}
		
		final String title = document.getTitle();
		if(title != null) {
			String resolvedTitle = resolveTitle(title);
			document.setTitle(resolvedTitle);
		}
		
		return success;
	}
	
			

						// ----------------------------
						// Supporting Private Methods
						// ---------------------------

	private void createNGrams(final CText document, final String inputText) { 
		CChunker chunksList = new CChunker();
		CTagger tags = new CTagger();
		
		/*
		 * Step 1: Extracts the tokens from the content
		 */
		NSentenceTokens[] sentencesTokens = tags.extractTokens(inputText);
		if(sentencesTokens != null && sentencesTokens.length > 0) {
				
			/*
			 * Step 2: extracts the NGrams from the tokens and possibly the
			 * terms frequency vector.
			 */
			for(int sentenceIndex = 0; sentenceIndex < sentencesTokens.length; sentenceIndex++) {
				extractSentenceNGrams(chunksList, sentencesTokens[sentenceIndex].getTokens(), sentenceIndex);
			}
		}
		
		if( _excludedNGramsMap != null) {
			excludeNGram(document.getTitle());
		}
	}	

	
	private void createComposites(CTaxonomyInstancesMap taxonomyInstancesMap) {
		List<CSemanticNGram> semRecordsList = null;
		
			/*
			 * Generate a map of taxonomyInstance nouns from the
			 * list of N-Grams.
			 */
		for(CTaggedNGram nGram : _nGramsMap.values()) {
			
			if(isValidNGram(nGram)) {
				/*
				 * Make sure the NGram is not one to be excluded
				 * during the training of the taxonomy model.
				 */
				semRecordsList = nGram.extractSemRecord(_excludedNGramsMap);	
					
				if( semRecordsList != null) {
					for( CSemanticNGram semRecord : semRecordsList) {
						final String label = taxonomyInstancesMap.addNGram(semRecord, nGram);
						_nGramsMap.updateFrequency(label, semRecord.is1Gram());
					}
				}
			}
		}
	}
		/*
		 * In case a list of keywords is provided along with the label and abstract
		 * then add those keywords as simple N-Grams.
		 */
	private void updateComposites(CTaxonomyInstancesMap taxonomyInstancesMap, 
								  final List<String> keywordsList) {
		if( keywordsList != null) {
			CSemanticNGram semRecord = null;
			
			for( String keyword : keywordsList) {
				semRecord = CTaggedNGram.getLookupRecord(keyword);
				
				if( semRecord != null) {
					final String label =  taxonomyInstancesMap.addInstance(semRecord, null);
					_nGramsMap.updateFrequency(label, semRecord.is1Gram());
				}
			}
		}
	}
	
	

			/**
			 * <p>Extracts multiple NGram from a bag of words within a sentence. Those bag of words 
			 * have terms which tags are of type JJ, NN, NNS, NNP, or are 'and, 'of'. The bag of words
			 * are then filtered to make sure  that the first term in the NGram is not a conjunction or 
			 * a possessive character.The last term cannot be an Adjective, a Conjunction or a possessive character.
		     * The NGram are extracted according to the classifier NGramClassifier.
			 * </p>
			 * @param taxonomyInstanceMap table of semantically valid taxonomyInstance nouns to be extracted from the document
			 * @param tokens  list of tokens extracted from the document
			 */
	private void extractSentenceNGrams(	CChunker 		chunksList, 
										final String[] 	tokens, 
										int 			sentenceIndex) {
		
				/*
				 * Use the Maximum-entropy model Tagger to extract the
				 * tags from each tokens.
				 */
		CTagger tagsProcessor = new CTagger();
		String[] tags = tagsProcessor.extractTags(tokens);
		
		String qualifiedWord = null;
			
		if( tags != null && tags.length > 1) {
			List<CTaggedWord> termsList = null;
			
			ETAG_TYPES tagType = null;
			CTaggedWord taggedTerm = null;
	
			_luhnPosition = CTaggedWord.NO_BOOST;
			
			/*
			 * extracts bag of words defined by contiguous terms 
			 * of predefined tags (NN, NNS, NNP, IN, JJ) which 
			 * define nouns and adverbs.
			 */
			for( int index = 0; index < tags.length; index++) {	
				tagType = null;
					
				qualifiedWord = _charValidationRule.qualify(tokens[index]);
				if( qualifiedWord != null ) {
					extractBoostingFactor(qualifiedWord, sentenceIndex);

					/*
					 * Filters out tokens with non relevant tags such JJ, NN, NNP...
					 */
					tagType = ETAG_TYPES.getTagType(tags[index], qualifiedWord);
					if( tagType != null) {
						if( termsList == null) {
							termsList = new LinkedList<CTaggedWord>();
						}
						taggedTerm = createTaggedWord(qualifiedWord, tagType);
				
	
						/*
						 * We need to valid if the first term in a sentence, with 
						 * a first character in upper case) is indeed of type NNP
						 * or mark it as 'dirty'. Generate stems for plurals nouns
						 * if available. 
						 */
						String stem = null;
						if( tagType.isNoun() ) {
							if( index == 0) {
								taggedTerm.resetTagType();
							}
							stem = _charValidationRule.stem();
							if( stem != null) {
								taggedTerm.setStem(stem);
							}
						}
						
						termsList.add(taggedTerm);	
					}
							
					/*
					 * The list or map of terms composing the N-Gram or Chunk
					 * of words has to be added to the list for evaluation.
					 */
					else if (termsList != null) {
						List<CTaggedNGram> nGramsList = chunksList.extract(termsList);
						if(nGramsList.size() > 0) {
							_nGramsMap.put(nGramsList, termsList);
						}
						if( _luhnPosition == CTaggedWord.FIRST_SUBJECT_ATTRIBUTE_BOOST) {
							_luhnPosition = CTaggedWord.FIRST_SENTENCE_BOOST;
						}

						termsList = null;
					}
				}
			}
					/*
					 * If the sentence ends with a N-Gram, add it to the 
					 * list of chunk of words for evaluation.
					 */
			if (termsList != null) {
				List<CTaggedNGram> nGramsList = chunksList.extract(termsList);
				if(nGramsList.size() > 0) {
					_nGramsMap.put(nGramsList, termsList);
				}	
				if( _luhnPosition == CTaggedWord.FIRST_SUBJECT_ATTRIBUTE_BOOST) {
					_luhnPosition = CTaggedWord.FIRST_SENTENCE_BOOST;
				}
			}
		}
	}

	
	private CTaggedWord createTaggedWord(final String word, ETAG_TYPES eTag) {
		CTaggedWord taggedTerm = new CTaggedWord(word, eTag);
		if( _luhnWeight ) {
			taggedTerm.setBoosting(_luhnPosition);
		}
		
		return taggedTerm;
	}
	
	
	
	
	private void extractBoostingFactor(final String qualifiedWord, int index) {
		if(index == 0) {
			if(_luhnPosition <= CTaggedWord.FIRST_SENTENCE_BOOST) {
				if( _isaAttributesMap.containsKey(qualifiedWord) ) {
					_luhnPosition = CTaggedWord.FIRST_SUBJECT_ATTRIBUTE_BOOST; 
				}
				else {
					_luhnPosition = CTaggedWord.FIRST_SENTENCE_BOOST;
				}
			}
		}
		else {
			_luhnPosition = CTaggedWord.NO_BOOST;
		}
	}
	

	

		/**
		 * <p>Update the component of the document document after normalization of the
		 * taxonomyInstance nouns generated from N-Grams.. </p>
		 * 
		 * @param document  document to be updated
		 * @param taxonomyInstanceMap map of normalized taxonomyInstance nouns (semantically valid N-Gram) 
		 * extracted from the document
		 * @return true if the document is updated, false otherwise
		 */
	private boolean prioritizeComposites(CText document, CTaxonomyInstancesMap taxonomyInstanceMap) {
		boolean success = false;
				
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<String, CTaxonomyInstance> taxonomyInstanceTreeMap = new TreeMap(new CTaggedNGramsExtractor.NCompositeComparator(taxonomyInstanceMap));

		CTaxonomyInstance thisComposite = null;
		for( String taxonomyInstanceLabel : taxonomyInstanceMap.keySet()) {
			
			thisComposite = taxonomyInstanceMap.get(taxonomyInstanceLabel);
			if( thisComposite != null) {
				taxonomyInstanceTreeMap.put(taxonomyInstanceLabel, thisComposite);
			}
		}

		success = (taxonomyInstanceTreeMap != null && taxonomyInstanceTreeMap.size() > 0);
		if( success ) {			
	//		document.setObjectsMap(taxonomyInstanceTreeMap);
			document.setState(CText.E_STATES.NGRAMS);
		}
		
		return success;
	}

	
	
	private void excludeNGram(final String excludedNGram) {
		if(_excludedNGramsMap != null) {
			_excludedNGramsMap.put(excludedNGram.toLowerCase(), null);
		}
	}

	
	private String resolveTitle(final String title) {
		if( title == null) {
			throw new IllegalArgumentException("Title is undefined for N-Grams");
		}
		
		String resolvedTitle = null;
		if(_nGramsMap.containsKey(title)) {
			resolvedTitle = title;
		}
		else {
			resolvedTitle = CStringUtil.convertFirstCharToLowerCase(title);
			if( !_nGramsMap.containsKey(resolvedTitle)) {
				resolvedTitle = title;
			}
		}
		
		return resolvedTitle;
	}
	
	
	private boolean isValidNGram(final CTaggedNGram nGram) {
		return (nGram != null && _nGramsMap.getFrequency(nGram.getLabel()) > 0);
	}
}


// ---------------------- eof --------------------------------
//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.textanalyzer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import com.c24x7.exception.InitException;
import com.c24x7.models.CTaxonomyObject;
import com.c24x7.models.CTaxonomyObjectsMap;
import com.c24x7.models.CText;
import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.semantics.lookup.CLookupRecord;
import com.c24x7.textanalyzer.filters.CCharsFilter;
import com.c24x7.textanalyzer.filters.AFilter;
import com.c24x7.textanalyzer.stemmer.CPluralStemmer;
import com.c24x7.textanalyzer.tfidf.CTfVector;
import com.c24x7.textanalyzer.CWords;
import com.c24x7.textanalyzer.CWords.NSentenceTokens;
import com.c24x7.util.CEnv;






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

public final class CNGramsExtractor {
				
	private static final int NGRAM_SPAN = 3;
	
	private static Map<String, Object> 		conjunctionTokensMap = null;
	private static Map<Character, Object> 	specialCharsMap = null;
	
		/**
		 * <p>Initialize the key parameters of N-Gram extractors during the
		 * launch of the application.</p> 
		 * @return true if the initialization is successful, false otherwise.
		 */
	public static void init() throws InitException {
		conjunctionTokensMap = new HashMap<String, Object>();
		conjunctionTokensMap.put("of", null);
		conjunctionTokensMap.put("the", null);
		conjunctionTokensMap.put("and", null);
		conjunctionTokensMap.put("in", null);
		conjunctionTokensMap.put("The", null);
		
		specialCharsMap = new HashMap<Character, Object>();
		specialCharsMap.put(',', null);
		specialCharsMap.put(':', null);
		specialCharsMap.put(';', null);

		CWords.init();
	}
	
	
	private class NGram {
		private String _label = null;
		private String _stem = null;
		
		public NGram(final String label) {
			_label = label;
		}
		
		public void addStem(final String stem) {
			_stem = stem;
		}
		
		public String getStem() {
			return _stem;
		}
		
		public String getLabel() {
			return _label;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			if(_stem != null) {
				buf.append("/");
				buf.append(_stem);
			}
			
			return buf.toString();
		}
	} 
	
		/**
		 * <p>Comparator for Integer that excludes equal terms. Contrary to the
		 * default comparator used in TreeSet and TreeMap this comparator
		 * return 1 for greater or equals and -1 for less operators.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 01/15/2012
		 */
	protected static class NTaxonomyObjectComparator implements Comparator<String> {
		
		Map<String, CTaxonomyObject> _map = null;
		protected NTaxonomyObjectComparator(Map<String, CTaxonomyObject> map) {
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
			return (_map.get(key1).getWeight() < _map.get(key2).getWeight()) ? 1 : -1;
		}
	}
	
	
	private int			_maxNumTaxonomyObjects	= -1;
	private AFilter 	_charValidationRule 	= null;
	private String		_title 					= null;
	private CTfVector	_tfVector				= null;
	
	
		/**
		 * <p>Default constructor for the extraction of N-Grams from document.</p>
		 */
	public CNGramsExtractor() {
		this(-1);
	}
	
		/**
		 * <p>Default constructor for the extraction of N-Grams from document.</p>
		 */
	public CNGramsExtractor(int maxNumTaxonomyObjects) {	
		_charValidationRule = new CCharsFilter();
		_maxNumTaxonomyObjects = maxNumTaxonomyObjects;
	}
	
	
	public void setTitle(final String title) {
		_title = title;
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
		if( document == null ) {
			throw new IllegalArgumentException("Cannot N-Grams from undefined document");
		}

		_tfVector = new CTfVector();


			/*
			 * Create N-Grams using a probabilistic distribution of 
			 * tags for Wikipedia entries
			 */
		CTaxonomyObjectsMap taxonomyObjectsMap = new CTaxonomyObjectsMap();
		
		createNGrams(document, taxonomyObjectsMap, inputText);				
			/*
			 * Extract semantic taxonomyInstance nouns
			 */
		createTaxonomyObjects(document, taxonomyObjectsMap);

			/*
			 * Normalize term frequencies and taxonomy classes weights
			 */
		orderTaxonomyObjects(document, taxonomyObjectsMap);

		return true;
	}


			

						// ----------------------------
						// Supporting Private Methods
						// ---------------------------


	private void createNGrams(	final CText document, 
								CTaxonomyObjectsMap taxonomyObjectsMap, 
								final String inputText) { 
		
		/*
		 * Step 1: Extracts the tokens from the content
		 */

		CWords words = new CWords();
		NSentenceTokens[] sentencesTokens = words.extractTokens(document, inputText);
		if(sentencesTokens != null && sentencesTokens.length > 0) {
			

			/*
			 * Step 2: extracts the NGrams from the tokens and possibly the
			 * terms frequency vector.
			 */
			final int numSentences = (_maxNumTaxonomyObjects == -1) ?  sentencesTokens.length : 1;
			
			for(int sentenceIndex = 0; sentenceIndex < numSentences; sentenceIndex++) {
				extractSentenceNGrams(	taxonomyObjectsMap, 
										sentencesTokens[sentenceIndex].getTokens(), 
										sentenceIndex);
			}
		}
	}	

	
	public void createTaxonomyObjects(CText document, final  CTaxonomyObjectsMap  taxonomyObjectsMap) {

		double inverseMaxFrequency = 1.0/((double)taxonomyObjectsMap.getMaxFrequency());
			
		CTaxonomyObject taxonomyObject = null;
		double relF = CEnv.UNINITIALIZED_INT;
			
		for(String objectLabel : taxonomyObjectsMap.keySet()) {
			taxonomyObject = taxonomyObjectsMap.get(objectLabel);
			String[] terms = objectLabel.split(" ");
				
				/*
				 * If this NGram has more then one term (1+Gram) then extract the number 
				 * of occurrences for each term contained in the N-Gram and compute
				 * the maximum number of occurrences.
				 */
			relF = CEnv.UNINITIALIZED_FLOAT;
			if( terms != null && terms.length > 1 ) {
				int numNGramOccurrences = taxonomyObjectsMap.get(objectLabel).getCount();
				
				int numTermOccurrences = 0,
					maxNumTermOccurrences = 0;
				
				for(String term : terms) {
					if( taxonomyObjectsMap.containsKey(term) && _tfVector.containsKey(term) ) {
						numTermOccurrences = _tfVector.get(term);
						
						if( numTermOccurrences != CEnv.UNINITIALIZED_INT) {				
							if( maxNumTermOccurrences < numTermOccurrences) {
								maxNumTermOccurrences = numTermOccurrences;
							}
						}
					}
				}	
				
					/*
					 * If the N-Gram has been already extracted from the document, 
					 * then compute its relative frequency. Statistics may be collected
					 * for training a classifier if the list of NGrams stats have been
					 * provided in the constructor.
					 */
				relF = taxonomyObject.computeTf(numNGramOccurrences, maxNumTermOccurrences, inverseMaxFrequency);
			}	
			
				/*
				 * If this is a 1-Gram, then compute the relative frequency 
				 * by normalizing with the maximum frequency of any terms in the document.
				 */
			else {
				int nGramNumOccurrences = taxonomyObjectsMap.get(objectLabel).getCount();
				if( nGramNumOccurrences == CEnv.UNINITIALIZED_INT) {
					nGramNumOccurrences = 1;
				}
				relF = taxonomyObject.computeTf(nGramNumOccurrences, 0, inverseMaxFrequency);
			}
			
			if( relF != CEnv.UNINITIALIZED_DOUBLE) {
				taxonomyObject.computeWeight(relF);
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
	private void extractSentenceNGrams(CTaxonomyObjectsMap 	taxonomyObjectsMap, 
									   final String[] 		tokens, 
									   int 					sentenceIndex) {
		
		String 	qualifiedWord 		 = null,
				stemmedQualifiedWord = null;
		
	
		if( tokens != null && tokens.length > 1) {

			int lastValidIndex = 0;
			List<NGram> nGramsList = null;
			String[] tokensSequence = null;
			
			for( int index = 0; index < tokens.length; index++) {	
				qualifiedWord = _charValidationRule.qualify(tokens[index]);
				
				if( qualifiedWord != null ) {
					tokens[index] = qualifiedWord;
					stemmedQualifiedWord  = extractQualifiedWordStem(qualifiedWord);
						
					final int firstTokenIndex = lastValidIndex < index -NGRAM_SPAN ? index -NGRAM_SPAN : lastValidIndex;
					tokensSequence = new String[index - firstTokenIndex+1];
					
					for(int k = 0; k < tokensSequence.length; k++) {
						tokensSequence[k] = tokens[firstTokenIndex+k];
					}
					
					/*
					 * Extract and validate all the (sub) N-Grams from this N-Gram
					 * by combining all the different tokens 'tokensSequence' to the N-Gram 
					 */
					nGramsList = extractNGrams(tokensSequence, stemmedQualifiedWord);
					/*
					 * Extract the semantic records from this list of N-Grams
					 * by using a semantic data source such as Freebase or Wikipedia.
					 */		
					if(nGramsList != null && nGramsList.size() > 0) {
						extractTaxonomyObject(taxonomyObjectsMap, nGramsList, sentenceIndex);
					}
				}
				
				else {
					lastValidIndex = index+1;
				}
			}
		}
	}
	
	
	
	private List<NGram> extractNGrams(final String[] tokens, final String lastTokenStem) {
		List<NGram> nGramsList = null;
	
		final int lastTokenIndex = tokens.length-1;
		String lastToken = tokens[lastTokenIndex];
		
			/*
			 * If the last term is not a conjunction as 
			 * semantically valid NGram does not start or end
			 * with conjunctions
			 */
		String strippedLastToken = validateLastToken(lastToken);
		
		if( strippedLastToken != null)  {
			nGramsList = new LinkedList<NGram>();
			
			StringBuilder[] buf = new StringBuilder[lastTokenIndex];
			for( int k = 0; k < lastTokenIndex; k++) {
				buf[k] = new StringBuilder();
			}
			
			/*
			 * Make sure that any of the NGrams do not start or
			 * end with a conjunction.
			 */
			boolean[] rejectedNGramIndices = new boolean[lastTokenIndex];
			
			for( int k = 0; k < lastTokenIndex; k++) {
				for( int j = 0; j <= k; j++) {
					if(j == k && conjunctionTokensMap.containsKey(tokens[k])) {
						rejectedNGramIndices[j] = true;
					}
					buf[j].append(tokens[k]);
					buf[j].append(" ");
				}
			}
			boolean variantFlag = (lastToken.length() > strippedLastToken.length());
			
			String newNGramLabel = null;
			if( lastTokenStem != null) {
					
				String firstPartofNGram = null;
				StringBuilder variantBuf = null;
				NGram nGram = null;
					
				for( int j = 0; j < buf.length; j++) {
					if( !rejectedNGramIndices[j]) {
						firstPartofNGram = buf[j].toString();
							
							/*
							 * Create a N-Gram using the original last token
							 */
						variantBuf = new StringBuilder(firstPartofNGram);
						variantBuf.append(lastToken);
						nGram = new NGram(variantBuf.toString());
					
							
							/*
							 * Create a N-Gram using the stem version of 
							 * the last token in the N-Gram
							 */
						variantBuf = new StringBuilder(firstPartofNGram);
						variantBuf.append(lastTokenStem);
						nGram.addStem(variantBuf.toString());
						
						nGramsList.add(nGram);
							
						if( variantFlag ) {
							variantBuf = new StringBuilder(firstPartofNGram);
							variantBuf.append(strippedLastToken);
							nGramsList.add(new NGram(variantBuf.toString()));	
						}
					}
				}
				
				nGram = new NGram(lastToken);
				nGram.addStem(lastTokenStem);
				nGramsList.add(nGram);
				
				if( variantFlag ) {
					nGramsList.add(new NGram(strippedLastToken));
				}
			}
							
			else {
					
				/*
				 * if the last token is not terminated with a special
				 * character, then create the NGrams
				 */
				if( !variantFlag) {
					for( int j = 0; j < buf.length; j++) {
						if( !rejectedNGramIndices[j]) {
							buf[j].append(lastToken);
							newNGramLabel = buf[j].toString();   
							nGramsList.add(new NGram(newNGramLabel));
						}
					}
					/*
					 * Add the last token in the list of N-Gram (1-Gram)
					 */
					nGramsList.add(new NGram(lastToken));
				}
				
				/*
				 * otherwise both variants of the last token, with and without the
				 * terminating special character are used to create the NGrams.
				 */
				else {
					String firstPartofNGram = null;
					StringBuilder variantBuf = null;
					
					for( int j = 0; j < buf.length; j++) {
						if( !rejectedNGramIndices[j]) {
							firstPartofNGram = buf[j].toString();
							
							/*
							 * Create a N-Gram using the original last token
							 */
							variantBuf = new StringBuilder(firstPartofNGram);
							variantBuf.append(lastToken);
							nGramsList.add(new NGram(variantBuf.toString()));
						
							/*
							 * Create a N-Gram using the stripped last token
							 */
							variantBuf = new StringBuilder(firstPartofNGram);
							variantBuf.append(strippedLastToken);
							nGramsList.add(new NGram(variantBuf.toString()));
						}
					}
					
					/*
					 * Finally add the last token and its stripped version
					 */
					nGramsList.add(new NGram(lastToken));
					nGramsList.add(new NGram(strippedLastToken));

				}
			}
		}
		
		return nGramsList;
	}
	

	private String extractQualifiedWordStem(final String qualifiedWord) {
		String stemmedQualifiedWord = null;
		
		if( !conjunctionTokensMap.containsKey(qualifiedWord) ) {
			final int qualifiedWordLastIndex = qualifiedWord.length() -1;
			
			String strippedQualifiedWord = 	specialCharsMap.containsKey(qualifiedWord.charAt(qualifiedWordLastIndex)) ?
											qualifiedWord.substring(0, qualifiedWordLastIndex) :
											qualifiedWord;
				
			stemmedQualifiedWord = CPluralStemmer.getInstance().stem(strippedQualifiedWord);
			_tfVector.put(strippedQualifiedWord, stemmedQualifiedWord);
		}

		return stemmedQualifiedWord;
	}


	private String validateLastToken(final String lastToken)  {
		String validLastToken = null;
		
		if( !conjunctionTokensMap.containsKey(lastToken) ) {
			final int tokenLastIndex = lastToken.length()-1;
			validLastToken = specialCharsMap.containsKey(lastToken.charAt(tokenLastIndex)) ?
							lastToken.substring(0, tokenLastIndex) :
							lastToken;
		}
		
		return validLastToken;
	}

	
	
	

	
		/**
		 * <p>Match a semantic definition for each N-Gram, extracted from the 
		 * current sentence. Once a semantic record with taxonomy lineage (WordNet hypernyms) 
		 * is found, it is added to the  taxonomy graph or map.</p>
		 * 
		 * @param semanticRecordsMap map or graph of the semantic records associated with the current sentence
		 * @param nGramsList list of N-Grams previously extracted from the current sentence
		 * @param sentenceIndex index or order of the sentence.
		 */
	private void extractTaxonomyObject(	CTaxonomyObjectsMap	taxonomyObjectsMap,
								 		final List<NGram>	nGramsList,
								 		int 				sentenceIndex) {
							
	
		if( _maxNumTaxonomyObjects == -1 || taxonomyObjectsMap.size() < _maxNumTaxonomyObjects) {
			
			/*
			 * Walks through the list of N-Grams extracted from the text.
			 */
			for(NGram nGram : nGramsList ) {
				CLookupRecord lookupRecord = CLookup.getInstance().getLookupRecord(nGram.getLabel(), nGram.getStem());		
				
				/*
				 * Found a semantic record corresponding to this N-Gram
				 */
				if( lookupRecord != null) {
					taxonomyObjectsMap.put(lookupRecord, sentenceIndex);
				}
			}
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
	private boolean orderTaxonomyObjects(CText document, CTaxonomyObjectsMap taxonomyObjectsMap) {
		boolean success = false;
				
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<String, CTaxonomyObject> taxonomyObjectsTreeMap = new TreeMap(new CNGramsExtractor.NTaxonomyObjectComparator(taxonomyObjectsMap));

		CTaxonomyObject thisObject = null;
		for( String taxonomyInstanceLabel : taxonomyObjectsMap.keySet()) {
			
			thisObject = taxonomyObjectsMap.get(taxonomyInstanceLabel);
			if( thisObject != null) {
				taxonomyObjectsTreeMap.put(taxonomyInstanceLabel, thisObject);
			}
		}

		success = (taxonomyObjectsTreeMap != null && taxonomyObjectsTreeMap.size() > 0);
		if( success ) {			
			document.setObjectsMap(taxonomyObjectsTreeMap);
			document.setState(CText.E_STATES.NGRAMS);
		}
		
		return success;
	}
	
}


// ---------------------- eof --------------------------------
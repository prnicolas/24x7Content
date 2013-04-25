// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.ngrams;

import com.c24x7.models.CText;
import com.c24x7.models.ngrams.CNGramsStats;
import com.c24x7.models.tags.CTagsStats;
import com.c24x7.textanalyzer.CTaggedNGramsExtractor;
import com.c24x7.textanalyzer.ngrams.CTaggedNGramsMap;
import com.c24x7.textanalyzer.ngrams.CTaggedWord.ETAG_TYPES;
import com.c24x7.textanalyzer.CTagger;
import com.c24x7.textanalyzer.CTagger.NSentenceTokens;




public final class CNGramsGenerator {
	private CNGramsStats		_nGramsFrequencyStats	= null;
	private CTaggedNGramsExtractor 	_extractor = null;
	
	public CNGramsGenerator() {
		super();
	}
			/**
			 * <p>Constructor for extracting statistics related to 
			 * N-Grams within a document.</p>
			 * @param nGramsStatsList array of statistics related to the ranking of N-Grams within a document.
			 */
	public CNGramsGenerator(final CNGramsStats nGramsFrequencyStats) {
		super();
		_nGramsFrequencyStats = nGramsFrequencyStats;
		_extractor = new CTaggedNGramsExtractor();
	}
	
	
	public CNGramsStats getNGramsFrequencyStats() {
		return _nGramsFrequencyStats;
	}
	
	public boolean extract(CText document, final String inputText) {
		if( document == null) {
			throw new IllegalArgumentException("Cannot extract N-Grams from undefined document");
		}
		
		return _extractor.extract(document, inputText);
	}
	
	public final CTaggedNGramsMap getNGramsMap() {
		return _extractor.getNGramsMap();
	}
	
			/**
			 * <p>Extract the statistics of a list of specified N-Gram tags from 
			 * a document or a reference corpus database.</p> 
			 * @param content content of the document from which the N-Gram tags have to be extracted.
			 * @param nGram N-Gram for which tags have to be collected
			 * @param tagsFilter list of tags for which statistics have to be collected. If the 
			 * parameter is null, the tags are extracted without statistics
			 * @return tag of the N-Gram
			 * @throws IllegalArgumentException if either content or N-Gram is undefined. 
			 */

	public String extractTags(	final String content, 
								final String nGram, 
								CTagsStats tagsStatistics) {
		
		if( content == null || nGram == null) {
			throw new IllegalArgumentException("Cannot extract N-Gram tag from undefined content or N-Gram");
		}
		
		String nGramTag = null;
		String[] nGramTerms = nGram.split(" ");
		
		CTagger tagsProc = new CTagger();
		NSentenceTokens[] sentenceTokensList = tagsProc.extractTokens(content);		
		if(sentenceTokensList != null && sentenceTokensList.length > 0) {
			
			String[] tokensStr = null;
			for(NSentenceTokens tokens :  sentenceTokensList) {
				String[] tags = tagsProc.extractTags(tokens);
				tokensStr = tokens.getTokens();
				for( int k = 0; k < tags.length; k++) {
					boolean match = false;
				
					if(tokensStr[k].compareTo(nGramTerms[0]) == 0) {
						match = true;
					
						for( int j = 1; j < nGramTerms.length && k+j < tags.length; j++) {
							if(tokensStr[k+j].compareTo(nGramTerms[j]) != 0) {
								match = false;
								break;
							}
						}
						
						if( match ) {
							ETAG_TYPES tagType = null;
							StringBuilder buf = new StringBuilder();
							
							for( int i = 0; i < nGramTerms.length-1 && k+i < tags.length; i++) {
								tagType = ETAG_TYPES.getTagType(tags[k+i], nGramTerms[i]);
								if( tagType != null) {
									buf.append(tagType.getTag());
								}
								else {
									if( tagsStatistics != null) {
										tagsStatistics.put(tags[k+1], tokensStr[k+i]);
									}
									buf.append(tags[k+i]);
								}
								buf.append(" ");
							}
							
							int lastTermIndex = k+nGramTerms.length-1;
							if( lastTermIndex < tags.length) {
								buf.append(tags[lastTermIndex]);
								nGramTag = buf.toString();
								return nGramTag;
							}
						}
					}
				}
			}
		}
		
		return nGramTag;
	}


	
	/**
	 * <p>If the N-Gram has been already extracted from the document, 
	 * then compute its relative frequency. Statistics may be collected
	 * for training a classifier if the list of NGrams stats have been
	 * provided in the constructor.</p>
	 * 
	 * @param nGram label of the N-Gram
	 * @param nGramNumOccurrences relative number of occurrences of the N-Gram in the document
	 * @param maxLabelnumLabelOccurrences maximum number of occurrences of any term of the N-Gram in the document.
	 */
	
	protected void updateNGramsFrequencyStats(final String nGram, double nGramNumOccurrences, double maxLabelnumLabelOccurrences) {
		
		if( _nGramsFrequencyStats != null && _nGramsFrequencyStats.isLabel(nGram)) {
			_nGramsFrequencyStats.setNumOccurrences(nGramNumOccurrences, maxLabelnumLabelOccurrences);
		}
	}
}

//----------------- EOF ------------------------------------------------------------

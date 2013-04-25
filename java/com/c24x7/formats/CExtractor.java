//  Copyright (C) 2010-2011  Patrick Nicolas
package com.c24x7.formats;

import java.util.Set;

import com.c24x7.util.string.CStringUtil;
import com.c24x7.util.CIntMap;



public final class CExtractor {
	public final static int MAX_CHARS_IN_TITLE = 140;
	
	protected String	_seed		= null;
	protected String 	_title 		= null;
	protected String 	_body 		= null;
	protected CIntMap _signWords 	= null;
		
	
	public CExtractor() {
		this(null);
	}
	
	public CExtractor(final String seed) { 
		if( seed == null || seed.length() < 32) {
			throw new IllegalArgumentException("Undefined seed");
		}
		_seed = seed;
		_signWords = new CIntMap();
	}
	
	
	public final Set<String> getSignWords() {
		/*
		ITextAnalyzer analyzer = new CTextAnalyzer(true);
		Map<String, Integer> signWords = analyzer.getSignificantWords(_seed);
		return (signWords != null && signWords.size() > 0) ? signWords.keySet() : null;
		*/
		
		return  _signWords.order();
	}
	
	/*
	public final String getTitle(CGenOutput output) {
		if( output == null ) {
			throw new IllegalArgumentException("Cannot extract title from a null output");
		}
		if( _title == null ) {
			_title = output.getTitle();
			_body = output.getBody();
		}
		return _title;
	}
	*/
	
	public final String getTitle() {
		return _title;
	}
	
	
	public String[] extract(final String[] sentences) {
		
		String[] bestSentences = null;
		/*
		if( sentences.length < 3) {
			bestSentences = new String[sentences.length];
			for( int k = 0; k < sentences.length;  k++) {
				bestSentences[k] = sentences[k];
			}
		}
		else {
			bestSentences = new String[2];
	
			float aveSimilarity = 0.0F;
			CSimpleTextAnalyzer analyzer = new CSimpleTextAnalyzer();
			CIntMap signWords = analyzer.getSignificantWords(_seed);
			_signWords.putAll(signWords, 3);
		
			if( signWords != null) {			
				float maxSimilarity = -1.0F, similarity = 0.0F;
				StringBuilder buf = new StringBuilder();
				CIntMap sentenceSignWord = null;
			
				int sentenceCounter = 0;
				int selectedSentence = -1;
				for(String sentence : sentences) {
					buf.append(sentence);
					sentenceSignWord = analyzer.getSignificantWords(sentence);
		
					if( sentenceSignWord != null) {
						_signWords.putAll(sentenceSignWord);
					
						int score = 0;
						for(String key : signWords.keySet()) {
							if( sentenceSignWord.containsKey(key) ) {
								score += signWords.get(key).intValue();
							}
						}
						similarity = (float)score*100.0F/signWords.size();
				
						if( similarity > maxSimilarity && sentence.length() < MAX_CHARS_IN_TITLE) {
							maxSimilarity = similarity;
							selectedSentence = sentenceCounter;
						}
						aveSimilarity += similarity;
					}
					sentenceCounter++;
				}
				
				bestSentences = new String[( selectedSentence == sentences.length -1) ? 1 : 2 ];
				for( int k = 0; k < bestSentences.length; k++) {
					bestSentences[k] = sentences[selectedSentence+k];
				}
			}
			
		}
		*/
		return bestSentences;
	}
	
	public final String getSeed() {
		return _seed;
	}
	
	
	public final String getTweet(final String link) {
		int maxChars = (link != null) ? 140 - link.length() - 5 : 140;
		return CStringUtil.extractFirstWords(_title, maxChars);
	}
	
	public final String getFacebookStatus(final String link) {
		int maxChars = (link != null) ? 255 - link.length() - 5 : 255;
		return CStringUtil.extractFirstWords(_title, maxChars);
	}
	
	public final String getSubject() {
		return CStringUtil.extractFirstWords(_title, 56);
	}
	
	public final String getBody() {
		return _body;
	}
	
	
	public final String getSignWordsList() {
		StringBuilder buf = new StringBuilder();
		
		for( String key : _signWords.keySet() ) {
			buf.append(" ");
			buf.append(key);
		}
		return buf.toString();
	}
}

// -------------------------- EOF ------------------------------------

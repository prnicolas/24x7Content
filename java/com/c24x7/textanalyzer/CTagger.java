// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;



			/**
			 * <p>Main NLP class that extract the sentences, tokens and tags from 
			 * a content.These methods rely on the OpenNLP library.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 10/14/2011
			 */
public final class CTagger {
	private static final String SENT_DETECT 		= CEnv.projectDir + "models/sentdetect/en-sent.bin";
	private static final String TOKENIZER 			= CEnv.projectDir + "models/tokenizer/en-token.bin";
	private static final String POSTAG_MAXENT 		= CEnv.projectDir + "models/postag/en-pos-maxent.bin";

	private static TokenizerME 			tokenizer = null;
	private static SentenceDetectorME 	sdetector = null;
	private static POSTaggerME 			tagger = null;
	
	
	public static void init() throws InitException {
		if( !initPosTagger() ||  !initTokenizer() || !initSentenceDetector() ) {
			throw new InitException("Cannot initialize POS tagger or tokenizer");
		}
	}
	
	
			/**
			 * <p>List of tokens extracted from a specific sentence.</p>
			 * @author Patrick Nicolas
			 * @date 05/13/2011
			 */
	public final class NSentenceTokens {
		private String[] _tokens = null;
			
			/**
			 * <p>Create a list of tokens extracted from a sentence</p>
			 * @param tokens array of tokens or terms
			 */
		public NSentenceTokens(final String[] tokens) {
			_tokens = tokens;
		}
			/**
			 * <p>Return the list of tokens from a sentence</p>
			 * @return array of tokens
			 */
		public final String[] getTokens() {
			return _tokens;
		}
	}
	
	
			/**
			 * <p>Extracts an array of sentences tokens from an input text.</p>
			 * @param input input text to analyzer
			 * @return The array of sentences tokens
			 *  @throws IllegalArgumentException if the content input is not defined
			 */
		
	public NSentenceTokens[] extractTokens(final String input) {
		if( input == null ) {
			throw new IllegalArgumentException("Cannot extract tokens from undefined input");
		}
		
		NSentenceTokens[] tokens = null;
		String[] sentences = extractSentences(input);
		if( sentences != null && sentences.length > 0) {
			tokens = extractTokens(sentences);
		}
		
		return tokens;
	}


	

			/**
			 * <p>Extracts an array of tokens from a raw text
			 * @param input input text to analyzer
			 * @return The array of sentences..
			 * @throws IllegalArgumentException if the content input is not defined
			 */
		
	private String[] extractSentences(final String input) { 
		if( input == null || input.length() < 8) {
			throw new IllegalArgumentException("Cannot extract tokens from undefined input");
		}
		String[] sentences = null;
		synchronized( sdetector) {
			sentences = sdetector.sentDetect(input);
		}
		
		return sentences;
	}
	
			/**
			 * <p>Extracts an array of tokens from a raw text
			 * @param rawText input text to analyzer
			 * @return The array of sentences..
			 * @throws IOException
			 */
	private NSentenceTokens[] extractTokens(final String[] sentences) {
		
		NSentenceTokens[] sentTokensList = null;
		String[] tokens = null;
		sentTokensList = new NSentenceTokens[sentences.length];
		
		int sentCounter =0;
		for( String sentence : sentences ) {
			synchronized( tokenizer) {
				tokens = tokenizer.tokenize(sentence);
			}
			sentTokensList[sentCounter] = new NSentenceTokens(tokens);
			sentCounter++;
		}
		
		return sentTokensList;
	}

	
	public String[] extractTags(final String[] tokens) {
		String[] tags = null;
		
		synchronized( tagger) {
			tags = tagger.tag(tokens);
		}
		return tags;
	}
	
			/**
			 * <p>Extract the tags of the tokens extracted from a sentence.</p>
			 * @param tokens tokens extracted from the sentence
			 * @return array of tags associated with this sentence
			 * @throws IllegalArgumentException exception if tokens are not defined..
			 */
	
	public String[] extractTags(final NSentenceTokens tokens) {
		if( tokens == null) {
			throw new IllegalArgumentException("Cannot extract tags from undefined tokens");
		}
		
		String[] tags = null;
		String[] tokensStr = tokens.getTokens();
		synchronized( tagger) {
			tags = tagger.tag(tokensStr);
		}
		return tags;
	}

	
					// -----------------------------
					// Private Supporting Methods
					// -----------------------------
	
	private static boolean initTokenizer() {
		InputStream in = null;
	
		try {
			in = new FileInputStream(TOKENIZER);
			TokenizerModel tokenModel = new TokenizerModel(in);
			tokenizer = new TokenizerME(tokenModel);
		}
		catch (IOException e) {
			CLogger.error("Cannot load token detector model " + e.toString());
		}
		finally {
			if( in != null ) {
				try {
					in.close();
				}
				catch( IOException e) {
				}
			}
		}
		
		return (tokenizer != null);
	}

	
	private static boolean initSentenceDetector() {
		FileInputStream in = null;
	
		try {
			in = new FileInputStream(SENT_DETECT);
			SentenceModel sentdetectModel = new SentenceModel(in);
			sdetector =  new SentenceDetectorME(sentdetectModel);
		}
		catch (IOException e) {
			CLogger.error("Cannot load sentence detector model " + e.toString());
		}
		finally {	
			if( in != null ) {
				try {
					in.close();
				}
				catch( IOException e) {
					CLogger.error("Cannot load sentence detector model " + e.toString());
				}
			}
		}
		
		return (sdetector != null);
	}
	
	
	private static boolean initPosTagger() {	
		InputStream in = null;
		try {
			in = new FileInputStream(POSTAG_MAXENT);
			POSModel posModel = new POSModel(in);
			tagger = new POSTaggerME(posModel);
			in.close();
		}
		catch( IOException e) {
			CLogger.error("InitPosTagger: " + e.toString());
		}
		finally { 
			if( in != null ) {
				try {
					in.close();
				}
				catch( IOException e) {
					CLogger.error("InitPosTagger: " + e.toString());
				}
			}
		}
		return (tagger != null);
	}
}
// -------------------------  EOF ------------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;



			/**
			 * <p>Main NLP class that extract the sentences and tokens from
			 * a given text or document. The detection of sentence and words
			 * boundary relies on the Maximum Entropy algorithm implemented
			 * in the OpenNLP library..</p>
			 * 
			 * @see com.c24x7.textanalyzer.CTokens
			 * @author Patrick Nicolas
			 * @date 10/14/2011
			 */
public final class CTokens extends CWords {
	private static final String TOKENIZER 	= CEnv.projectDir + "models/tokenizer/en-token.bin";

	private static TokenizerME 	tokenizer = null;

	
	/**
	 * <p>Initialize the sentences and words boundary detection by loading the
	 * relevant OpenNLP model file.</p>
	 * @throws InitException if the sentence detection model file is not available.
	 */

	public static void init() throws InitException {
		if( !initTokenizer() ||  !initSentenceDetector()) {
			throw new InitException("Cannot initialize tokenizer");
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


	
					// -----------------------------
					// Private Supporting Methods
					// -----------------------------
	
	private static boolean initTokenizer() {
		InputStream in = null;
	
		try {
			/*
			 * Loads the tokenizer model from file.
			 */
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
	
}
// -------------------------  EOF ------------------------------------
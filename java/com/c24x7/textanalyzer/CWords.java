/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.textanalyzer;

import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import com.c24x7.exception.InitException;
import com.c24x7.models.CText;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



		/**
		 * <p>Basic class to extract Words or Tokens from a text or document. The 
		 * main method relies on the Maximum Entropy classifier to extract each 
		 * sentence using OpenNLP. A more elaborate extraction of tokens is 
		 * implemented in the CTokens subclass</p>
		 * 
		 * @see com.c24x7.textanalyzer.CTokens
		 * @author Patrick Nicolas         24x7c 
		 * @date April 7, 2012 4:58:45 PM
		 */
public class CWords {
	protected static final String SENT_DETECT = CEnv.projectDir + "models/sentdetect/en-sent.bin";
	protected static SentenceDetectorME 	sdetector = null;
	
	
		/**
		 * <p>Initialize the sentence detection algorithm by loading the
		 * relevant OpenNLP model file.</p>
		 * @throws InitException if the sentence detection model file is not available.
		 */
	public static void init() throws InitException {
		if( !initSentenceDetector() ) {
			throw new InitException("Cannot initialize the sentence detector");
		}
		System.out.println("Sentence detection ready");
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
			 * @throws IllegalArgumentException if the content input is not defined
			 */
	public NSentenceTokens[] extractTokens(CText document, final String input) {
		if( input == null ) {
			throw new IllegalArgumentException("Cannot extract tokens from undefined input");
		}
		
		NSentenceTokens[] tokens = null;
		String[] sentences = extractSentences(input);		
		String cleanSentence = null;
		
		if( sentences != null && sentences.length > 0) {
			tokens = new NSentenceTokens[sentences.length];
			
			/*
			 * Extract the sentence and remove the trailing
			 * punctuation such as '.', '?' or '!'
			 */
			for(int j = 0; j < sentences.length; j++) {
				cleanSentence = CStringUtil.stripSentence(sentences[j]);
				tokens[j] = new NSentenceTokens(cleanSentence.split(" "));
			}
			
			/*
			 * Add the array of sentences in the text model.
			 */
			document.setSentences(sentences);
		}
		
		
		return tokens;
	}


	
	

			/**
			 * <p>Extracts an array of tokens from a raw text
			 * @param input input text to analyzer
			 * @return The array of sentences..
			 * @throws IllegalArgumentException if the content input is not defined
			 */
		
	protected String[] extractSentences(final String input) { 
		if( input == null ) {
			throw new IllegalArgumentException("Cannot extract tokens from undefined input");
		}
		String[] sentences = null;
		synchronized( sdetector) {
			sentences = sdetector.sentDetect(input);
		}
		
		return sentences;
	}


	
	protected static boolean initSentenceDetector() {
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
	

}

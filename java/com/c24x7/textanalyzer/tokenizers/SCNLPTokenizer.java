// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tokenizers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;


import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;



		/**
		 * <p>Tokenizer that relies on a Maximum Entropy classifier as implemented
		 * in the OpenNLP open source library.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date February 19, 2012 8:56:27 PM
		 */
public final class SCNLPTokenizer implements ITokenizer {
	protected static final String SENT_DETECT 	= CEnv.projectDir + "models/sentdetect/en-sent.bin";
	protected static final String TOKENIZER 	= CEnv.projectDir + "models/tokenizer/en-token.bin";
	protected static TokenizerME 			tokenizer = null;
	protected static SentenceDetectorME 	sdetector = null;
	
		/**
		 * <p>Initialize the sentence detector and tokenizer used in OpenNLP<.p>
		 * @throws InitException if the model parameters cannot be loaded.
		 */
	public static void init() throws InitException {
		try{
			initSentenceDetector();
			initTokenizer();
		}
		catch(IOException e) {
			throw new InitException(e.toString());
		}
	}
	
	/**
	 * <p>
	 * Create a words tokenizer using Maximum Entropy classifier in OpenNLP</p>.
	 * </p>
	 */
	public SCNLPTokenizer() { }
	

		/**
		 * <p>Break down a text into tokens using the Maximum Entropy
		 * classifier implemented in OpenNLP library.</p>
		 * @param inputText text to be tokenized 
		 * @return list of tokens extracted from the input text using NLP classes
		 * @throws IllegalArgumentException if the input text is undefined.
		 */
	public List<String> tokenize(final String inputText) {
		if( inputText == null ) {
			throw new IllegalArgumentException("Cannot tokenize undefined content");
		}
		
		List<String> tokensList = new LinkedList<String>();
		String[] sentences = null;
		
			/*
			 * First break down the input text into sentences
			 */
		if( sdetector != null ) {
			sentences = sdetector.sentDetect(inputText);
			
			/*
			 * Then break down each sentence into tokens
			 */
			if( tokenizer != null && sentences != null) {
				String[] tokens = null;
				for( String sentence : sentences) {
					tokens = tokenizer.tokenize(sentence);
					
					for( String token : tokens) {
						tokensList.add(token);
					}
				}
			}
		}
		
		return tokensList;
	}

	
	
									// ----------------------
									// Private Methods
									// -----------------
	
	private static void initTokenizer() throws IOException {
		InputStream in = null;
	
		try {
			in = new FileInputStream(TOKENIZER);
			TokenizerModel tokenModel = new TokenizerModel(in);
			tokenizer = new TokenizerME(tokenModel);
		}
		finally {
			if( in != null ) {
				in.close();
			}
		}
	}

	
	private static  void initSentenceDetector() throws IOException {
		FileInputStream in = null;
	
		try {
			in = new FileInputStream(SENT_DETECT);
			SentenceModel sentdetectModel = new SentenceModel(in);
			sdetector =  new SentenceDetectorME(sentdetectModel);
		}
		finally {	
			if( in != null ) {
				in.close();
			}
		}
	}
}
// ---------------------  EOF -----------------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.nlservices;

import java.util.Map;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.CText;
import com.c24x7.topics.CTopicsMap;
import com.c24x7.semantics.CTaxonomyConnectionsPool;
import com.c24x7.semantics.CTaxonomyConnectionsPool.NTaxonomiesConn;
import com.c24x7.semantics.CTaxonomyExtractor;
import com.c24x7.textanalyzer.CNGramsExtractor;
import com.c24x7.topics.CTopicsExtractor;
import com.c24x7.topics.CTopicsExtractor.NFeaturesSet;



			/**
			 * <p>Analyzer class to process and classify user defined content.</p>
			 * @author Patrick Nicolas
			 * @date 04/19/2011
			 */
public final class CTextSemanticService {
	private NTaxonomiesConn _taxonomyConnection = null;
	private short			_textIndex = 0;

	/**
	 * <p>Create a text semantic service.</p>
	 */
	public CTextSemanticService() throws SemanticAnalysisException {
		this((short)0);
	}
	
	
	/**
	 * <p>Create a text semantic service.</p>
	 */
	public CTextSemanticService(short index) throws SemanticAnalysisException {	
		_taxonomyConnection = CTaxonomyConnectionsPool.getInstance().getLabelsConnection();
		_textIndex = index;
	}


	
		/**
		 * <p>Extract the semantic components of a document using NLP algorithm to extract
		 * semantic N-Grams, WordNet hypernyms and dbpedia entries.</p>
		 * @param inputText input document
		 * @return the model of the text to be analyzed.
		 */
	public void execute(final String inputText, CTopicsMap topicsMap) {
		this.execute(inputText, null, topicsMap);
	}


		/**
		 * <p>Extract the semantic components of a document using NLP algorithm to extract
		 * semantic N-Grams, WordNet hypernyms and dbpedia entries.</p>
		 * @param inputText input document
		 * @param title title of the document.
		 * @return the model of the text to be analyzed.
		 */
	public void execute(final String inputText, final String title, CTopicsMap topicsMap) {
		if(inputText == null) {
			throw new IllegalArgumentException("Cannot extract semantics data from an undefined document");
		}
		


		CText document = getTaxonomyList(inputText, title, false);
		if( document.getState() == CText.E_STATES.TAXONOMY) {
			
			CTopicsExtractor topicsExtractor = new CTopicsExtractor();
			if( topicsExtractor.extract(document, topicsMap, _textIndex) ) {
				
				document.setState(CText.E_STATES.TOPICS);
				topicsMap.addSentences(document.getSentences());
			}
		}
	}
			


	
	public CText getTopicsFeaturesList(final String inputText, Map<String, NFeaturesSet> topicFeaturesMap) {
		if(inputText == null) {
			throw new IllegalArgumentException("Cannot extract semantics data from an undefined document");
		}

		CText document = getTaxonomyList(inputText, null, false);
		if( document.getState() == CText.E_STATES.TAXONOMY) {
			CTopicsMap topicsMap = new CTopicsMap();
			(new CTopicsExtractor()).getFeaturesList(document, topicFeaturesMap, topicsMap);	
		}

		return document;
	}
	
	
	
	private CText getTaxonomyList(	final String inputText, 
									final String title, 
									boolean breakdown) {
			/*
			 * Step 1: Create a document model
			 */
		CText document = new CText(title);
				
			/*
			 * Step 2:Extract the N-Grams from the document
			 */
		CNGramsExtractor nGramsExtract = new CNGramsExtractor();
		
		//INFO
		System.out.println("Last execution");
		if( nGramsExtract.extract(document, inputText) ) {
			document.setState(CText.E_STATES.NGRAMS);
			System.out.println("NGram extracted");
			
			/*
			 * Step 3: Extract Composite and semantics 
			 */
			CTaxonomyExtractor taxonomyExtractor = new CTaxonomyExtractor(_taxonomyConnection, breakdown);
			if( taxonomyExtractor.extract(document) ) {
				document.setState(CText.E_STATES.TAXONOMY);
			}
		}
		
		return document;
	}
		
	
	
	/**
	 * <p>Close all the connections used in this service.</p>
	 */
	public void close() {
		_taxonomyConnection.close();
	}

}


// ----------------------------------------  EOF ----------------------------------------
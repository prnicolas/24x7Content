// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.wordnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;


				/**
				 * <p>Client class to access WordNet dictionary to resolve ambiguities 
				 * for the synonym and hypernyms (ontology) of a word.</p>
				 * @author Patrick Nicolas
				 * @date 06/11/2011
				 */
public final class CWordNet {
	public final static String WORDNET_CONFIG_FILE = CEnv.configDir + "file_properties.xml";
	protected final static String EXCEPTION_LABEL1 = "Cannot extract synonyms: ";
	protected final static String EXCEPTION_LABEL2 = "Cannot extract glossary of null terms";
		
	protected static CWordNet instance = null;
	
	public static CWordNet getInstance() {
		if( instance == null) {
			init();
		}
		return instance;
	}
			/**
			 * <p>Static initialization of the WordNet Extractor.</p>
			 * @return true if the initialization is successful, false otherwise.
			 */
	public static boolean init() {
		boolean success = false;
		
		try {
			instance = new CWordNet();
			success = true;
		}
		catch( JWNLException e) {
			CLogger.error("Cannot create a WordNet client " + e.toString());
		}
		catch( IOException ex) {
			CLogger.error("Cannot create a WordNet client  " + ex.toString());
		}		
		return success;
	}
	
	
					/**
					 * <p>Test whether the token is a valid word with synonyms..</p>
					 * @param token token or word to test....
					 * @return true if the term is valid, false otherwise..
					 * @throws IOException
					 */
	public boolean isValid(final String token) throws IOException {
		IndexWord word = null;
		try {
			if( Dictionary.getInstance() != null ) {
				word = Dictionary.getInstance().getIndexWord(POS.NOUN, token);
			}
		}
		catch( JWNLException e) {
			throw new IOException(EXCEPTION_LABEL1 + e.toString());
		}
		return (word != null);
	}
	
	
	public Map<String, Object> getTaxonomy(final String word) {
		Map<String, Object> hypernymsMap = new HashMap<String, Object>();
		try {
			
			IndexWord indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, word);
			
			if( indexWord != null ) {
				Synset[] synsets = indexWord.getSenses();
				for( Synset synset : synsets) {
					getTaxonomy(synset, hypernymsMap );
				}
			}
		}
		catch( JWNLException e) {
			CLogger.error(e.toString());
		}
		
		return hypernymsMap;
	}
	
	
	public List<String> getSynsetWords(final String word) {
		List<String> synsetsList = new LinkedList<String>();
		try {
			
			IndexWord indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, word);
			
			if( indexWord != null ) {
				Synset[] synsets = indexWord.getSenses();
				for( Synset synset : synsets) {
					for( Word term : synset.getWords()) {
						synsetsList.add(term.toString());
					}
				}
			}
		}
		catch( JWNLException e) {
			CLogger.error(e.toString());
		}
		
		return synsetsList;
	}
	
	public List<String> getSynsetLemma(final String word) {
		List<String> synsetsList = new LinkedList<String>();
		try {
			
			IndexWord indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, word);
			
			if( indexWord != null ) {
				Synset[] synsets = indexWord.getSenses();
				for( Synset synset : synsets) {
					for( Word term : synset.getWords()) {
						synsetsList.add(term.getLemma());
					}
				}
			}
		}
		catch( JWNLException e) {
			CLogger.error(e.toString());
		}
		
		return synsetsList;
	}
	
	public List<String> getSynsetGloss(final String word) {
		List<String> synsetsList = new LinkedList<String>();
		try {
			
			IndexWord indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, word);
			
			if( indexWord != null ) {
				Synset[] synsets = indexWord.getSenses();
				for( Synset synset : synsets) {
					synsetsList.add(synset.getGloss());
				}
			}
		}
		catch( JWNLException e) {
			CLogger.error(e.toString());
		}
		
		return synsetsList;
	}
	
	
					/**
					 * <p>Retrieve the map of keywords and definition from WordNet.</p>
					 * @param terms list of terms to evaluate
					 * @return map of synonyms
					 * @throws IOException
					 */
	
	public Map<String, String[]> getTaxonomy(final List<String> terms)  {
		if( terms == null ) {
			throw new IllegalArgumentException(EXCEPTION_LABEL2);
		}
		Map<String, String[]> glossWordsMap = new HashMap<String, String[]>();
			
		try {
			IndexWord word = null;
			Synset[] syns = null;
			String[] glossWords = null;
	
			for( String term : terms ) {
				word = Dictionary.getInstance().getIndexWord(POS.NOUN, term);
				
				if( word != null ) {
					syns = word.getSenses();
			 
					glossWords = new String[syns.length];
					for (int i = 0; i < syns.length; i++) {
						glossWords[i] = syns[i].getGloss();
					}
					glossWordsMap.put(term, glossWords);
				}
			}
		}
		catch( JWNLException e) {
			CLogger.error(EXCEPTION_LABEL1 + e.toString());
		}

		return glossWordsMap;
	}
	
	
	
						// ------------------------
						// Private Supporting Methods
						// ----------------------------
	
	
	private CWordNet() throws JWNLException, IOException {
		InputStream inputprops = null; 
		try {
			inputprops = new FileInputStream(new File(WORDNET_CONFIG_FILE));
			JWNL.initialize(inputprops);
		}
		catch( JWNLException e) {
			CLogger.error("Cannot create a CWordNetExtractor " + e.toString());
		}
		catch( IOException ex) {
			CLogger.error("Cannot create a CWordNetExtractor " + ex.toString());
		}
		finally {
			if( inputprops != null ) {
				try {
					inputprops.close();
				}
				catch( IOException ex) { 
					CLogger.error("Cannot close WordNet database " + ex.toString());
				}
			}
		}
	}
	

	private void getTaxonomy(final Synset synset, 
							 Map<String, Object> hypernymsMap) throws JWNLException {		
		
		PointerTargetTree ancestorTree = PointerUtils.getInstance().getHypernymTree(synset);		
		PointerTargetNodeList[] nodesLists = ancestorTree.reverse();
		
		PointerTargetNode targetNode = null;
		StringBuilder ontologyStr = null;
		String lemma = null;
		int lastNodeIndex = 0;
		Word[] words = null;
		String classStr = null;
		String[] classes = null;
		int wordCounter = 0;
		int sensesCounter =0;
		
		for( PointerTargetNodeList nodesList : nodesLists) {
			ontologyStr  = new StringBuilder();
			
			lastNodeIndex = nodesList.size() - 1;
			sensesCounter =0;
			
			for( Object node : nodesList ) {
				
				targetNode = (PointerTargetNode)node;
				Synset thisSynset = targetNode.getSynset();
				words = thisSynset.getWords();
				
				/*
				 * Extract the hypernyms or taxonomy classes.
				 */
				if( sensesCounter < lastNodeIndex) {
					
					if( words != null) {
						if( words.length > 0) {
							classStr = words[0].getLemma().replace("_" , " ");
							classes = classStr.split(CEnv.FIELD_DELIM);
							ontologyStr.append(classes[0].trim());
							ontologyStr.append("/");
						}
					}
				}
				
				else {
					wordCounter = 0;
					for( Word term : words) {
						lemma = term.getLemma().replace("_" , " ");
						ontologyStr.append(lemma);
						if(++wordCounter < words.length) {
							ontologyStr.append(CEnv.FIELD_DELIM);
						}
						hypernymsMap.put(lemma, null);
					}
				}
				sensesCounter++;
			}
			hypernymsMap.put(ontologyStr.toString(), new Object());
		}
	}
}

// ------------------------  EOF -------------------------------
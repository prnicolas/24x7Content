/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.nlservices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.CTopicPoint;
import com.c24x7.topics.CTopicsMap;



public class CServiceManager implements Runnable {
	private List<String> _inputTextList 	= null;
	private List<String> _errorsList		= null;
	
	public CServiceManager() {
		_inputTextList = new ArrayList<String>();
	}
	
	public void addInputText(final String inputText) {
		_inputTextList.add(inputText);
	}
	
	public void run() {
		CTextSemanticService semanticService = null;
		
		for(String inputText : _inputTextList) {
			try {
				semanticService = new CTextSemanticService();
			}
			catch( SemanticAnalysisException e) {
			}
		}
	}
	
	
	
	public Collection<CTopicPoint> execute() {
		return execute(null, null, null);
	}
			
	
	public Collection<CTopicPoint> execute(	StringBuilder matrixDimBuf, 
											StringBuilder sentencesBuf, 
											StringBuilder topicsBuf) {
	
		CTextSemanticService semanticService = null;
		CTopicsMap topicsMap = new CTopicsMap();
		Map<String, CTopicPoint> topicPointsMap = null;
		
		int index = 0;
		for(String inputText : _inputTextList) {
			try {
				semanticService = new CTextSemanticService();
				semanticService.execute(inputText, topicsMap);
				index++;
			}
			catch( SemanticAnalysisException e) {
				if( _errorsList == null ) {
					_errorsList = new LinkedList<String>();
				}
				_errorsList.add(new String(e.toString()));
			}
		}
		
		/*
		 * Execute the topography service.
		 */
		CTopographyService topographicService = new CTopographyService();
		topicPointsMap = new HashMap<String, CTopicPoint>();
		topographicService.execute(topicsMap, topicPointsMap);
	
		/*
		 * Collect the dimension of the XY matrix.
		 */
		if( matrixDimBuf != null) {
			matrixDimBuf.append(topographicService.getNumSentences());
			matrixDimBuf.append(",");
			matrixDimBuf.append(topographicService.getNumTopicsIndexes());
		}
		
		if( sentencesBuf != null ) {
			List<String> sentenceAbstractList = topographicService.getSentences();
			int lastSentenceIndex = sentenceAbstractList.size()-1;
			
			index = 0;
			for(String sentenceAbstract : sentenceAbstractList) {
				sentencesBuf.append(sentenceAbstract);
				if( ++index < lastSentenceIndex) {
					sentencesBuf.append("#");
				}
			}
		}
			
		if( topicsBuf != null ) {
			for(String topic : topographicService.getTopics()) {
				topicsBuf.append(topic);
				topicsBuf.append("#");
			}
		}
		
		return topicPointsMap.values();
	}
}

// -----------------------------------  EOF ---------------------------------------

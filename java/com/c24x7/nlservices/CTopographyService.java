/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.nlservices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.c24x7.math.utils.CFreqArray;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CTopicPoint;
import com.c24x7.topics.CTopicsMap;
import com.c24x7.util.CEnv;
import com.c24x7.util.CIntMap;
import com.c24x7.util.collections.CGraph;
import com.c24x7.util.string.CStringUtil;



public final class CTopographyService {
		
	private class NSentenceIndex {
		private String _sentence = null;
		private String _title = null;
		
		private NSentenceIndex(final String sentence) {
			this(sentence, null);
		}
		
		private NSentenceIndex(final String sentence, final String title) {
			_sentence = sentence;
			_title = title;
		}
		
		private final String getSentence() {
			return _sentence;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_sentence);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(_title);
			
			return buf.toString();
		}
	}
	
	public interface IFunction {
		public void apply(final String nodeLabel, int level);
	}
	
	
	
	public final class NTopicUpdateFunction implements IFunction {
		private int 						_topicIndex = 0;
		private Map<String, CTopicPoint>	_topicPointsMap = null;
		
		public NTopicUpdateFunction(int topicIndex, Map<String, CTopicPoint> topicPointsMap) { 
			_topicIndex = topicIndex;
			_topicPointsMap = topicPointsMap;
		}
		

		public void apply(final String nodeLabel, int level) {
			CTopicPoint topicPoint = _topicPointsMap.get(nodeLabel);
			topicPoint.setTopicIndex(_topicIndex + level);
		}
	}

	
	private NSentenceIndex[] _sentenceAxis 			= null;
	private CIntMap      	 _topicsDistribution 	= null;
	private String[] 		 _topTopics				= null; 

	
	public int getNumSentences() {
		return _sentenceAxis.length;
	}
	
	public int getNumTopicsIndexes() {
		return _topicsDistribution.size()*10;
	}
	
	
	public final String[] getTopics() {
		return _topTopics;
	}
	
	
	
	public final List<String> getSentences() {
		List<String> sentencesList = new ArrayList<String>();
		
		String sentence = null;
		for(NSentenceIndex sentenceIndex : _sentenceAxis) {
			sentence = CStringUtil.extractFirstWords(sentenceIndex.getSentence(), 48);
			sentencesList.add(sentence);
		}
		return sentencesList;
	}
	
		/**
		 * <p>Create a semantic topography map for a list of text (or document)
		 * and a topics map extracted from the list of documents.</p>
		 * @param topicsMap topics map for the list of documents
		 * @param textList list of textual documents.
		 */
	public void execute(final CTopicsMap 		 topicsMap, 
						Map<String, CTopicPoint> topicPointsMap) {
		
		if( topicsMap == null || topicPointsMap == null) {
			throw new IllegalArgumentException("Cannot create semantic map from undefined topics map");
		}
		
			/*
			 * Create an ordered array (Axis) of topics.
			 */
		createTopicsAxis(topicsMap);
		
			/*
			 * Create an ordered array of sentences indices for all the 
			 * documents used to generate the semantic topography map
			 */
		createSentencesAxis(topicsMap);
		
			/*
			 * Create the final topographic points
			 */
		createTopographyPoints(topicsMap, topicPointsMap);
	}
	
	
	
	
					
								// ---------------------------
								//  Private Supporting Methods
								// ---------------------------
	
	
	/**
	 * <p>
	 * TODO: order top topics (graph root label) according to a predefined rank.
	 * @param topicsMap
	 */
	private void createTopicsAxis(final CTopicsMap topicsMap) {
		List<CGraph> graphsList = topicsMap.getGraphsList();
		
		_topTopics = new String[graphsList.size()];
		
		/*
		 * Get the top of each graph as the main topic
		 */
		int index = 0;
		for( CGraph graph : graphsList) {
			_topTopics[index] = graph.getRootLabel();
			index++;
		}
		
		// TODO: It is ordering topics according to their natural order not as
		// a logical groups..
		Arrays.sort(_topTopics);
		
		index = 0;
		_topicsDistribution = new CIntMap();
		for(; index < _topTopics.length; index++) {
			_topicsDistribution.put(_topTopics[index], Integer.valueOf(index*10));
		}
	}
	
	
	
	
	private void createSentencesAxis(final CTopicsMap topicsMap) {
		int totalNumSentences = 0,
		    index 			  = 0;
		
			/*
			 * Compute the total number of sentences..
			 */
		List<String[]> sentencesGroupsList = topicsMap.getSentencesGroupsList();
		for(String[] sentences : sentencesGroupsList) {
			totalNumSentences += sentences.length;
		}
		
		_sentenceAxis = new NSentenceIndex[totalNumSentences];
		
			/*
			 * Create the Text unit axis of data.
			 */
		for(String[] sentences : sentencesGroupsList) {
			for( String sentence : sentences) {
				_sentenceAxis[index] = new NSentenceIndex(sentence);
				index++;
			}
		}
	}
	
	
	
	
	private void createTopographyPoints(final CTopicsMap topicsMap, 
										Map<String, CTopicPoint> topicPointsMap) {
		CTopicPoint newTopicPoint = null;
		
			/*
			 * Create and collect topic points
			 */
		List<ATaxonomyNode> taxonomyPathsList = topicsMap.getTaxonomyNodesList();
		
		
		String nodeLabel = null;
		for( ATaxonomyNode node : taxonomyPathsList ) {
			nodeLabel = node.getLabel();
			newTopicPoint = new CTopicPoint(node);
			
			CFreqArray freqArray = topicsMap.updateSentenceIndices(node);
			if( freqArray != null ) {
				newTopicPoint.setSentencesIndexes(freqArray.getValues()); 
			}
			
			if( _topicsDistribution.containsKey(nodeLabel)) {
				newTopicPoint.setTopicIndex(_topicsDistribution.get(nodeLabel).intValue());
			}
			
			topicPointsMap.put(nodeLabel, newTopicPoint);
		}
		
		List<CGraph> graphsList = topicsMap.getGraphsList();
		
			/*
			 * Let's update all the taxonomy class (or sub-topics) associated
			 * to each graph.
			 */
		NTopicUpdateFunction updateFunction = null;
		String rootLabel = null;
		int topicIndex = -1;
		
		for( CGraph graph : graphsList) {
			rootLabel = graph.getRootLabel();
			if( topicPointsMap.containsKey(rootLabel)) {
				topicIndex =  _topicsDistribution.get(rootLabel).intValue();
			
				updateFunction = new NTopicUpdateFunction(topicIndex, topicPointsMap);
				graph.visit(updateFunction);
			}
		}
		
	}
	
}

// --------------------------  EOF ----------------------------------
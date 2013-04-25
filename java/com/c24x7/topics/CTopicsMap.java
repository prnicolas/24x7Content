/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.topics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CTaxonomyObject;
import com.c24x7.math.utils.CFreqArray;
import com.c24x7.topics.CTopicsExtractor.NTaxonomySentencesDistribution;
import com.c24x7.util.collections.CGraph;



		/**
		 * <p>Class that implements or describes the mapping between taxonomy 
		 * classes and document, paragraph and sentences.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 11, 2012 4:21:32 PM
		 */

public final class CTopicsMap {
	
		/**
		 * <p>Customized class for comparing Taxonomy Paths</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date July 11, 2012 10:36:03 PM
		 */
	public class NTaxonomyPathComparator implements Comparator<ATaxonomyNode[]> {
		
		@Override
		public int compare(ATaxonomyNode[] taxonomyPath1, ATaxonomyNode[] taxonomyPath2) {
			return taxonomyPath1[taxonomyPath1.length-1].getWeight() < taxonomyPath2[taxonomyPath2.length-1].getWeight() ? 1 : -1;
		}
	}

	private TreeMap<Float, TreeSet<ATaxonomyNode[]>> _taxonomyPathsMap 	= null;
	private Map<ATaxonomyNode, CFreqArray> 		_nodesSentencesMap 	= null;
	private List<CGraph>						_graphsList 		= null;
	private List<String[]>						_sentencesGroupList	= null;
	
		/**
		 * <p>Create a topics map for a specific document or text as a 
		 * collection of taxonomy tables.</p>
		 */
	public CTopicsMap() {
		_taxonomyPathsMap = new TreeMap<Float, TreeSet<ATaxonomyNode[]>>();
		_sentencesGroupList = new ArrayList<String[]>();
	}
	
	
	public final List<CGraph> getGraphsList() {
		return _graphsList;
	}

	
	
		/**
		 * <p>Add a taxonomy object to the taxonomy objects table of a document.</p>
		 * @param taxonomyObject object to be added to the table.
		 * @param topClass root class of the lineage containing the taxonomy object
		 * @param sentencesIndices  dynamic array of indices to sentences.
		 */
	public void addObject(	final CTaxonomyObject 	taxonomyObject, 
							final ATaxonomyNode[] 	taxonomyClasses,
							NTaxonomySentencesDistribution taxonomyDistribution) {
		
		CFreqArray sentencesIndices = taxonomyObject.getSentencesIndices();
		for(int j = 0; j < taxonomyClasses.length; j++) {
			taxonomyDistribution.add(taxonomyClasses[j], sentencesIndices);
		}
	}
	
	public void addObject( final CTaxonomyObject taxonomyObject, 
						  NTaxonomySentencesDistribution taxonomyDistribution) {
		
		CFreqArray sentencesIndices = taxonomyObject.getSentencesIndices();
		taxonomyDistribution.add(taxonomyObject, sentencesIndices);
	}
	
	public void addSentences(final String[] sentences) {
		_sentencesGroupList.add(sentences);
	}
	
	public List<String[]> getSentencesGroupsList() {
		return _sentencesGroupList;
	}
	
	
		/**
		 * <p>Generate topics map for this group of documents.</p>
		 * @param objectsMap map of taxonomy objects.
		 * @param mostRelevantTopClassesMap
		 * @param taxonomyDistribution
		 */
	public void generateTopicsMap(final Map<String, CTaxonomyObject> objectsMap, 
								  final Map<String, ATaxonomyNode> 	 mostRelevantTopClassesMap,
								  NTaxonomySentencesDistribution 	 taxonomyDistribution) {
		
		for( CTaxonomyObject taxonomyObject : objectsMap.values()) {
			addObject(taxonomyObject, mostRelevantTopClassesMap);
		}
		_nodesSentencesMap = new HashMap<ATaxonomyNode, CFreqArray>();
		
			/*
			 * Walk through the map of taxonomy path.
			 */
		CFreqArray sentencesIndices = null;
		for( SortedSet<ATaxonomyNode[]> nodesList : _taxonomyPathsMap.values() )  {
			
			for( ATaxonomyNode[] nodes : nodesList) {
				
				for( ATaxonomyNode node : nodes) {
					sentencesIndices = taxonomyDistribution.getSentencesIndices(node);
					_nodesSentencesMap.put(node, sentencesIndices);
				}
			}
		}
	}
		

	
	/**
	 * <p>Retrieve the indices of the sentences associate with a specific taxonomy node.</p>
	 * @param node taxonomy node for which the indices of sentences has to be retrieved
	 * @return array of indices of sentences associated with a specific taxonomy node
	 * @throws IllegalArgumentException exception if the taxonomy node is undefined (null).
	 */
	public CFreqArray getSentencesIndices(final ATaxonomyNode node) {
		if( node == null ) {
			throw new IllegalArgumentException("Cannot access sentences indices from undefined node");
		}
		return _nodesSentencesMap.get(node);
	}
	
	
		/**
		 * <p>Computer the absolute index of all the sentences associated with 
		 * this taxonomy class or topic.</p>
		 * @param node taxonomy node or topic for which the sentence index has to be converted in absolute value
		 * @throws IllegalArgumentException  if the taxonomy node is undefined
		 * @throws NullPointerException if the node does not have sentences indices associated to it.
		 */
	public CFreqArray updateSentenceIndices(final ATaxonomyNode node) {
		if( node == null ) {
			throw new IllegalArgumentException("Cannot access sentences indices from undefined node");
		}
		if( !_nodesSentencesMap.containsKey(node)) {
			throw new NullPointerException("node " + node.getLabel() + " does not have associated sentences");
		}
		
		CFreqArray freqArray = null;
		int textIndex = node.getTextIndex();
		if( _nodesSentencesMap.containsKey( node)) {
			freqArray = _nodesSentencesMap.get(node);
			if( freqArray != null) {
				freqArray.shiftValueBy(textIndex);
			}
		}
		
		return freqArray;
	}
	

		/**
		 * <p>Retrieve the list of taxonomy paths relevant to a group of topics.</p>
		 * @return list of set of taxonomy paths
		 */
	
	public List<Set<ATaxonomyNode[]>> getTaxonomyPathSets() {
		
		List<Set<ATaxonomyNode[]>> taxonomyPathSetsList = new ArrayList<Set<ATaxonomyNode[]>>();
		
		NavigableMap<Float, TreeSet<ATaxonomyNode[]>> descendingOrder = _taxonomyPathsMap.descendingMap();
		for(Set<ATaxonomyNode[]> taxonomyPathList : descendingOrder.values()) {
			taxonomyPathSetsList.add(taxonomyPathList);
		}
		
		return taxonomyPathSetsList;
	}
	
	
	public List<ATaxonomyNode> getTaxonomyNodesList() {
		List<ATaxonomyNode> taxonomyPathsList = new ArrayList<ATaxonomyNode>();
		
		List<Set<ATaxonomyNode[]>> taxonomyPathSetsList = getTaxonomyPathSets();
		
		for( Set<ATaxonomyNode[]> taxonomyPathSets : taxonomyPathSetsList) {
			for( ATaxonomyNode[] nodes : taxonomyPathSets) {
				for( ATaxonomyNode node : nodes ) {
					taxonomyPathsList.add(node);
				}
			}
		}
		
		return taxonomyPathsList;
	}
	


		/**
		 * <p>Test if the map or table contains at least one topic.</p>
		 * @return true is the table does not contain any topic, false otherwise
		 */
	public boolean isEmpty() {
		return (_taxonomyPathsMap.size() == 0);
	}
	
	public void setGraphsList(List<CGraph> graphsList) {
		_graphsList = graphsList;
	}
		
	/**
	 * <p>Display the content of non-transient hash tables containing
	 * the taxonomy paths and association with sentences indices.</p>
	 * @return textual description of a topics map.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("\n");
		buf.append("\n\nTaxonomy Path Distribution\n --------------------\n");

			/*
			 * Print the map of taxonomy paths
			 */
		NavigableMap<Float, TreeSet<ATaxonomyNode[]>> descendingOrder = _taxonomyPathsMap.descendingMap();

		for(SortedSet<ATaxonomyNode[]> taxonomyPathList : descendingOrder.values()) {
			buf.append(taxonomyPathList.first()[0].getLabel());
			buf.append("\n");
			
			/*
			 * Display the main taxonomy paths for the document
			 */
			for( ATaxonomyNode[] taxonomyPath : taxonomyPathList) {
				buf.append(taxonomyPath[0].getWeight());
				buf.append(": ");
				
				for( int k = 0; k < taxonomyPath.length-1; k++) {
					buf.append(taxonomyPath[k].getLabel());
					buf.append("/");
				}
				buf.append("   ");
				buf.append(taxonomyPath[taxonomyPath.length-1].getWeight());
				buf.append("\n");
			}
		}

		/*
		 * Print the distribution of taxonomy classes over the 
		 * different components of the document.
		 */
		if( _nodesSentencesMap != null ) {
			buf.append("\n\n");
			
			CFreqArray sentencesIndices = null;
			for( ATaxonomyNode node : _nodesSentencesMap.keySet()) {
				buf.append(node.getLabel());
				buf.append(":");
				if( _nodesSentencesMap.containsKey(node)) {
					sentencesIndices = _nodesSentencesMap.get(node);
					if( sentencesIndices != null ) {
						buf.append(sentencesIndices.toString());
					}
				}
				buf.append("\n");
			}
		}
		
		return buf.toString();
	}
	
	
							// ------------------------
							//  Private Methods
							// -----------------------
	
	
		/**
		 * <p>Add a taxonomy object to the taxonomy objects table of a document.</p>
		 * @param taxonomyObject object to be added to the table.
		 * @param mostRelevantTopClassesList  map of the most relevant top taxonomy classes.
		 */
	private void addObject(final CTaxonomyObject taxonomyObject, 
						  Map<String, ATaxonomyNode> mostRelevantTopClassesList) {
		
		if( taxonomyObject == null || mostRelevantTopClassesList == null) {
			throw new IllegalArgumentException("Cannot add undefined taxonomy objects or classes to the topics map");
		}
		
		List<ATaxonomyNode[]> taxonomyNodesList = taxonomyObject.getTaxonomyNodesList();
		
		if( taxonomyNodesList != null && taxonomyNodesList.size() > 0) {
			ATaxonomyNode[] taxonomyPath = null;
			String topClassLabel = null;
			
			for(ATaxonomyNode[] taxonomyClasses : taxonomyNodesList) {	
				topClassLabel = taxonomyClasses[0].getLabel();
				
				/*
				 * If the list of most relevant topics contains this 
				 * top level taxonomy class, 
				 */
				if( mostRelevantTopClassesList.containsKey(topClassLabel) ) {
					
					/*
					 * We avoid duplication of labels between the lowest taxonomy class 
					 * and the taxonomy object (or instance), as Wikipedia and WordNet
					 * do not enforce unique identification between classes and N-Grams
					 */
					if(taxonomyObject.compareTo(taxonomyClasses[taxonomyClasses.length-1]) != 0) {
						taxonomyPath = Arrays.copyOf(taxonomyClasses, taxonomyClasses.length+1);
						taxonomyPath[taxonomyClasses.length] = taxonomyObject;
						put(taxonomyClasses[0].getWeight(), taxonomyPath);
					}
					else {
						put(taxonomyClasses[0].getWeight(), taxonomyClasses);
					}
				
				}
			}
		}
	}
	
	
	
	private void put(final float weight, 
					 final ATaxonomyNode[] taxonomyPath) {
		
		Float weightInt = Float.valueOf(weight);
		if( _taxonomyPathsMap.containsKey(weightInt) ) {
			TreeSet<ATaxonomyNode[]> existingTaxonomyPath = _taxonomyPathsMap.get(weightInt);
			existingTaxonomyPath.add(taxonomyPath);
		}
		else {
			TreeSet<ATaxonomyNode[]> newTaxonomyPath = new TreeSet<ATaxonomyNode[]>(new NTaxonomyPathComparator());
			newTaxonomyPath.add(taxonomyPath);
			_taxonomyPathsMap.put(weightInt, newTaxonomyPath);
		}
	}

}

// -----------------------  EOF ---------------------------------------- 

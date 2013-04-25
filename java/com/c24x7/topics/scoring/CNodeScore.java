/**
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.topics.scoring;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CText;




	 /**
	 *<p> Class that extract the taxonomy nodes with the highest score. The process
	 * consists of selecting the top or root classes for local hierarchy or graph.
	 *</p>
	 * @author Patrick Nicolas
	 * @date June 1, 2012
	 */
public final class CNodeScore {
	private static final int DEFAULT_NUM_TOP_NODES = 10;
	
	/*
	private static class NRootClassesMap extends HashMap<String, Integer> {

		private static final long serialVersionUID = -5348102521668387191L;
		
		private static final int SHIFT = 9;
		private NRootClassesMap() { 
			super();
		}
		
		private Integer put(String label, String parentGroupIds) {
			int separatorIndex = parentGroupIds.indexOf(CEnv.FIELD_DELIM),
				parentId = Integer.parseInt(parentGroupIds.substring(0, separatorIndex)),
				grandParentId =  Integer.parseInt(parentGroupIds.substring(separatorIndex+1));
			
			int value = parentId + (grandParentId <<SHIFT);
			
			return super.put(label, Integer.valueOf(value));
		}
		
		
		private int distance(final String from, final String to) {
			if( from == null || to == null) {
				throw new IllegalArgumentException("Cannot compare and score undefined taxonomy nodes");
			}
			int compareResult = 3;
			
			if(containsKey(from) && containsKey(to)) {
				int fromInt = get(from).intValue();
				int toInt = get(to).intValue();
				
				if( (fromInt & SHIFT) == (toInt & SHIFT) ) {
					compareResult = 1;
				}
				else if( (fromInt >> SHIFT) == (toInt >> SHIFT)) {
					compareResult = 2;
				}
			}
			
			return compareResult;
		}
	}
	
	
	public static class NRootClassesExtractor implements IValueExtractor {
		private NRootClassesMap _rootClassesMap = null;
		
		private NRootClassesExtractor(NRootClassesMap rootClassesMap) {
			_rootClassesMap = rootClassesMap;
		}
		
		@Override
		public void extract(final String key, final String value) {
			_rootClassesMap.put(key, value);
		}

	}
	
	
	private static NRootClassesMap rootClasses = null;
	
	static {
		rootClasses = new NRootClassesMap();
		load();
	}
	*/
	
	/*
	public static class NRootClassesExtractor implements IValueExtractor {
		private NRootClassesMap _rootClassesMap = null;
		
		private NRootClassesExtractor(NRootClassesMap rootClassesMap) {
			_rootClassesMap = rootClassesMap;
		}
		
		@Override
		public void extract(final String key, final String value) {
			_rootClassesMap.put(key, value);
		}

	}
	*/

		/**
		 * <p>Class that implement the comparator for the taxonomy nodes. 
		 * The comparator is used to ordered the list of Taxonomy nodes
		 * (classes and instances) extracted from 
		 * a document by comparing their relative weights.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 05/23/2012
		 */
	protected static class NTaxonomyNodeComparator implements Comparator<String> {
		private Map<String, ATaxonomyNode> _map = null;
		
		private NTaxonomyNodeComparator(Map<String, ATaxonomyNode> map) {
			super();
			_map = map;
		}
		
		/**
		 * <p>Compare to taxonomy nodes defined by their labels or keys
		 * by their relative weights.</p>
		 * @param key1  Key of the first taxonomy node to be ordered
		 * @param key2  Key of the second taxonomy node 
		 * @return 1 if first taxonomy node has a lower weight -1 otherwise.
		 */
		@Override
		public int compare(String key1, String key2) {
			return _map.get(key1).getWeight() < _map.get(key2).getWeight() ? 1 : -1;
		}
	}
	
	private int _numTopNodes = DEFAULT_NUM_TOP_NODES;

	public CNodeScore() {
		this(DEFAULT_NUM_TOP_NODES);
	}
	
	public CNodeScore(int numTopNodes) { 
		_numTopNodes = numTopNodes;
	}

	
	protected final Map<String, Object> getRelevantClassesMap(final CText document) {
		Map<String, Object> topClassesMap = new HashMap<String, Object>();
		
		/*
		 * Order the taxonomy classes by their relative weight.
		 */
		Map<String, ATaxonomyNode> nodesMap = document.getClassesMap();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<String, ATaxonomyNode> lineagesTreeMap = new TreeMap(new NTaxonomyNodeComparator(nodesMap));
	
		for( ATaxonomyNode node : nodesMap.values()) {
			lineagesTreeMap.put(node.getLabel(), node);
		}
		
			/*
			 * Extract only a subset of classes with the highest weights
			 */
		int k = 0;
		for( String topClassStr : lineagesTreeMap.keySet()) {
			if( ++k >= _numTopNodes) {
				break;
			}
			topClassesMap.put(topClassStr, null);
		}

		return topClassesMap;
	}
	
	
	/**
	 * <p>Extract the top taxonomy classes from the document.</p>
	 * @param document document from which the taxonomy graph has been generated
	 * @return array of top taxonomy classes ordered by their relative weights.
	 */
	
	public final ATaxonomyNode[] score(final CText document) {
		if( document == null ) {
			throw new IllegalArgumentException("Cannot extract top classes from undefined document");
		}
		
		/*
		 * Order the taxonomy classes by their relative weight.
		 */
		Map<String, ATaxonomyNode> nodesMap = document.getClassesMap();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<String, ATaxonomyNode> lineagesTreeMap = new TreeMap(new NTaxonomyNodeComparator(nodesMap));
	
		for( ATaxonomyNode node : nodesMap.values()) {
			lineagesTreeMap.put(node.getLabel(), node);
		}
		
			/*
			 * Extract only a subset of classes with the highest weights
			 */

		ATaxonomyNode[] topClasses = new ATaxonomyNode[_numTopNodes];
		int k = 0;
		for( String topClassStr : lineagesTreeMap.keySet()) {
			if( k >= _numTopNodes) {
				break;
			}
			topClasses[k++] = nodesMap.get(topClassStr);
		}
		return topClasses;
	}
	

}

// ---------------------- EOF ---------------------------------------------------------------

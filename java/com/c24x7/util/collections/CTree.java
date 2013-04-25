// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.collections;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;


			/**
			 * <p>Class the implements a basic Direct Graph.</p>
			 *
			 * @author Patrick Nicolas
			 * @date 01/13/2012
			 */

public final class CTree {	
	protected static final String INDENTATION = "  ";

			/**
			 * <p>Class that implements a node for the ontology graph. A node is composed
			 * of a ontology element, its weight and the list of its children. For the
			 * sake of efficiency in execution, the children are referred through their 
			 * name. By default the weight is the level of the element in the taxonomy + 1.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 01/25/2012
			 */
	protected final class NNode  {
		protected List<String> 	_nextNodes = null;
		protected String 		_label = null;
		protected int 			_level = 0;
		protected float 		_weight = 0.0F;
		protected int			_frequency= 0;
		
			/**
			 * <p>Create an Ontology graph node for an ontology element and a weight.</p>
			 * @param element element of the taxonomy
			 * @param level level (or distance from the root element) of this element in the taxonomy
			 */
		protected NNode(final String label, int level) {
			_label = label;
			_level = level;
		}
		
		protected final boolean isLeaf() {
			return (_nextNodes == null);
		}
		
		protected final List<String> getNextNodes() {
			return _nextNodes;
		}
		
		public int getLevel() {
			return _level;
		}
		
		public void setLevel(int level) {
			_level = level;
		}
	
		
		protected NNode addChild(final String content) {
			if( _nextNodes == null) {
				_nextNodes = new LinkedList<String>();
			}
			_nextNodes.add(content);
			return new NNode(content, _level);
		}
		
		protected void setFrequency(int frequency) {
			_frequency = frequency;
		}
		
		protected void setWeight(float weight) {
			_weight = weight;
		}
		
		public float getWeight() {
			return _weight;
		}
				
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(_frequency);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_weight);
			
			return buf.toString();
		}
	}
	
	protected Map<String, NNode> 	_treeNodes = null;
	protected String 				_rootLabel = null;
	
	
			/**
			 * <p>Create a directed graph with a specified root label.</p>
			 * @param rootLabel label of the root or starting node of this graph.
			 */
	public CTree(final String rootLabel) {
		if( rootLabel == null || rootLabel.length() < 2) {
			throw new IllegalArgumentException("Cannot create a graph with unspecified root");
		}
		_rootLabel = rootLabel;
		_treeNodes = new HashMap<String, NNode>();
	}
	
		
	
		/**
		 * <p>Retrieve the size (or number of vertices) of this graph.</p>
		 * @return number of vertices.
		 */
	public final int size() {
		return _treeNodes.size();
	}
	

		/**
		 * <p>Retrieve the weight of a specified vertex.</p>
		 * @param vertexLabel label (or name) of the vertex
		 * @return weight as floating point value if the vertex is found, -1.0 otherwise
		 * @throw IllegalArgumentException if the vertex label is incorrect.
		 */
			
	public float getNodeWeight(final String nodeLabel) {
		if( nodeLabel == null || nodeLabel.length() < 2) {
			throw new IllegalArgumentException("Cannot retrieve weight of unspecified tree node");
		}
		
		float weight = -1.0F;
		if( _treeNodes.containsKey(nodeLabel)) {
			NNode node = _treeNodes.get(nodeLabel);
			weight = node.getWeight();
		}
		return weight;
	}

	
	public void add(final String[] nodes) {
		int[] frequencies = new int[nodes.length];
		float[] weights = new float[nodes.length];
		this.add(nodes, frequencies, weights);
	}
	
	
	
		/**
		 * <p>Add an array of vertices and weights in this directed graph.</p>
		 * @param vertices array of vertices (string)
		 * @param weights array of weights.
		 * @throw IllegalArgumentException if either vertices array or weights array are null 
		 * of if those array have different length.
		 */
	public void add(final String[] labels, int[] frequencies, float[] weights) {
		if( labels == null || weights == null || labels.length != weights.length) {
			throw new IllegalArgumentException("Cannot add element to a graph (inconsistent input)");
		}
		
		final int lastFieldIndex = labels.length -1;
		
		String nodeLabel = null;
		NNode node = null;
		int foundNodeIndex = -1;
		
			/*
			 * Start from the last element (or leaf) of the taxonomy path (or route)
			 */
		for(int k = lastFieldIndex; k >= 0; k--) {
			nodeLabel = labels[k];
			
				/*
				 * If found a node with the same ontology field
				 * append the subtree to it. 
				 * BUG: It should not apply to the leaf node.
				 */
			if( _treeNodes.containsKey(nodeLabel)) {
				foundNodeIndex = k;
				break;
			}
		}
			/*
			 * Update all the descendant of this node, then update the count for all the 
			 * parents..
			 */
		if( foundNodeIndex != -1) {
			node = _treeNodes.get(labels[foundNodeIndex]);
			String[] remainingNodes = new String[lastFieldIndex-foundNodeIndex];
			
			int cursor = foundNodeIndex;
			for( int j = 0; j < remainingNodes.length; j++) {
				remainingNodes[j] = labels[++cursor];
			}
			
				/*
				 * Create the subtree of descendants..
				 */
			NNode thisNode = node;
			
			for( int j = 0, l = foundNodeIndex +2; j < remainingNodes.length; j++, l++) {
				thisNode = node.addChild(remainingNodes[j]);
				thisNode.setWeight(weights[j]);
				thisNode.setLevel(l);
				thisNode.setFrequency(frequencies[j]);
				_treeNodes.put(remainingNodes[j], thisNode);
			}
			
				/*
				 * Update the weight in the parent node.
				 */
			for( int j = 0; j <= foundNodeIndex; j++) {
				/*
				 * If one of the element of a new taxonomy branch
				 * does not exist, added to the ontology.
				 */
				if( !_treeNodes.containsKey(labels[j])) {
					thisNode = new NNode(labels[j], j);
					_treeNodes.put(labels[j], thisNode);
				}
				else {
					thisNode = _treeNodes.get(labels[j]);
				}
				thisNode.setWeight(weights[j]);
				thisNode.setFrequency(frequencies[j]);
			}
		}
			/*
			 * If none of existing match exists, then create a root, most
			 * likely the ontology elements of the first NGram
			 */
		
		else {
			NNode beforeNode = null, newNode = null;
			/*
			 * Each node, representing an Ontology element has a relative
			 * position in the Ontology graph and a weight.
			 */
			for(int k = 0; k < labels.length; k++) {
				newNode = new NNode(labels[k], k+1);
				newNode.setWeight(weights[k]);
				newNode.setFrequency(frequencies[k]);
				
				_treeNodes.put(labels[k], newNode);

				if( newNode != null && beforeNode != null) {
					beforeNode.addChild(labels[k]);
				}
				beforeNode = newNode;
			}
		}
	}
	
	
			/**
			 * <p>Compute the best path in this directed graph using the
			 * local steepest gradient and generating a list of vertices.</p>
			 * @return a list of vertices.
			 */
	public List<String> getBestPathList() {
		List<String> nodesList = new LinkedList<String>();
		searchNextBestTreeNode(nodesList,_rootLabel);
		
		return nodesList;
	}
	
	
	/**
	 * <p>Compute the best path in this directed graph using the
	 * local steepest gradient and generating a string of vertices 
	 * separated by '/'</p>
	 * @return a best path as a string.
	 */
	public String getBestPathString() {
		StringBuilder buf = new StringBuilder();
		searchNextBestTreeNode(buf,_rootLabel);
		
		return buf.toString();
	}
	
	public String printNodes() {
		StringBuilder buf = new StringBuilder();
		for( NNode node : _treeNodes.values()) {
			buf.append("\n");
			buf.append(node.toString());
		}
		
		return buf.toString();
	}
	
		/**
		 * <p>Create a textual representation of graph using indentation.</p>
		 * @return string of all graph vertices..
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		if( _treeNodes.size() > 0) {
			Map<String, Object> visitedNodes = new HashMap<String, Object>();
			printChildren(_rootLabel, buf, visitedNodes);
		}		
		return buf.toString();
	}
	

	
					// -------------------------
					// Supporting Private Methods
					// --------------------------	
	
	
	protected void searchNextBestTreeNode(List<String> nodesLabelList, String nodeLabel) {

		NNode treeNode = _treeNodes.get(nodeLabel), nextTreeNode = null;
		String bestNodeLabel = null;
		
		if( !treeNode.isLeaf()) {
			List<String> graphNextNodes = treeNode.getNextNodes();
			float maxWeight = 0.0F;
			
			for( String nextNodeLabel : graphNextNodes) {
				nextTreeNode = _treeNodes.get(nextNodeLabel);
				if(nextTreeNode.getWeight() > maxWeight) {
					maxWeight = nextTreeNode.getWeight();
					bestNodeLabel = nextNodeLabel;
				}
			}	
			nodesLabelList.add(bestNodeLabel);			
			searchNextBestTreeNode(nodesLabelList,  bestNodeLabel);
		}
	}

	
	protected void searchNextBestTreeNode(StringBuilder routePath, String nodeLabel) {
		
		NNode treeNode = _treeNodes.get(nodeLabel), nextTreeNode = null;
		
		String bestNodeLabel = null;
		
		if( !treeNode.isLeaf()) {
			List<String> treeNextNodesLabels = treeNode.getNextNodes();
			float maxWeight = 0.0F;
			
			for( String treeNextNodeLabel : treeNextNodesLabels) {
				nextTreeNode = _treeNodes.get(treeNextNodeLabel);
				if(nextTreeNode.getWeight() > maxWeight) {
					maxWeight = nextTreeNode.getWeight();
					bestNodeLabel = treeNextNodeLabel;
				}
			}
		
			routePath.append("/");	
			routePath.append(bestNodeLabel);			
			searchNextBestTreeNode(routePath,  bestNodeLabel);
		}
	}
	
	public String printGraphDebug() {
		StringBuilder buf = new StringBuilder("\n\nTree Debug: ----------------");
		for( NNode node : _treeNodes.values() ) {
			buf.append("\n");
			buf.append(node.toString());
			buf.append("\n");
			if( !node.isLeaf() ) {
				for(String childLabel : node.getNextNodes()) {
					buf.append("    ");
					buf.append(childLabel);
					buf.append("\n");
				}
			}
		}
		
		return buf.toString();
	}
	
	protected void printChildren(final String nodeLabel, 
							   StringBuilder buf, 
							   Map<String, Object> visitedNodes) {
		
		if( !visitedNodes.containsKey(nodeLabel)) {
			NNode node = _treeNodes.get(nodeLabel);
			List<String> nextNodes = node.getNextNodes();
			
				/*
				 * otherwise move back up to the parent.
				 */
			final int indentation = node.getLevel()-1;
			
			for( int k = 0; k < indentation; k++) {
				buf.append(INDENTATION);
			}
			buf.append(node.toString());
			buf.append("\n");
			visitedNodes.put(nodeLabel, null);
			
				/*
				 * If this node has children, recurse the printing method.
				 */
			if( nextNodes != null ) {
				for( String nextNode : nextNodes) {
					printChildren(nextNode, buf, visitedNodes);
				}
			}
		}
	}
}

// -------------------------  EOF ---------------------------------

/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.util.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.c24x7.models.ATaxonomyNode;
import com.c24x7.nlservices.CTopographyService.IFunction;
import com.c24x7.util.CEnv;


	/**
	 * <p>Generic undirected graph class. The graph is composed of Vertex which carry also
	 * the weight of the edge from the previous vertices. The content structure of a Vertex
	 * is defined in run-time as an implementation of the interface INode.</p>
	 * 
	 * @author Patrick Nicolas         24x7c 
	 * @date June 27, 2012 4:49:27 PM
	 */
public final class CGraph {
	private static final String INDENTATION = "  ";
	
		/**
		 * <p>Generic interface for the content (or Node) of a vertex.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date June 27, 2012 4:50:04 PM
		 */
	public interface INode {
		public String getLabel();
		public int getLevel();
	}
	
		/**
		 * <p>Generic Vertex inner class for this undirected graph. The Vertex contains the
		 * following information: label, map of previous vertices, map of next vertices and 
		 * the weight (or level) of the vertex.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 27, 2012 4:50:35 PM
		 */
	public class NVertex {
		private String				_label = null;
		private Map<String, Object> _nextVertices = null;
		private Map<String, Object> _prevVertices = null;
		protected int 				_level = 0;
		
		private NVertex(final String label, int level) {
			_label = label;
			_level = level;
		}
			
		private int getLevel() {
			return _level;
		}
		
		private final String getLabel() {
			return _label;
		}
		private Map<String, Object> nextVertices() {
			return _nextVertices;
		}
				
		public int getNumChildren() {
			return (_nextVertices != null) ? _nextVertices.size() : 0;
		}

		public Set<String> getParentsSet() {
			return (_prevVertices != null) ? _prevVertices.keySet() : null;
		}
		
		private void setParent(final NVertex vertex) {
			if ( _prevVertices == null) {
				_prevVertices = new HashMap<String, Object>();
			}
			_prevVertices.put(vertex.getLabel(), vertex);
		}
				
		
		private void addNextVertex(final NVertex nextVertex, int numVertices) {
			if ( _nextVertices == null) {
				_nextVertices = new HashMap<String, Object>();
			}
			_nextVertices.put(nextVertex.getLabel(), null);
			nextVertex.setParent(this);
		}

		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(getLevel());

			return buf.toString();
		}
	}
	
	
	private Map<String, NVertex> _verticesMap 	= null;
	private String 				 _rootLabel 	= null;
	
	
		/**
		 * <p>Create a Graph with a specific root label. This constructor has
		 * to be used if the graph does not have a single first node (tree like).
		 * @param rootLabel label of the first or root Node.
		 */
	public CGraph(final String rootLabel) {
		_verticesMap = new HashMap<String, NVertex>();
		_rootLabel = rootLabel;
	}
	
	
	public final String getRootLabel() {
		return _rootLabel;
	}

	
		/**
		 * <p>Add an array of node or vertex content or vertex node. </p>
		 * @param nodes add an array of nodes or vertices content to this graph. 
		 * @throws IllegalArgumentException exception if the nodes are undefined.
		 */
	public void addVertices(final INode[] nodes) {
		if( nodes == null || nodes.length == 0) {
			throw new IllegalArgumentException("Cannot add undefined vertice to the graph");
		}
		
		NVertex vertex 		= null, 
				prevVertex 	= null, 
				lastNewVertex = null;
		String label = null;
		
		/*
		 * Insert the element of the array of nodes one by one.
		 */
		for( int level = 0; level < nodes.length; level++) {
				/*
				 * If this is a new Vertex, add it to the
				 * vertices map and link to the previous one
				 */
			label = nodes[level].getLabel();
			if( !_verticesMap.containsKey(label)) {
				vertex = new NVertex(nodes[level].getLabel(), level);
				
				if( prevVertex != null) {
					prevVertex.addNextVertex(vertex, nodes.length);
				}
				_verticesMap.put(label, vertex);
				lastNewVertex = vertex;
				prevVertex = vertex;
			}
		
			else {
				prevVertex = _verticesMap.get(label);
				if( lastNewVertex != null) {
					lastNewVertex.addNextVertex(prevVertex, nodes.length);
				}
				lastNewVertex = null;
			}
		}
	}
	
	public int getNumChildren(final String nodeLabel) {
		return (_verticesMap.containsKey(nodeLabel)) ?
				_verticesMap.get(nodeLabel).getNumChildren() :
				-1;
	}
	
	
	public Set<String> getParentsSet(final String nodeLabel) {
		return (_verticesMap.containsKey(nodeLabel)) ?
				_verticesMap.get(nodeLabel).getParentsSet() :
				null;
	}
	
	public double getParentsWeight(final String nodeLabel, Map<String, ATaxonomyNode> classesMap) {
		double totalParentWeight = 0.0;
		
		if( _verticesMap.containsKey(nodeLabel) ) {
			Set<String> parentsSet = _verticesMap.get(nodeLabel).getParentsSet();
		
			for(String parentLabel : parentsSet) {
				totalParentWeight += classesMap.get(parentLabel).getWeight();
			}
		}
		
		return totalParentWeight;
	}
	
	
	public void visit(IFunction function) {
		if( _verticesMap.size() > 0) {
			visit(_rootLabel, function);
		}
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		if( _verticesMap.size() > 0) {
			printChildren(_rootLabel, buf);
		}		
		return buf.toString();
	}
	
	
				// --------------------------
				// Private Supporting Methods
				// --------------------------
	
	private void printChildren(final String vertexLabel, StringBuilder buf) {
		
		NVertex vertex = _verticesMap.get(vertexLabel);			

		final int indentation = vertex.getLevel();
		for( int k = 0; k < indentation; k++) {
			buf.append(INDENTATION);
		}	
		buf.append(vertex.toString());
		buf.append("\n");
			
			/*
			* If this node has children, recurse the printing method.
			*/
		Map<String, Object> nextVertices = vertex.nextVertices();
			
		if( nextVertices != null ) {
			for( String nextVertice : nextVertices.keySet()) {
				printChildren(nextVertice, buf);
			}
		}
	}
	
	
	private void visit(final String vertexLabel, IFunction function) {
		
		NVertex vertex = _verticesMap.get(vertexLabel);	
		function.apply(vertexLabel, vertex.getLevel());
			
			/*
			* If this node has children, recurse the printing method.
			*/
		Map<String, Object> nextVertices = vertex.nextVertices();
			
		if( nextVertices != null ) {
			for( String nextVertice : nextVertices.keySet()) {
				visit(nextVertice, function);
			}
		}
	}


	
}
// --------------------  EOF ----------------------------------------
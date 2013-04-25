package com.c24x7.util.collections;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;


public final class CDirectedGraph {
	private static final String INDENTATION = "  ";
	
	private class NVertex {
		
		Map<String, Object> _nextVertices = null;
		protected String 	_label 		= null;
		protected int 		_level 		= 0;
		protected float 	_weight 	= 0.0F;
		
		private NVertex(final String label, float weight, int level) {
			_label = label;
			_level = level;
			_weight = weight;
		}
		
		private final String getLabel() {
			return _label;
		}
		
		private float getWeight() {
			return _weight;
		}
		
		private int getLevel() {
			return _level;
		}
		
		private Map<String, Object> nextVertices() {
			return _nextVertices;
		}
		
		
		private void addNextVertex(final NVertex nextVertex) {
			if ( _nextVertices == null) {
				_nextVertices = new HashMap<String, Object>();
			}
			_nextVertices.put(nextVertex.getLabel(), null);
		}

		
		private final NVertex nextSpanningVertex() {
			NVertex nextSpanningVertex = null;
			if(_nextVertices != null ) {
				float maxWeight = Float.MIN_VALUE;
				
				for( String nextVertexLabel : _nextVertices.keySet()) {
					nextSpanningVertex = _verticesMap.get(nextVertexLabel);
					if( maxWeight < nextSpanningVertex.getWeight()) {
						maxWeight = nextSpanningVertex.getWeight();
					}
				}
			}
			return nextSpanningVertex;
		}
		
		private final String nextSpanningVertexLabel() {
			NVertex nextSpanningVertex = nextSpanningVertex();
			return (nextSpanningVertex != null) ? nextSpanningVertex.getLabel() : null;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(_level);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_weight);
			
			return buf.toString();
		}
	}
	
	private Map<String, NVertex> _verticesMap 	= null;
	private String 				 _rootLabel 	= null;
	
	public CDirectedGraph(final String rootLabel) {
		_verticesMap = new HashMap<String, NVertex>();
		_rootLabel = rootLabel;
	}
	
	
	
	public void addVertices(final String[] labels, final float[] weights) {
		if( labels == null || weights == null || labels.length != weights.length) {
			throw new IllegalArgumentException("Cannot add undefined vertice to the graph");
		}
		
		NVertex vertex 			= null, 
				prevVertex 		= null, 
				lastNewVertex 	= null;
		
		for( int level = 0; level < labels.length; level++) {
				/*
				 * If this is a new Vertex, add it to the
				 * vertices map and link to the previous one
				 */
			if( !_verticesMap.containsKey(labels[level])) {
				vertex = new NVertex(labels[level], weights[level], level);
				if( prevVertex != null) {
					prevVertex.addNextVertex(vertex);
				}
				_verticesMap.put(labels[level], vertex);
				lastNewVertex = vertex;
				prevVertex = vertex;
			}
		
			else {
				prevVertex = _verticesMap.get(labels[level]);
				if( lastNewVertex != null) {
					lastNewVertex.addNextVertex(prevVertex);
				}
				lastNewVertex = null;
			}
		}
	}
	
	public void addVertices(final String[] nodes) {
		float[] weights = new float[nodes.length];
		
		this.addVertices(nodes, weights);
	}
	
	public List<String> getSpanningTreeLabels() {
		List<String> vertexLabels = new LinkedList<String>();
		nextSpanningLabel(vertexLabels, _rootLabel);
		
		return vertexLabels;
	}
	
	public List<NVertex> getSpanningTree() {
		List<NVertex> vertexLabels = new LinkedList<NVertex>();
		nextSpanningVertex(vertexLabels, _rootLabel);
		
		return vertexLabels;
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
	

	private void nextSpanningLabel(List<String> vertexLabels, String thisLabel) {
		vertexLabels.add(thisLabel);
		NVertex thisVertex = _verticesMap.get(thisLabel);
		String nextSpanningVertex = thisVertex.nextSpanningVertexLabel();	
		
		if(nextSpanningVertex != null) {
			nextSpanningLabel(vertexLabels, nextSpanningVertex);
		}
	}
	
	private void nextSpanningVertex(List<NVertex> vertexLabels, String thisLabel) {
		NVertex thisVertex = _verticesMap.get(thisLabel);
		vertexLabels.add(thisVertex);
	
		String nextSpanningVertex = thisVertex.nextSpanningVertexLabel();	
		if(nextSpanningVertex != null) {
			nextSpanningVertex(vertexLabels, nextSpanningVertex);
		}
	}
	
}
// --------------------  EOF ----------------------------------------
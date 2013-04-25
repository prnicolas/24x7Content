package com.c24x7.util.collections;


import java.util.HashMap;
import java.util.Map;


public class CSGraph {
	private static final String INDENTATION = "  ";
	
	public class NVertex {
		private Map<String, Object> _nextVertices = null;
		private String _label = null;
		protected int  _level = 0;
		
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
				

		
		private void addNextVertex(final NVertex nextVertex, int numVertices) {
			if ( _nextVertices == null) {
				_nextVertices = new HashMap<String, Object>();
			}
			_nextVertices.put(nextVertex.getLabel(), null);
		}

		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			buf.append("(");
			buf.append(getLevel());
			buf.append(")");

			return buf.toString();
		}
	}
	

	
	private Map<String, NVertex> _verticesMap 	= null;
	private String 				 _rootLabel 	= null;
	
	public CSGraph(final String rootLabel) {
		_verticesMap = new HashMap<String, NVertex>();
		_rootLabel = rootLabel;
	}
	
	
	/*
	public void orderVertices() {
	
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<String, NVertex> verticesTreeMap = new TreeMap(new NVertexComparator(_verticesMap));
	
		NVertex vertex = null;
		for( String vertexLabel : _verticesMap.keySet()) {
			vertex = _verticesMap.get(vertexLabel);
			if( vertex != null && vertex.getRelWeight() > 0.0F) {
				verticesTreeMap.put(vertexLabel, vertex);
			}
		}
		
	
		Collection<NVertex> verticesCollection = verticesTreeMap.values();
		NVertex[] topicVertices = verticesCollection.toArray(new NVertex[0]);
		int lastVertexIndex = topicVertices.length -1;
		
		for( int k = 0; k < lastVertexIndex; k++) {
			for( int j = 1; j <  topicVertices.length; j++) {
				
				if(topicVertices[k].getLevel() > topicVertices[j].getLevel()) {
					if( topicVertices[j].findParent(topicVertices[k]) ) {
						final String topicVertexLabel = topicVertices[j].getLabel();
						if( topicVertexLabel != null) {
							verticesTreeMap.remove(topicVertexLabel);
						}
					}
				}
			}
		}
	}
	*/

	
	
	
	public void addVertices(final String[] vertices) {
		if( vertices == null || vertices.length == 0) {
			throw new IllegalArgumentException("Cannot add undefined vertice to the graph");
		}
		
		NVertex vertex 			= null, 
				prevVertex 		= null, 
				lastNewVertex 	= null;
		
		for( int level = 0; level < vertices.length; level++) {
				/*
				 * If this is a new Vertex, add it to the
				 * vertices map and link to the previous one
				 */
			if( !_verticesMap.containsKey(vertices[level])) {
				vertex = new NVertex(vertices[level], level);
				if( prevVertex != null) {
					prevVertex.addNextVertex(vertex, vertices.length);
				}
				_verticesMap.put(vertices[level], vertex);
				lastNewVertex = vertex;
				prevVertex = vertex;
			}
		
			else {
				prevVertex = _verticesMap.get(vertices[level]);
				if( lastNewVertex != null) {
					lastNewVertex.addNextVertex(prevVertex, vertices.length);
				}
				lastNewVertex = null;
			}
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
}

// ----------------------------  EOF -----------------------------
/**
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.topics.scoring;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CTaxonomyObject;
import com.c24x7.models.CText;
import com.c24x7.models.taxonomy.CTaxonomyModel;



		/**
		 * <p>
		 * </p>
		 * @author Patrick Nicolas
		 * @date June 1, 2012
		 */
public class CLineageScore {
	private static final int DEFAULT_NUM_TOP_LINEAGE = 6;
	
	
	public interface IScore {
		public float score(float weight, final ATaxonomyNode tClass);
	}
	
	public static class NAddScore implements IScore {
		public NAddScore() { }
		
		public float score(float weight, final ATaxonomyNode tClass) {
			return weight + tClass.getWeight();
		}
	}
	
	
	public static class NNormalizeAddScore implements IScore {
		public float score(float weight, final ATaxonomyNode tClass) {
			return weight + tClass.getWeight()/tClass.getLevel();
		}
	}
	
	
	public class NRelevantLineage {
		private ATaxonomyNode[] _nodes = null;
		private CTaxonomyObject _instance = null;
		
		public NRelevantLineage(ATaxonomyNode[] nodes, CTaxonomyObject instance) {
			_nodes = nodes;
			_instance = instance;
		}
		
		public final CTaxonomyObject getInstance() {
			return _instance;
		}
		
		public final ATaxonomyNode[] getNodes() {
			return _nodes;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(CTaxonomyModel.convertClassesToLineage(_nodes));
			buf.append("/");
			buf.append(_instance.getLabel());
		
			return buf.toString();
		}
	}
	
	
	/**
	 * <p>Class that implement the comparator for the taxonomy lineages. 
	 * The comparator is used to ordered the list of Taxonomy lineages
	 * (classes & instances) extracted from a document by comparing 
	 * the total weight for all the classes composing the lineage.</p>
	 * 
	 * @author Patrick Nicolas
	 * @date 05/23/2012
	 */

	protected static class NLineageComparator implements Comparator<NRelevantLineage> {
		private Map<NRelevantLineage, Float> _map = null;
		
		private NLineageComparator(Map<NRelevantLineage, Float> map) {
			super();
			_map = map;
		}

		/**
		 * <p>Compare to taxonomy lineages defined by their labels or keys
		 * by their relative weights.</p>
		 * @param key1  Key of the first taxonomy node to be ordered
		 * @param key2  Key of the second taxonomy node 
		 * @return 1 if first taxonomy node has a lower weight -1 otherwise.
		 */
		@Override
		public int compare(NRelevantLineage key1, NRelevantLineage key2) {
			return _map.get(key1) <  _map.get(key2) ? 1 : -1;
		}
	}

	
	private int 	_numTopLineages = DEFAULT_NUM_TOP_LINEAGE;
	private IScore 	_scoreMethod 	= null;
	
	public CLineageScore() {
		this(DEFAULT_NUM_TOP_LINEAGE);
	}
	
	public CLineageScore(int numTopLineages) {
		this(numTopLineages, new NAddScore());
	}
	
	
	public CLineageScore(int numTopLineages, IScore scoreMethod) { 
		_numTopLineages = numTopLineages;
		_scoreMethod = scoreMethod;
	}
	
	
	
	/**
	 * <p>Extract the most taxonomy lineages from a specified document.</p>
	 * @param document document from which the taxonomy graph has been generated
	 * @return list of the most relevant taxonomy lineages ordered by their sum of the relative weights of each of its classes.
	 */
	public List<NRelevantLineage> score(final CText document) {
		if( document == null ) {
			throw new IllegalArgumentException("Cannot extract top lineages from undefined document");
		}
		
		Map<NRelevantLineage, Float> lineagesMap = new HashMap<NRelevantLineage, Float>();
		
		CNodeScore nodeScore = new CNodeScore(); 
		Map<String, Object> bestClassesMap = nodeScore.getRelevantClassesMap(document);

		
		Collection<CTaxonomyObject> instancesList =  document.getObjectsMap().values();
		
		List<ATaxonomyNode[]> nodesList = null;
		for( CTaxonomyObject instance : instancesList) {
			nodesList =  instance.getTaxonomyNodesList();
			
			if(nodesList != null) {
				float weight = 0.0F;
				for( ATaxonomyNode[] nodes : nodesList) {
					weight = 0.0F;
					for(int k =0; k < nodes.length; k++) {
						if(bestClassesMap.containsKey(nodes[k].getLabel())) {
							weight = _scoreMethod.score(weight, nodes[k]);
						}
					}
					
					/*
					 * Add the weight of this taxonomy instance only if 
					 * any classes of its lineages match the most relevant
					 * node labels.
					 */
					if( weight > 0.0F) {
						weight = _scoreMethod.score(weight, instance);
						lineagesMap.put(new NRelevantLineage(nodes, instance), new Float(weight/nodes.length));
					}
				}
			}
		}
		
		/*
		 * Rank the best taxonomy lineages..
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TreeMap<NRelevantLineage, Float> lineagesTreeMap = new TreeMap(new NLineageComparator(lineagesMap));
		for(NRelevantLineage key : lineagesMap.keySet()) {
			lineagesTreeMap.put(key, lineagesMap.get(key));
		}
				
		
		List<NRelevantLineage> bestLineages = new LinkedList<NRelevantLineage>();
		Map<String, String> lineagesDuplicates = new HashMap<String, String>();
		int k = 0;
		for(NRelevantLineage relevantLineage : lineagesTreeMap.keySet()) {
			if( k >= _numTopLineages) {
				break;
			}
			final String bestLineageStr = CTaxonomyModel.convertClassesToLineage(relevantLineage.getNodes());
			
			if( !lineagesDuplicates.containsKey(bestLineageStr)) {
				lineagesDuplicates.put(bestLineageStr, null);
				bestLineages.add(relevantLineage);
				k++;
			}
		
		}
			
		return bestLineages;
	}
}

// ---------------------  EOF ---------------------------------------------
/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.util.CEnv;






		/**
		 * <p>Class that defines a document document. A document document is composed
		 * of a text content, a state, a list of N-Grams prioritized by relative 
		 * frequency and a list of taxonomy branches associated with those N-Grams.</p>
		 * @author Patrick Nicolas
		 * @date 02/10/2012
		 */
public class CText {
	
		/**
		 * <p>Implements the Document document analysis life cycle. The enumeration
		 * defines the state of completion of specific NLP or semantics operations.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 01/11/2012
		 */
	public enum E_STATES {
		NONE(0), NGRAMS(1), TAXONOMY(2), TOPICS(3), COMPLETE(4), ERROR(5);
		
		protected int _state = -1;
		
		E_STATES(int state) {
			_state = state;
		}
		public int getState() {
			return _state;
		}
	}

	private String		_title			= null;
	private String[]	_sentences		= null;
	private E_STATES	_state 			= E_STATES.NONE;
	private float		_maxClassWeight = 0;
	
	private Map<String, CTaxonomyObject>  _instancesMap 	= null;
	private Map<String, ATaxonomyNode> 	  _classesMap 	= null;


	public CText() {
		this(null);
	}
	
	
		/**
		 * <p>Create a Document document for a text content.</p>
		 * @param content text to be analyzed and classified.
		 */
	public CText(final String title) {
		_title = title;
		_classesMap = new HashMap<String, ATaxonomyNode>();
	}
	
	

	public final String getTitle() {
		return _title;
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	public void setState(E_STATES state) {
		_state = state;
	}
	
	public final E_STATES getState() {
		return _state;
	}
	
	public final String[] getSentences() {
		return _sentences;
	}
	
	public void setSentences(final String[] sentences) {
		_sentences = sentences;
	}
	

	
	public int getNumSentences() {
		return _sentences.length;
	}
	
	public void setObjectsMap(Map<String, CTaxonomyObject> taxonomyObjectsMap)  {
		_instancesMap = taxonomyObjectsMap;
	}
	

	public final Map<String, ATaxonomyNode> getClassesMap() {
		return _classesMap;
	}
	
	
	public final ATaxonomyNode getClass(final String label) {
		return _classesMap.get(label);
	}
			
	
			/**
			 * <p>Add an array of taxonomy classes specified by their labels 
			 * to this document document.</p>
			 * @param taxonomyClassesLabels array of labels of taxonomy classes.
			 * @return array of taxonomy classes.
			 */
	public void addTaxonomyClasses(	final CTaxonomyObject 	instance, 
									final String[] 			taxonomyClassesLabels) {
		
		if( instance == null || taxonomyClassesLabels == null || taxonomyClassesLabels.length == 0) {
			throw new IllegalArgumentException("Cannot add null taxonomy instances to a document");
		}
		
		ATaxonomyNode[] taxonomyNodes = new ATaxonomyNode[taxonomyClassesLabels.length];
		ATaxonomyNode thisClass = null;
			
		short level = 0;
		for( String taxonomyClassLabel : taxonomyClassesLabels) {
			if(_classesMap.containsKey(taxonomyClassLabel)) {
				thisClass = _classesMap.get(taxonomyClassLabel);
			}
			else {
				thisClass = new CTaxonomyClass(taxonomyClassLabel);
				thisClass.setLevel(level);
				_classesMap.put(taxonomyClassLabel, thisClass);
			}
			taxonomyNodes[level++] = thisClass;
		}
		instance.addTaxonomyClasses(taxonomyNodes);
	}



	

			/**
			 * <p>Add an array of taxonomy classes specified by their labels 
			 * to this document document.</p>
			 * @param taxonomyClassesLabels array of labels of taxonomy classes.
			 * @return array of taxonomy classes.
			 */
	public void addTaxonomyClassesAndFields(final CTaxonomyObject 	instance, 
											final String[] 			taxonomyClassesLabels) {
		
		if( instance == null || taxonomyClassesLabels == null || taxonomyClassesLabels.length == 0) {
			throw new IllegalArgumentException("Cannot add null taxonomy instances to a document");
		}
		
		ATaxonomyNode[] taxonomyNodes = new ATaxonomyNode[taxonomyClassesLabels.length];
		String[] classFields = null,
		         multipleFieldsClass = null;
		
		int levelClassFields = -1;
		String taxonomyClassLabel = null;
		
		short level = 0;
		for( int k =0; k < taxonomyClassesLabels.length; k++) {
			taxonomyClassLabel = taxonomyClassesLabels[k];
		
			if( k < taxonomyClassesLabels.length -1) {
				taxonomyNodes[level++] =  updateClassesMap(taxonomyClassLabel, level);
			}
			
			else {
				multipleFieldsClass = null;
				classFields = extractFields(taxonomyClassLabel);
				
				if( classFields != null) {
					levelClassFields = level;
					multipleFieldsClass = classFields;
					
					for(String classField : classFields) {
						updateClassesMap(classField, level);
					}
					
					if( levelClassFields >= 0 ) {
						ATaxonomyNode[] derivedTaxonomyLineage = null;
						for(int i = 1; i < multipleFieldsClass.length; i++) {
							
							derivedTaxonomyLineage = new ATaxonomyNode[taxonomyNodes.length];
							
							for( int j = 0; j < taxonomyNodes.length; j++) {
								derivedTaxonomyLineage[j] = (taxonomyNodes[j] == null)  ?
															_classesMap.get(multipleFieldsClass[i]) :
															taxonomyNodes[j];
							}
							instance.addTaxonomyClasses(derivedTaxonomyLineage);
						}

						/*
						 * Insert the first field or synonym into the existing
						 * taxonomy lineage..
						 */
						 
						int i = 0;
						for( ; i < taxonomyNodes.length; i++) {
							if(taxonomyNodes[i] == null) {
								taxonomyNodes[i] = _classesMap.get(multipleFieldsClass[0]);
							}
						}
					}
				}
				else {
					taxonomyNodes[level++] = updateClassesMap(taxonomyClassLabel, level);
				}
			}
		}
		instance.addTaxonomyClasses(taxonomyNodes);	
	}
	
	
	private ATaxonomyNode updateClassesMap(final String taxonomyClassLabel, short level) {
		ATaxonomyNode thisClass = null;
		
		if(_classesMap.containsKey(taxonomyClassLabel)) {
			thisClass = _classesMap.get(taxonomyClassLabel);
		}
		else {
			thisClass = new CTaxonomyClass(taxonomyClassLabel);
			thisClass.setLevel(level);
			_classesMap.put(taxonomyClassLabel, thisClass);
		}

		return thisClass;
	}
		
	
	private String[] extractFields(final String classLabel) {
		String[] fields = null;
		int fieldSeparatorIndex = classLabel.indexOf(CEnv.FIELD_DELIM);
		if(fieldSeparatorIndex != -1) {
			fields = classLabel.split(CEnv.FIELD_DELIM);
		}
		
		return fields;
	}

	
	public final Collection<CTaxonomyObject> getObjectsList() {
		return ( _instancesMap != null) ? _instancesMap.values() : null;
	}
	
	public final Map<String, CTaxonomyObject> getObjectsMap() {
		return _instancesMap;
	}
	
		/**
		 * <p>Retrieve the list of Taxonomy branch (or list of array of
		 * Taxonomy Classes in this document document.</p>
		 * @return list of array of taxonomy classes if the analysis is successful, null otherwise. 
		 */
	public final List<ATaxonomyNode[]> getTaxonomyClassesList() {
		List<ATaxonomyNode[]> documentClassesList = null;
		
		if( _instancesMap != null) {
			documentClassesList = new LinkedList<ATaxonomyNode[]>();
			
			for(CTaxonomyObject taxonomyObject : _instancesMap.values()) {
				
				List<ATaxonomyNode[]> taxonomyClassesList = taxonomyObject.getTaxonomyNodesList();
				if( taxonomyClassesList != null) {
					for( ATaxonomyNode[] taxonomyClasses : taxonomyClassesList) {
						documentClassesList.add(taxonomyClasses);
					}
				}
			}
		}
		
		return documentClassesList;
	}
	
	
	public final int getTaxonomyClassOrder(final String label) {
		int order = -1;
		
		if(_classesMap.containsKey(label) ) {
			
			if( _instancesMap != null) {	
				String tClassLabel = null;
				/*
				 * Walk through the list of taxonomyObjects
				 */
				for(CTaxonomyObject taxonomyObject : _instancesMap.values()) {
					List<ATaxonomyNode[]> taxonomyClassesList = taxonomyObject.getTaxonomyNodesList();
					
					/*
					 * Walk through the list taxonomy lineages associated with this taxonomyObject
					 */
					if( taxonomyClassesList != null) {
						for( ATaxonomyNode[] taxonomyClasses : taxonomyClassesList) {
							
							/*
							 * Walk through each taxonomy lineage of this taxonomyObject
							 */
							for(int k = 0; k < taxonomyClasses.length; k++) {
								tClassLabel = taxonomyClasses[k].getLabel();
								/* 
								 * Return the order of the class if match is found,
								 * the order of a taxonomy class start with the
								 * value 2 for the lowest class or hyponym as taxonomyObject
								 * or taxonomy instance have the default value of 1.
								 */
								if(tClassLabel != null && tClassLabel.compareTo(label)==0) {
									order = taxonomyClasses.length - k+1;
									return order;
								}
							}
						}
					}
				}
			}
		}
		
		return order;
	}

	
	
		/**
		 * <p> Normalize each taxonomyObject noun if this document document.</p>
		 */
	public void normalize() {
		float maxWeight = 0.0F;
		
		/*
		 * Compute the maximum weight for all the taxonomyObjects.
		 */
		float maxTaxonomyWeight = 0;
		for( CTaxonomyObject taxonomyObject : _instancesMap.values() ) {
			if( maxWeight < taxonomyObject.getWeight()) {
				maxWeight = taxonomyObject.getWeight();
			}
		}
		/*
		 * Propagate the weight along all the taxonomy classes 
		 * associated with each of the taxonomyObjects in this document document..
		 */
		for( CTaxonomyObject taxonomyObject : _instancesMap.values()) {
			maxTaxonomyWeight = taxonomyObject.applyKirchoff(maxWeight);
			if( maxTaxonomyWeight > _maxClassWeight) {
				_maxClassWeight = maxTaxonomyWeight;
			}
		}
		
		for( ATaxonomyNode taxonomyClass : _classesMap.values()) {
			taxonomyClass.normalize(_maxClassWeight);
		}
	}


		/**
		 * <p>Test if the analysis of this document document has been completed.</p>
		 * @return true if the analysis was completed, false otherwise.
		 */
	public boolean analysisCompleted() {
		return (_state == E_STATES.TOPICS);
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("\n\n");		
		
		if( _instancesMap != null) {
			buf.append("\nComposite map:");
			for( CTaxonomyObject taxonomyObject : _instancesMap.values()) {
				buf.append("\n\n");
				buf.append(taxonomyObject.printTopics());
			}
		}
		
		return buf.toString();
	}
	
	
	/**
	 * <p>Print the list of taxonomy instances label extracted from the document
	 * or document document.</p>
	 * @return text description of the list of taxonomy instances labels.
	 */
	public String printTaxonomy() {
		StringBuilder buf = new StringBuilder("\n\n");		
		
		if( _instancesMap != null) {
			buf.append("\nComposite map:");
			for( CTaxonomyObject taxonomyObject : _instancesMap.values()) {
				buf.append("\n\n");
				buf.append(taxonomyObject.printDetails());
			}
		}
		
		return buf.toString();
	}
	
	/*
	public String printGraph() {
		StringBuilder buf = new StringBuilder("\n\n");
		CTopicsExtractor documentOntology = new CTopicsExtractor();
		buf.append(documentOntology.createGraph(this).toString());
		
		return buf.toString();
	}
	*/

}

// ------------------------------------ EOF ---------------------------------------

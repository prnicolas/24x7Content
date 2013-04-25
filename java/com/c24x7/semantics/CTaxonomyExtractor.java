//  Copyright (C) 2010-2012  Patrick Nicolas
package com.c24x7.semantics;

import com.c24x7.models.CTaxonomyObject;
import com.c24x7.models.CText;

import com.c24x7.semantics.CTaxonomyConnectionsPool.NTaxonomiesConn;
import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;


				/**
				 * <p>Class to extract and query the content of dbpedia unstructured data.
				 * @author Patrick Nicolas
				 * @date 07/19/2011
				 */  
public class CTaxonomyExtractor {
	public final static String 	DBPEDIA_LABELS_FILE 		= CEnv.configDir + "/dbpedia/labels_en.nt";
	public final static String 	DBPEDIA_ROOT_RESOURCE_URL 	= "<http://dbpedia.org/resource/";	
	public final static String 	DBPEDIA_DATASET_COUNTER_FILE = CEnv.configDir + "/dbpedia/counter";

	private static final float 	NGRAMS_SELECT_RATIO_PARAM	= 0.35F;
	
			/**
			 * <p>Structure for the dbpedia/wikipedia information. At a minimum a DBpedia entry
			 * should contains a label and short abstract.</p>
			 * @author Patrick Nicolas
			 * @date 07/18/2011
			 */
	private class NLabelTaxonomy {
		private String 	 _label = null;
		private Map<String, Object> _taxonomyLineages = null;
		
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);

			if( _taxonomyLineages != null) {
				buf.append("\nO:");
				for(String taxonomyLineage : _taxonomyLineages.keySet()) {	
					buf.append(taxonomyLineage);
					buf.append("\n");
				}
			}
			
			return buf.toString();
		}
		

		
		private NLabelTaxonomy(final String label) {
			_label = label;		
		}

		private boolean extract(final CText document, 
								CTaxonomyObject taxonomyInstance)  throws SQLException {
			return getElements(taxonomyInstance) && update(document, taxonomyInstance);
		}
		
		
		private boolean update(final CText document, CTaxonomyObject taxonomyInstance) {
			boolean success = false;
			
			if( _taxonomyLineages != null) {
				String[] taxonomyClasses = null;
				
				/*
				 * Extract the lower classes (bottom hierarchy of
				 * WordNet hypernyms) from the lineages extracted from Dbpedia.
				 */
				for( String taxonomyLineage : _taxonomyLineages.keySet()) {
					taxonomyClasses = taxonomyLineage.split("/");
					
					/*
					 * Add the taxonomy lineages to the document and 
					 * update the list of taxonomy classes associated with this taxonomyInstance.
					 */
					if( taxonomyClasses != null) {
						if( _taxonomyClassBreakdown )  {
							document.addTaxonomyClassesAndFields(taxonomyInstance, taxonomyClasses);
						}
						else {
							document.addTaxonomyClasses(taxonomyInstance, taxonomyClasses);
						}
						success = true;
					}
				}
			}
			
			return success;
		}
		
				/**
				 * <p>Extract the dbpedia entry from the database according to a predefined label.</p>
				 * @param taxonomyInstance taxonomyInstance for which the taxonomy needs to be extracted.
				 * @return true if at least one element is extracted.
				 * @throws SQLException if the query fails...
				 */
		private boolean getElements(final CTaxonomyObject taxonomyInstance) throws SQLException {
			final Character recordType = taxonomyInstance.getType();
				
				/*
				 * If this is a redirects (or dbpedia entry alias) 
				 */
			String encodedLabel = CStringUtil.encodeLatin1(_label);
			if( encodedLabel != null ) {
				String[] taxonomyLineages = (isAliasRecord(recordType) ) ?
											retrieveTaxonomies(CTaxonomyConnectionsPool.ALIAS_TABLE, encodedLabel) :
											retrieveTaxonomies(CTaxonomyConnectionsPool.ENTRY_TABLE,encodedLabel);
				
								
				/*
				 * If one or more taxonomy lineages have been found..
				 */
				if( taxonomyLineages != null) {
					if( _taxonomyLineages == null) {
						_taxonomyLineages = new HashMap<String, Object>();
					}
					for(int k = 0; k < taxonomyLineages.length; k++) {
						if(taxonomyLineages[k].length() > 1) {
							_taxonomyLineages.put(taxonomyLineages[k], null);
						}
					}
				}
			}
			
			return (_taxonomyLineages != null && _taxonomyLineages.size() > 0);
		}
		
		
		private String[] retrieveTaxonomies(int index, 
											final String encodedLabel) throws SQLException {
			String[] taxonomyLineages = null;
			
			_taxonomyConnection.getPreparedStmt(index).set(1, encodedLabel);
			ResultSet rs = _taxonomyConnection.getPreparedStmt(index).query();
			if(rs.next() ) {
				taxonomyLineages = _taxonomyConnection.retrieve(rs);
			}

			return taxonomyLineages;
		}
	}
	
	
	private NTaxonomiesConn _taxonomyConnection = null;
	private boolean			_taxonomyClassBreakdown = true;

	
		/**
		 * <p>Create taxonomy extraction instance with a preallocated
		 * pool of JDBC connections.
		 * @param taxonomyConnection predefined pool of JDBC connections
		 */
	public CTaxonomyExtractor(NTaxonomiesConn taxonomyConnection) { 
		this(taxonomyConnection, true);
	}

	
	
		/**
		 * <p>Create taxonomy extraction instance with a preallocated
		 * pool of JDBC connections.
		 * @param taxonomyConnection predefined pool of JDBC connections
		 */
	public CTaxonomyExtractor(NTaxonomiesConn taxonomyConnection, boolean taxonomyClassBreakdown) { 
		_taxonomyConnection = taxonomyConnection;
		_taxonomyClassBreakdown = taxonomyClassBreakdown;
	}
	
	


	
	

		/**
		 * <p>Main method to extract ontology, label, summary information 
		 * from Dbpedia database to update the text model.</p>
		 * @param textClassifier  model or classifier for the text or content
		 * @return true if Wikipedia records can be acceded, false otherwise.
		 */
	public boolean extract(CText document) {
		if( document == null) {
			throw new IllegalArgumentException("Cannot extract taxonomy from undefined document");
		}
		
		boolean success = false;
		Map<String, CTaxonomyObject> taxonomyInstancesMap = document.getObjectsMap();
		
			/*
			 * We extract the semantic definition of the taxonomyInstances only
			 * if any N-Grams have been found..
			 */
		if( taxonomyInstancesMap != null && taxonomyInstancesMap.size() > 0 ) {
		
			NLabelTaxonomy semanticLabel = null;	
			Map<String, CTaxonomyObject> reducedTaxonomyInstancesMap = new HashMap<String, CTaxonomyObject>(); 
			
			/*
			 * Walk through the list of taxonomyInstances to extract the taxonomy
			 * lineages associated with the label or N-Gram
			 */
			double maxWeight = -1.0;

			for( CTaxonomyObject taxonomyInstance  : taxonomyInstancesMap.values()) {	
				semanticLabel = new NLabelTaxonomy(taxonomyInstance.getLabel());
							
				try {
					if( semanticLabel.extract(document, taxonomyInstance) ) {
						if( maxWeight < 0.0) {
							maxWeight = taxonomyInstance.getWeight();
						}
						if(taxonomyInstance.getWeight() < NGRAMS_SELECT_RATIO_PARAM * maxWeight) {
							break;
						}
						reducedTaxonomyInstancesMap.put(taxonomyInstance.getLabel(), taxonomyInstance);
					}
				}
				catch (SQLException e) {
					CLogger.error("Cannot extract semantic for " + taxonomyInstance.getLabel() + " " + e.toString());
				}
			}
			
			document.setObjectsMap(reducedTaxonomyInstancesMap);
			
				/*
				 * Update the list of taxonomyInstance nouns for the document
				 * document, then normalize the frequencies and weights.
				 */
			success = reducedTaxonomyInstancesMap.size() > 0;
			if( success) {
				document.normalize();	
				document.setState(CText.E_STATES.TAXONOMY);
			}
		}		

		return success;
	}	
	
	

	
								// ----------------------------
								//  Private Supporting Methods
								// ---------------------------

	private static boolean isAliasRecord(Character type) {
		return (type.charValue() == CLookup.DPBEDIA_ENTRY_ALIAS);
	}
}

// ------------------------------- EOF ----------------------------------------------
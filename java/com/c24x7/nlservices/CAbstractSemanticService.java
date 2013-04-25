/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.nlservices;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CTaxonomyObject;
import com.c24x7.models.CText;
import com.c24x7.models.taxonomy.CTaxonomyModel;
import com.c24x7.semantics.CTaxonomyConnectionsPool;
import com.c24x7.semantics.CTaxonomyExtractor;
import com.c24x7.semantics.CTaxonomyConnectionsPool.NTaxonomiesConn;
import com.c24x7.textanalyzer.CNGramsExtractor;



public final class CAbstractSemanticService {
	private static Map<String, Object> actionVerbs = new HashMap<String, Object>();
	static {
		actionVerbs.put("is", null);
		actionVerbs.put("was", null);
		actionVerbs.put("were", null);
		actionVerbs.put("are", null);
	}
	
	
	protected static class NLineageComparator implements Comparator<String> {
		
		private Map<String, Integer> _map = null;
		protected NLineageComparator(Map<String, Integer> map) {
			super();
			_map = map;
		}
		
		/**
		 * <p>compare two Composite using their computed, normalized weights.
		 * This method is called for the ranking of the key taxonomyInstance nouns 
		 * in a document.</p>
		 * @param key1 content of the taxonomyInstance noun to compare from
		 * @param key2 content of the taxonomyInstance noun to compare to
		 */
		@Override
		public int compare(String key1, String key2) {
			return (_map.get(key1).intValue() < _map.get(key2).intValue()) ? 1 : -1;
		}
	}


	private NTaxonomiesConn _taxonomyConnection 	= null;
	private int				_numTaxonomyObjects 	= -1;
	private List<String>	_firstSentenceLineages 	= null;
	private String			_wordnetLineage 		= null;
	private String[]		_categoriesLineages 	= null;
	private String[]		_newTaxonomyLineages	= null;
	
	
	public  CAbstractSemanticService(int numTaxonomyObjects) throws SemanticAnalysisException {
		_numTaxonomyObjects = numTaxonomyObjects;
		_taxonomyConnection = CTaxonomyConnectionsPool.getInstance().getLabelsConnection();

	}
	
	public void setTaxonomyLineages(String 	wordnetLineage,
									String[] categoriesLineages) {
		_wordnetLineage = wordnetLineage;
		_categoriesLineages = categoriesLineages;
	}
	
	
	
	public final String[] getNewTaxonomyLineages() {
		return _newTaxonomyLineages;
	}
	
	
	/**
	 * <p>Extract the semantic components of a document using NLP algorithm to extract
	 * semantic N-Grams, WordNet hypernyms and dbpedia entries.</p>
	 * @param inputText input document
	 * @param title title of the document.
	 * @return the model of the text to be analyzed.
	 */
	public CText execute(final String inputText, final String title) {
		CText document = extract(inputText, title);
		_newTaxonomyLineages = null;
		
		_firstSentenceLineages = new LinkedList<String>();
		if(  document.getObjectsMap() != null &&  document.getObjectsMap().size() > 0) {
			int k = 0;
			for( CTaxonomyObject taxonomyObject : document.getObjectsMap().values()) {
				if( title.toLowerCase().compareTo(taxonomyObject.getLabel().toLowerCase()) != 0) {
					List<ATaxonomyNode[]> lineagesList = taxonomyObject.getTaxonomyNodesList();
					
					if( lineagesList != null ) {
						for(ATaxonomyNode[] lineage : lineagesList) {
							String lineageStr = CTaxonomyModel.convertClassesToLineage(lineage);
							_firstSentenceLineages.add(lineageStr);
						}
						k++;
					}
				}
				if(k > 1) {
					break;
				}
			}
		}
		
		Set<String> orderedCategoryLineages = resolveCategoriesLineages();
		String newTaxonomy  = resolveWordnetLineage();

		int finalLineageSetSize = orderedCategoryLineages.size();
		if(finalLineageSetSize > 3) {
			finalLineageSetSize = 3;
		}
		
		if( newTaxonomy != null ) {
			int k = 0;
			for(String lineage : orderedCategoryLineages) {
				if( k >= finalLineageSetSize || (lineage.compareTo(newTaxonomy) == 0)) {
					break;
				}
				k++;
			}
		
			if( k < finalLineageSetSize) {
				_newTaxonomyLineages =  new String[finalLineageSetSize];
				_newTaxonomyLineages[0] = newTaxonomy;
				int j = 0,
				    newIndex = 1;
				
				for(String lineage : orderedCategoryLineages) {
					if(j >= finalLineageSetSize) {
						break;
					}
					if(j != k) {
						_newTaxonomyLineages[newIndex] = lineage;
						newIndex++;
					}
					j++;
				}
			}
			else {
				_newTaxonomyLineages =  new String[finalLineageSetSize+1];
				int j = 0;
				for(String lineage : orderedCategoryLineages) {
					if(j >= finalLineageSetSize) {
						break;
					}
					_newTaxonomyLineages[j] = lineage;
					j++;
				}
				_newTaxonomyLineages[finalLineageSetSize] = newTaxonomy;
			}
		}
		else {
			_newTaxonomyLineages = orderedCategoryLineages.toArray(new String[0]);
		}
		
		return ( _newTaxonomyLineages != null) ? document : null; 
	}
	
	
	
	
	private Set<String> resolveCategoriesLineages() {		
		Map<String, Integer> finalLineagesMap = new HashMap<String, Integer>();
		
		int count = 0;
		for( String categoryLineages : _categoriesLineages ) {
			count = 0;
			for( String firstLineage : _firstSentenceLineages) {
				String[] firstTaxonomyClasses = firstLineage.split("/");
				for( String firstTaxonomyClass : firstTaxonomyClasses) {
					if( categoryLineages.indexOf(firstTaxonomyClass) != -1) {
						count++;
					}
				}
			}
		
			for( String otherSubLineage : _firstSentenceLineages) {
				if( categoryLineages.compareTo(otherSubLineage) != 0) {
					String[] otherSubClasses = otherSubLineage.split("/");
					for( String otherSubClass : otherSubClasses) {
						if( categoryLineages.indexOf(otherSubClass) != -1) {
							count++;
						}
					}
				}
			}
			
			finalLineagesMap.put(categoryLineages, Integer.valueOf(count));
			
			if (_wordnetLineage != null) {
				count = 0;
				for( String firstLineage : _firstSentenceLineages) {
					String[] firstTaxonomyClasses = firstLineage.split("/");
					for( String firstTaxonomyClass : firstTaxonomyClasses) {
						if( _wordnetLineage.indexOf(firstTaxonomyClass) != -1) {
							count++;
						}
					}
				}
				finalLineagesMap.put(_wordnetLineage, Integer.valueOf(count));
			}
		}
		
		
		/*
		* Order the categories and their taxonomy by their frequency
		*/
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TreeMap<String, Integer> orderedLineagesTaxonomies = new TreeMap(new NLineageComparator(finalLineagesMap));
		
		for(String key : finalLineagesMap.keySet()) {
			orderedLineagesTaxonomies.put(key, finalLineagesMap.get(key));
		}
		
		
		return orderedLineagesTaxonomies.keySet();	
	}

	
	
	private String resolveWordnetLineage() {
		String newTaxonomyLineage = null;
		int count = 0,
		    maxCount  = -1;
		
		for( String firstLineage : _firstSentenceLineages) {
			count = 0;
		
			for( String subLineage : _categoriesLineages ) {
				String[] subLineageClasses = subLineage.split("/");
				
				for( String subLineageClass : subLineageClasses) {		
					if( firstLineage.indexOf(subLineageClass) != -1) {
						count++;
					}
				}
			}
			
			if( _wordnetLineage != null ) {
				String[] lineageClasses = _wordnetLineage.split("/");
				for( String lineageClass : lineageClasses) {
					if( firstLineage.indexOf(lineageClass) != -1) {
						count++;
					}
				}
			}
			
			if( maxCount < count) {
				maxCount = count;
				newTaxonomyLineage = firstLineage;
			}
		}
		
		return newTaxonomyLineage;
	}


	
		/**
		 * <p>Extract the semantic components of a document using NLP algorithm to extract
		 * semantic N-Grams, WordNet hypernyms and dbpedia entries.</p>
		 * @param inputText input document
		 * @param title title of the document.
		 * @return the model of the text to be analyzed.
		 */
	private CText extract(final String inputText, final String title) {
		if(inputText == null || title == null) {
			throw new IllegalArgumentException("Cannot extract semantics data from an undefined document");
		}
		
		String content = null;
		String matchingVerb = null;
		int indexLabel = -1,
		    minIndexLabel = 2048;
		
		for( String verb : actionVerbs.keySet()) {
			indexLabel = inputText.indexOf(verb); 
			if(indexLabel != -1) {
				if( minIndexLabel > indexLabel) {
					minIndexLabel = indexLabel;
					matchingVerb = verb;
				}
			}
		}
		
		if( minIndexLabel < 2048) {
			minIndexLabel += matchingVerb.length()+1;
			if(inputText.length() - minIndexLabel > 12) {
				content = inputText.substring(minIndexLabel);
			}
		}
		
		if( content == null ) {
			content = inputText;
		}

				/*
			 * Step 1: Create a document model
			 */
		CText document = new CText(null);
				
			/*
			 * Step 2:Extract the N-Grams from the document
			 */
		CNGramsExtractor nGramsExtract = new CNGramsExtractor(_numTaxonomyObjects);
		if( nGramsExtract.extract(document, inputText) ) {
			document.setState(CText.E_STATES.NGRAMS);
			
			/*
			 * Step 3: Extract Composite and semantics 
			 */
			CTaxonomyExtractor taxonomyExtractor = new CTaxonomyExtractor(_taxonomyConnection, false);
			if( taxonomyExtractor.extract(document) ) {
				document.setState(CText.E_STATES.TAXONOMY);
			}
		}
		else {
			document.setState(CText.E_STATES.ERROR);
		}
	
		return document;
	}

}


// ---------------------  EOF ---------------------------------
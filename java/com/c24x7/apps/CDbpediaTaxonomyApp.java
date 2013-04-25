/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.apps;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.c24x7.semantics.CTaxonomyFilter;
import com.c24x7.semantics.dbpedia.CDbpediaSql;
import com.c24x7.textanalyzer.stemmer.CPluralStemmer;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



	/**
	 * <p>Class to list WordNet Hypernyms in long (original) and short (reduced)
	 * format. The short format consists of lower hypernyms filtered out by
	 * top taxonomy classes defined in "taxonomycutoff" file.</p>
	 *  
	 * @author Patrick Nicolas         24x7c 
	 * @date June 2, 2012 3:40:03 PM
	 */
public final class CDbpediaTaxonomyApp {
	private static final int	MIN_FIELD_LENGTH 		= 8;
	private static final int	MAX_TAXONOMY_LENGTH 	= 3800;
	private static final int	MAX_SUB_TAXONOMY_LENGTH = 13512;
	
	private static final String DBPEDIA_TAXONOMY_LIST_FILE 	= CEnv.trainingDir + "sets/original_taxonomies_list"; 
	private static final String REDUCED_TAXONOMY_LIST_FILE 	= CEnv.trainingDir + "sets/short_taxonomies_list"; 
	private static final String TOP_TAXONOMY_LIST_FILE 		= CEnv.trainingDir + "sets/top_taxonomies_list"; 

	
	private static final String SELECT_WORDNET 		= "SELECT wordnet FROM 24x7c.dbpedia WHERE id=?;";
	private static final String SELECT_CATEGORIES 	= "SELECT categories,lgabstract,label,wordnet,wnet,taxonomy FROM 24x7c.dbpedia WHERE categories IS NOT NULL AND id=?;";
	private static final String SELECT_CAT_TAXONOMY = "SELECT taxonomy FROM 24x7c.dbpedia WHERE label=? AND wnet =1;";
	private static final String INSERT_SUB_TAXONOMY = "UPDATE 24x7c.dbpedia SET sub_taxonomy=? WHERE id=?;"; 
	private static final String INSERT_TAXONOMY 	= "UPDATE 24x7c.dbpedia SET taxonomy=? WHERE id=?;"; 

	
	private CSqlPreparedStmt	_pStmt 				= null;
	private CSqlPreparedStmt	_pStmtCatTaxonomy 	= null;
	private CSqlPreparedStmt	_pStmtSubTaxonomy 	= null;
	private Map<String, Object>	_allTaxonomyLineages = null;
	private int					_count = 0;
	
	
	
		/**
		 * <p>Class that implement the comparator for the taxonomy nodes. 
		 * The comparator is used to ordered the list of Taxonomy nodes
		 * (classes and instances) extracted from 
		 * a document by comparing their relative weights.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 05/23/2012
		 */
	protected static class NCategoryComparator implements Comparator<String> {
		private Map<String, Integer> _frequenciesMap = null;
		private Map<String, String>  _stemsMap 		 = null;
		
		
		private NCategoryComparator(Map<String, Integer> frequenciesMap, Map<String, String> stemsMap) {
			super();
			_frequenciesMap = frequenciesMap;
			_stemsMap = stemsMap;
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
			final String stemmedKey1 = _stemsMap.get(key1);
			if( stemmedKey1 != null) {
				key1 = stemmedKey1;
			}
			final String stemmedKey2 = _stemsMap.get(key2);
			if( stemmedKey2 != null) {
				key2 = stemmedKey2;
			}
			
			return _frequenciesMap.get(key1) < _frequenciesMap.get(key2) ? 1 : -1;
		}
	}




	/**
	 * <p>Main command line application to generate a list of hypernyms
	 * in original and short format.<br>
	 * The command line is: 
	 * com.c24x7.apps.CDbpediaTaxonomyApp [-original/-short/-all]  with<br>
	 * -original for original WordNet format with '/' delimiter<br>
	 * -short for hypernyms without top classes defined in taxonomycutoff file<br>
	 * - all generate both original and short format.</p>
	 */
	public static void main(String[] args) {
		if( args.length > 0) {
			CLogger.setLoggerInfo(CLogger.TOPIC_TRAIN_TRACE);
			CDbpediaTaxonomyApp app = new CDbpediaTaxonomyApp();
			
			try {
				if( args[0].compareTo("-wordnet")==0) {
					app.extractWordnetHypernyms();
				}
				else if( args[0].compareTo("-subgraphs")==0) {
					app.extractHypernymsSubGraphs();
				}
				else if(  args[0].compareTo("-all")==0) {
					app.extractWordnetHypernyms();
					app.extractHypernymsSubGraphs();
				}
				else if( args[0].compareTo("-taxonomy")==0) {
					app.addCategoriesTaxonomy();
				}
				else if( args[0].compareTo("-subTaxonomy")==0) {
					app.addHypernymsSubGraphs();
				}
				else if(args[0].compareTo("-firstSentence") ==0) {
					
				}
				else {
					CLogger.error("Incorrect command line arguments");
				}
			}
			catch(IOException e) {
				CLogger.error(e.toString());
			}
		}
		else {
			CLogger.error("Incorrect command line arguments");
		}
	}
	
	
	
	private CDbpediaTaxonomyApp() { }
	
	private void close() {
		if( _pStmt != null) {
			_pStmt.close();
		}
		if( _pStmtCatTaxonomy != null) {
			_pStmtCatTaxonomy.close();
		}
		if( _pStmtSubTaxonomy != null) {
			_pStmtSubTaxonomy.close();
		}
	}
	

		/**
		 * <p>Generate the list of WordNet hypernyms, extracted from 
		 * DBpedia table. Those hypernyms share the common root 'entity'.</p>
		 * @throws IOException if the ordered list of hypernyms hierarchy cannot be written into file.
		 */
	private void extractWordnetHypernyms() throws IOException {
		if( _allTaxonomyLineages == null) {
			_allTaxonomyLineages = new TreeMap<String, Object>();
		}

		if( _pStmt == null) {
			_pStmt = new CSqlPreparedStmt(SELECT_WORDNET);
		}
		
		/*
		 * Retrieve the list of WordNet hypernyms..
		 */
		int maxId = CDbpediaSql.getInstance().getNumEntries();
		for( int id = 1; id < maxId; id++) {
			try {
				getRecord(id);
			}
			catch( SQLException e) {
				CLogger.error(e.toString());
			}
		}
		
		if( _pStmt != null) {
			_pStmt.close();
		}
		
		/*
		 * Save the list into a configuration file
		 */
		PrintWriter writer = null;
		FileOutputStream fos = null;

		try {	
			fos = new FileOutputStream(DBPEDIA_TAXONOMY_LIST_FILE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
				
			for( String lineage : _allTaxonomyLineages.keySet()) {
				writer.println(lineage);
			}
			writer.close();
			fos.close();
			
			if( writer.checkError()) {
				throw new IOException("Cannot save content");
			}
		}

		finally {
			if( writer != null ) {
				writer.close();

				if( writer.checkError()) {
					throw new IOException("Cannot save content");
				}		
				fos.close();
			}
		}
	}
	
	
	private boolean extractHypernymsSubGraphs() {
		boolean success = false;
		if( _allTaxonomyLineages == null) {
			_allTaxonomyLineages = new TreeMap<String, Object>();
		}
		
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> topClassesMap = new TreeMap<String, String>();
		Map<String, Integer> parentsMap = new HashMap<String, Integer>();
		Map<String, Integer> grandParentsMap = new HashMap<String, Integer>();
		
		try {
			CFileUtil.readKeysValues(DBPEDIA_TAXONOMY_LIST_FILE, map);
	
			String[] hypernyms = null;
			CTaxonomyFilter cutoff = CTaxonomyFilter.getInstance();
			int parentIndex = 1,
			    grandParentIndex = 1;
			
			for(String lineage : map.keySet()) {
				hypernyms = lineage.split("/");
				int k = 0;
				for(; k < hypernyms.length && cutoff.contains(hypernyms[k]); k++) 
					;
				if( k < hypernyms.length-1) {
						
					StringBuilder bufParent = new StringBuilder();
					if( k > 0) {
						if( !parentsMap.containsKey(hypernyms[k-1])) {
							parentsMap.put(hypernyms[k-1], parentIndex++);
						}
						bufParent.append(hypernyms[k-1]);
					}
					if( k > 1) {
						if( !grandParentsMap.containsKey(hypernyms[k-2])) {
							grandParentsMap.put(hypernyms[k-2], grandParentIndex++);
						}
						bufParent.append(CEnv.FIELD_DELIM);
						bufParent.append(hypernyms[k-2]);
					}
						
					topClassesMap.put(hypernyms[k], bufParent.toString());
					String newLineage = CStringUtil.convertClassesToLineages(hypernyms);
	
					_allTaxonomyLineages.put(newLineage, null);
				}
			}
							
				
			StringBuilder lowerHypernyms = new StringBuilder();
			for(String key : _allTaxonomyLineages.keySet()) {
				lowerHypernyms.append(key);
				lowerHypernyms.append("\n");
			}
				
			success = CFileUtil.write(REDUCED_TAXONOMY_LIST_FILE, lowerHypernyms.toString());
				
			lowerHypernyms = new StringBuilder();
			String parentHierarchyStr = null;
			Integer parentId = null,
			        grandParentId = null;
				
			for(String key : topClassesMap.keySet()) {
				lowerHypernyms.append(key);
				lowerHypernyms.append(CEnv.KEY_VALUE_DELIM);
					
				parentHierarchyStr = topClassesMap.get(key);
					
				if( parentHierarchyStr != null) {
					String[] parents = parentHierarchyStr.split(CEnv.FIELD_DELIM);
					if( parents.length == 2) {
						parentId = parentsMap.get(parents[0]);
						grandParentId = grandParentsMap.get(parents[1]);
						lowerHypernyms.append(String.valueOf(parentId) + CEnv.FIELD_DELIM + String.valueOf(grandParentId));
					}
				
					else if( parents.length == 1) {
						parentId = parentsMap.get(parents[0]);
						lowerHypernyms.append(parentId);
					}
				}
				lowerHypernyms.append("\n");
					
			}
			success = CFileUtil.write(TOP_TAXONOMY_LIST_FILE, lowerHypernyms.toString());
		}
		catch(IOException e) {
			CLogger.error(e.toString());
		}
		
		return success;
	}
	

	
	private void addCategoriesTaxonomy() {
		/*
		 * Allocate the appropriate JDBC connections,.
		 */
		if( _pStmt == null) {
			_pStmt = new CSqlPreparedStmt(SELECT_WORDNET);
		}
		if( _pStmtSubTaxonomy == null) {
			_pStmtSubTaxonomy = new CSqlPreparedStmt(INSERT_TAXONOMY);
		}
		
		/*
		 * Iterates through the entire DBpedia table
		 */
		int maxId = CDbpediaSql.getInstance().getNumEntries();
		for(int id = 1; id < maxId; id++) {
			addCategoriesTaxonomy(id);
		}
			
		close();
	}
	
	
	
	private void addHypernymsSubGraphs() {
		/*
		 * Allocate the appropriate JDBC connections,.
		 */
		if( _pStmt == null) {
			_pStmt = new CSqlPreparedStmt(SELECT_CATEGORIES);
		}
		if( _pStmtSubTaxonomy == null) {
			_pStmtSubTaxonomy = new CSqlPreparedStmt(INSERT_SUB_TAXONOMY);
		}
		if( _pStmtCatTaxonomy == null) {
			_pStmtCatTaxonomy = new CSqlPreparedStmt(SELECT_CAT_TAXONOMY);
		}
		
		int maxId = CDbpediaSql.getInstance().getNumEntries();
		for(int id = 1; id < maxId; id++) {
			addHypernymsSubGraphs(id);
		}
			
		close();
	}

	

	private void addCategoriesTaxonomy(int id) {
		try {
			String wordnet = null;
			_pStmt.set(1, id);
			
			ResultSet rs = _pStmt.query();
			StringBuilder buf = null;
			boolean validTaxonomyClassFlag = false;
			
			if( rs.next() ) {
				wordnet = rs.getString("wordnet");
				
				if(wordnet != null && wordnet.length() > 8) {
					wordnet = CStringUtil.decodeLatin1(wordnet);
					
					buf = new StringBuilder();
					
					String[] taxonomyLineages = wordnet.split("##");
					int k = 0;
					for( String taxonomyLineage : taxonomyLineages) {
						String[] taxonomyClasses = taxonomyLineage.split("/");
						validTaxonomyClassFlag = false;
						
						for(String taxonomyClass : taxonomyClasses) {
							if( validTaxonomyClassFlag || !CTaxonomyFilter.getInstance().contains(taxonomyClass)) {
								validTaxonomyClassFlag = true;
								buf.append(taxonomyClass);
								buf.append("/");
							}
						}
						
						if(buf.length() > 1) {
							buf.deleteCharAt(buf.length()-1);
						}
						if( ++k < taxonomyLineages.length ) {
							buf.append("#");
						}
					}

					if( updateTaxonomy(buf.toString(), id, MAX_TAXONOMY_LENGTH) >= 0 && _count++ %100 == 0) {
						System.out.println("Counter: " + _count);
					}	
				}
			}
		}
		catch(SQLException e) {
			CLogger.error("Cannot create sub taxonomy for " + id + " " + e.toString());
		}
	}
	

	
	private void addHypernymsSubGraphs(int id) {
		try {
			String categories = null,
			       content = null;

			_pStmt.set(1, id);
			
			ResultSet rs = _pStmt.query();
			if( rs.next() ) {
				categories = rs.getString("categories");
				content = rs.getString("lgabstract");
				
				if( categories != null) {
					Map<String, Integer> categoryTaxonomiesMap  = new HashMap<String, Integer>();
					Map<String, String> catStemsMap = new HashMap<String, String>();
					
					extractCategories(categories, categoryTaxonomiesMap, catStemsMap);
					
					
					/*
					 * No need to insert null categories taxonomy in the table.
					 */
					if( categoryTaxonomiesMap.size() > 0) {
						content = CStringUtil.decodeLatin1(content);
						content = content.toLowerCase();
						
								
						int counter = 0;						
						String stemCatLabel = null;
						for(String catLabel : catStemsMap.keySet()) {
							stemCatLabel = catStemsMap.get(catLabel);
							if( stemCatLabel != null) {
								counter = getCategoryFrequency(stemCatLabel, content);
								if( counter > 0) {
									Integer curCount = categoryTaxonomiesMap.get(stemCatLabel);
									categoryTaxonomiesMap.put(stemCatLabel, Integer.valueOf(curCount.intValue() + counter));
								}
							}
							
							else {
								counter = getCategoryFrequency(catLabel, content);
								if(counter > 0) {
									Integer curCount = categoryTaxonomiesMap.get(catLabel);
									categoryTaxonomiesMap.put(catLabel, Integer.valueOf(curCount.intValue() + counter));
								}
							}
						}
						
						/*
						 * Order the categories and their taxonomy by their frequency
						 */
						@SuppressWarnings({ "rawtypes", "unchecked" })
						TreeMap<String, Integer> orderedCategoriesTaxonomies = new TreeMap(new NCategoryComparator(categoryTaxonomiesMap, catStemsMap));
						
						for(String key : categoryTaxonomiesMap.keySet()) {
							orderedCategoriesTaxonomies.put(key, categoryTaxonomiesMap.get(key));
						}

						String[] lineages = null;
						int value = 0,
						    maxValue = -1;
						
						/*
						 * We collect only the taxonomy lineages from categories
						 * which have the highest frequency in the corpus 
						 * reference document or abstract.
						 */
						Map<String, Object> redundantLineagesMap = new HashMap<String, Object>();
						StringBuilder mostFrequentCatLineages = new StringBuilder();
						
						/*
						 * Extract the taxonomy lineages associated with the 
						 * category with the highest frequency in the Wikipedia
						 * reference corpus or abstract.
						 */
						for( String key : orderedCategoriesTaxonomies.keySet()) {
							lineages = extractCategoryTaxonomy(key);
							if( lineages != null) {
								value = categoryTaxonomiesMap.get(key);
								if (maxValue == -1) {
									maxValue = value;
								}
								else if( maxValue != value) {
									break;
								}
								for( String lineage : lineages) {
									if( !redundantLineagesMap.containsKey(lineage)) {
										redundantLineagesMap.put(lineage, null);
										mostFrequentCatLineages.append(lineage);
										mostFrequentCatLineages.append("#");
									}
								}
							}
						}
						
							/*
							 * Insert all the taxonomy lineages corresponding to the
							 * categories associated with this label.
							 */
						if(mostFrequentCatLineages.length() > 1) {
							mostFrequentCatLineages.deleteCharAt(mostFrequentCatLineages.length()-1);
							CLogger.info(mostFrequentCatLineages.toString(), CLogger.TOPIC_TRAIN_TRACE);
						}
						
						int numRecords = updateTaxonomy(mostFrequentCatLineages.toString(), id, MAX_SUB_TAXONOMY_LENGTH);
						if( numRecords >= 0 && _count++ %250 == 0) {
							System.out.println("Cnt= " + _count + " id=" + id);
						}
					}
				}
			}
		}
		catch(SQLException e) {
			CLogger.error("Cannot create sub taxonomy for " + id + " " + e.toString());
		}
	}
	
	
	
		/**
		 * <p>Count the number of occurrences of a category or its stem 
		 * within a reference document or corpus such as Dbpedia.</p>
		 * @param category Wikipedia category
		 * @param content reference document or abstract used in computing the frequency the category
		 * @return frequency of the category within the reference document.
		 */
	private int getCategoryFrequency(final String category, final String content) {
		int count = 0;
		
		if(content.indexOf(category) != -1) {
			try {
				String[] contentSplits = content.split(category);
				count += contentSplits.length -1;
			}
			catch(Exception e) {
				CLogger.error("Error: " + category + "\n" + content);
			}
		}
		
		return count;
	}
	
		
		
		
	private int updateTaxonomy(String subTaxonomyStr, int id, int maxFieldLength) throws SQLException {
	
		int numRecords = -1;
		subTaxonomyStr = CStringUtil.encodeLatin1(subTaxonomyStr);
		
			/*
			 * Make sure that the new value does not exceed the 
			 * maximum size of the table column, otherwise
			 * we need to truncate to the previous delimiter.
			 */
		if( subTaxonomyStr.length() >= maxFieldLength) {
			subTaxonomyStr = subTaxonomyStr.substring(0, maxFieldLength-1);
			subTaxonomyStr = CStringUtil.decodeLatin1(subTaxonomyStr);
			
			/*
			 * If the value is not null, truncate to the last delimiter.
			 */
			if( subTaxonomyStr != null) {
				int lastSeparator = subTaxonomyStr.lastIndexOf("#");
				
				if( lastSeparator != -1) {
					subTaxonomyStr = subTaxonomyStr.substring(0, lastSeparator-1);
					subTaxonomyStr = CStringUtil.encodeLatin1(subTaxonomyStr);
				}
				else {
					subTaxonomyStr = null;
				}
			}
		}
		
		/*
		 * Insert to list of taxonomy classes related to categories
		 * to the table..
		 */
		if( subTaxonomyStr != null) {
			_pStmtSubTaxonomy.set(1, subTaxonomyStr);
			_pStmtSubTaxonomy.set(2, id);			
			numRecords = _pStmtSubTaxonomy.update();
		}
		
		return numRecords;
	}
	
	
	
		/**
		 * <p>Extracts the individual categories and their stems if exist from
		 * the categories list in the Wikipedia reference database. The
		 * method compute the number of occurrences of each category entry 
		 * within the list.</p>
		 * @param category category to be extracted
		 * @param categoryTaxonomiesMap map of all the categories associated to the Wikipedia entry 
		 * @param stemsMap map of category labels and their associated stems, if exists.
		 */
	
	private void extractCategoryAndStems(final String 			category, 
										 Map<String, Integer> 	categoryTaxonomiesMap,
										 Map<String, String> 	stemsMap) {
		/*
		 * Extract the lower case of the category and its corresponding stem
		 */
		String categoryEntry = category.toLowerCase();
		String stemmedCategoryEntry = CPluralStemmer.getInstance().stem(categoryEntry);
		
		Integer numOccurrences = null;
		int count = 0;
		
		/*
		 * if the stem for the category label exists then count the 
		 * number of occurrences of the stem as the number of occurrences 
		 * of the category label is the same as the number of occurrences
		 * of its stem.
		 */
		if( stemmedCategoryEntry != null) {
			if( categoryTaxonomiesMap.containsKey(stemmedCategoryEntry) ) {
				numOccurrences = categoryTaxonomiesMap.get(stemmedCategoryEntry);
				count = numOccurrences.intValue()+1;
			}
			else {
				count = 1;
			}
			categoryTaxonomiesMap.put(stemmedCategoryEntry, Integer.valueOf(count));
		}
		/*
		 * If the stem is not different from the label then update the
		 * number of category labels.
		 */
		else {
			if( categoryTaxonomiesMap.containsKey(categoryEntry))  {
				numOccurrences = categoryTaxonomiesMap.get(categoryEntry);
				count = numOccurrences.intValue()+1;
			}
			else {
				count = 1;
			}
			categoryTaxonomiesMap.put(categoryEntry, Integer.valueOf(count));
		}
		
		/*
		 * update the map (category labels <-> stemmed labels)
		 */
		stemsMap.put(categoryEntry, stemmedCategoryEntry);
	}
	
	
	
	private String[] extractCategoryTaxonomy(final String category) throws SQLException {
		
		String[] taxonomies = null;
			
		_pStmtCatTaxonomy.set(1, category);
		ResultSet rs = _pStmtCatTaxonomy.query();
		String wordnet = null;
		
		if(rs.next() ) {
			wordnet = rs.getString("taxonomy");
		
			if(wordnet != null && wordnet.length() > MIN_FIELD_LENGTH) {				 
				wordnet = CStringUtil.decodeLatin1(wordnet);
				taxonomies = wordnet.split("#");
			}
		}
		
		return taxonomies;
	}
	
	
	private void extractCategories(	String categoriesStr, 
									Map<String, Integer> categoryTaxonomiesMap,
									Map<String, String> stemsMap) throws SQLException {
			
		if(categoriesStr.length() > MIN_FIELD_LENGTH) {
			
			categoriesStr = CStringUtil.decodeLatin1(categoriesStr);
			if( categoriesStr != null ) {
				String[] categories = categoriesStr.split("#");
				String[] categoryTerms = null;
				
				for( String category : categories) {
					//INFO
					CLogger.info("Cat=" + category, CLogger.TOPIC_TRAIN_TRACE);
					
					extractCategoryAndStems(category, categoryTaxonomiesMap, stemsMap);
					categoryTerms = category.split(" ");
						
					if( categoryTerms != null && categoryTerms.length > 0) {
						for(String categoryTerm : categoryTerms) {
						
							if( categoryTerm != null && categoryTerm.length() > 3) {
								extractCategoryAndStems(categoryTerm, categoryTaxonomiesMap, stemsMap);
							}
						}
					}
				}
			}
		}
	}
	
		
	private void getRecord(int id) throws SQLException {
		_pStmt.set(1, id);		
		ResultSet rs = _pStmt.query();
		String taxonomyLineageStr = null;
		int wordnetFlag = -1;

		if( rs.next() ) {
			taxonomyLineageStr = rs.getString("wordnet");
			wordnetFlag = rs.getInt("wnet");
		}
		
		/*
		 * We only extract abstract with more than 1024 characters and only one
		 * taxonomy lineage (or WordNet hierarchy of hypernyms).
		 */
		if( wordnetFlag == 1 && taxonomyLineageStr != null && taxonomyLineageStr.length() > 5) {
			final String decodedTaxonomyLineageStr = CStringUtil.decodeLatin1(taxonomyLineageStr, CEnv.ENCODED_ENTRY_DELIM);
			String[] taxonomyLineagesArray = decodedTaxonomyLineageStr.split(CEnv.ENTRIES_DELIM);
			
			for(String taxonomyLineage: taxonomyLineagesArray) {
				_allTaxonomyLineages.put(taxonomyLineage, null);
			}
		}
	}	
}

// -----------------------------------  EOF --------------------------------------------
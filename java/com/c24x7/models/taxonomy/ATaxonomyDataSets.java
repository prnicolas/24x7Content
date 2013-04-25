// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.mapred.IDataSets;
import com.c24x7.util.CEnv;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



public abstract class ATaxonomyDataSets implements IDataSets {
	private final static int 	DISPLAY_INTERVAL = 250;
	private static final String PREPARED_STATEMENT = "SELECT label,wordnet,lgabstract,categories,wnet FROM 24x7c.dbpedia WHERE id=?;";

	protected static Object lock 	= new Object();
	protected static int 	counter = 0;
	
	protected int 				_indexStart	= -1;
	protected int 				_indexEnd 	= -1;
	protected CSqlPreparedStmt	_pStmt 		= null;


	
		/**
		 * <p>Create a generic sets of observations for training
		 * or validation of the taxonomy model features.</p>
		 * @param indexStart index of the first Wikipedia record used in training or validation
		 * @param indexEnd  index of the last Wikipedia record used in training or validation
		 */
	protected ATaxonomyDataSets() {
		_pStmt = new CSqlPreparedStmt(PREPARED_STATEMENT);
	}
	
	protected ATaxonomyDataSets(int startIndex, int endIndex) {
		_pStmt = new CSqlPreparedStmt(PREPARED_STATEMENT);
		_indexStart = startIndex;
		_indexEnd = endIndex;
	}
	
	public void run() {
		counter = 0;
		map();
	}

			/**
			 * <p>Execute the training of the taxonomy model. This method computes the
			 * average values of the taxonomy model features extracted from the 
			 * training sets.</p>
			 * @return number of records or sets used in the validation.
			 */
	public void map() {		
		for(int k = _indexStart; k < _indexEnd; k++)  {
			try {
				loadObservations(k);
			}
			catch( SQLException e) {
				CLogger.error("\nCannot extract training sets: " + e.toString());
			}
		}
	}
	
	
	public void close() {
		if( _pStmt != null ) {
			_pStmt.close();
		}
	}
	
	protected void displayProgress() {
		if(counter++ % DISPLAY_INTERVAL == 0) {
			System.out.println(counter + " records processed");
		}
	}
	
	
		
	
	protected static int getCounter() {
		return counter;
	}
	
	/**
	 * <p>Method that adds a new observation/data point representing a 
	 * document or a document of a document, to the data set (Training, Validation, Clustering)</p>
	 * @param title  title of the abstract
	 * @param content content for which the taxonomy has to be classified
	 * @param labeledTaxonomy  taxonomy provided as label (supervised learning)
	 * @param categoriesList  keywords representing categories for the document
	 */
	protected void addObservation(	final String 		title,  
									final String 		content, 
									final String[] 		labeledTaxonomyLineages, 
									final List<String> 	categoriesList)  { }
	
	public void collectResults(int[] counter) { }
	
	protected void loadObservations(final int id) throws SQLException  {
		
		_pStmt.set(1, id);		
		ResultSet rs = _pStmt.query();
		String taxonomyLineageStr = null;
		String categoriesStr = null;
		String lgAbstract = null;
		String keyword = null;
		int wordnetFlag = -1;

		if( rs.next() ) {
			keyword = rs.getString("label");
			taxonomyLineageStr = rs.getString("wordnet");
			categoriesStr = rs.getString("categories");
			lgAbstract = rs.getString("lgabstract");
			wordnetFlag = rs.getInt("wnet");
		}
		
		if(keyword != null &&  wordnetFlag == 1 && taxonomyLineageStr != null && taxonomyLineageStr.length() > 2)  {
			
			final String decodedTaxonomyLineageStr = CStringUtil.decodeLatin1(taxonomyLineageStr, CEnv.ENCODED_ENTRY_DELIM);
			final String decodedKeyword = CStringUtil.decodeLatin1(keyword);
			String[] taxonomyLineagesArray = decodedTaxonomyLineageStr.split(CEnv.ENCODED_ENTRY_DELIM);
			
				/*
				 * We collect only the labels which have only
				 * sense or ontology branch. Terms with multiple senses
				 * create dependencies adding noise to the training set.
				 */
			if( lgAbstract != null && taxonomyLineagesArray != null && taxonomyLineagesArray.length == 1) {
				final String decodedLgAbstract = CStringUtil.decodeLatin1(lgAbstract);
				
				/*
				 * Make sure the abstract is lengthly enough to generated
				 * meaningful values for the model features during training. 
				 */
				if( decodedLgAbstract != null && decodedLgAbstract.length() > CTaxonomyModel.MIN_VALID_CONTENT_LENGTH ) {
					
					List<String> categoriesList = null;
					/*
					 * If the model uses categories abstract, then add them into the analysis
					 */
					if( CTaxonomyModel.getInstance().hasCategories() && categoriesStr != null) {
						categoriesList = new LinkedList<String>();
						final String decodedCategoryStr = CStringUtil.decodeLatin1(categoriesStr, CEnv.ENCODED_ENTRY_FIELDS_DELIM);
						
						if( decodedCategoryStr != null) {
							String[] categoriesArray = decodedCategoryStr.split(CEnv.ENTRY_FIELDS_DELIM); 
							
							/*
							 * collect all categories without duplicating the label which
							 * may be included in the categories list.
							 */
							for( String category : categoriesArray) {
								if( category.length() > 2 && category.compareTo(decodedKeyword) != 0) {
									categoriesList.add(category);
								}
							}
							
									/*
									 * Add each word net sense or taxonomy lineage to the set of observations.
									 * TODO create an algorithm to select the best labeled lineage
									 */
							addObservation(decodedKeyword, decodedLgAbstract, taxonomyLineagesArray, categoriesList);	
						}
					}
					
					
					else {
						addObservation(decodedKeyword, decodedLgAbstract, taxonomyLineagesArray, null);
					}
				}
			}
		}
	}
}

// ----------------------------  EOF -----------------------------------

package com.c24x7.models.taxonomy;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.taxonomy.CTaxonomyModel;
import com.c24x7.models.taxonomy.CTaxonomyModel.NLikelihood;
import com.c24x7.mapred.IDataSets;
import com.c24x7.nlservices.CDbpediaSemanticService;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



public class CTaxonomyTestSets extends ATaxonomyDataSets {
	
	private static final String TEST_FILE 	= CEnv.trainingDir + "test/taxonomy_test";
	private static StringBuilder resultsBuffer = null;

	
	
	public IDataSets create(int startIndex, int endIndex) {
		return new CTaxonomyTestSets(startIndex, endIndex);
	}
	

	
		/**
		 * <p>Create a generic sets of observations for training of the taxonomy model features.</p>
		 * @param indexStart index of the first Wikipedia record used in training or validation
		 * @param indexEnd  index of the last Wikipedia record used in training or validation
		 */
	protected CTaxonomyTestSets(int startIndex, int endIndex) {
		super(startIndex, endIndex);
	}

		
		/**
		 * <p>Save the results of the validation run into file.</p>
		 * @param resultsStr textual table of results of the validation
		 * @param fileId file identifier for a specific validation run
		 * @param count array that contains the number of Wikipedia entries and the number of successful matches.
		 * @return true if the validation results are saved, false otherwise.
		 */
	public void saveResults(int[] count) {
		try {
			StringBuilder output = new StringBuilder("\n\n");
			output.append(CTaxonomyModel.getInstance().paramsDescription());
			if( resultsBuffer != null) {
				output.append("\n");
				output.append(resultsBuffer.toString());
			}
			
			CFileUtil.write(CTaxonomyModel.getInstance().getFileName(TEST_FILE) , output.toString());
		}
		catch( IOException e) {
			CLogger.error("Cannot store validation results " + e.toString());
		}
	}
	
		
		
	
	protected void loadObservations(final int id) throws SQLException  {
		
		_pStmt.set(1, id);		
		ResultSet rs = _pStmt.query();
		String taxonomyLineageStr = null;
		String categoriesStr = null;
		String lgAbstract = null;
		String keyword = null;

		if( rs.next() ) {
			keyword = rs.getString("label");
			taxonomyLineageStr = rs.getString("wordnet");
			categoriesStr = rs.getString("categories");
			lgAbstract = rs.getString("lgabstract");
		}
		
		if(keyword != null && (taxonomyLineageStr == null || taxonomyLineageStr.length() < 2))  {
			final String decodedKeyword = CStringUtil.decodeLatin1(keyword);
			final String decodedLgAbstract = CStringUtil.decodeLatin1(lgAbstract);
		
				
				/*
				 * Make sure the abstract is lengthly enough to generated
				 * meaningful values for the model features during training. 
				 */
			if( decodedLgAbstract.length() > CTaxonomyModel.MIN_VALID_CONTENT_LENGTH ) {
					
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
						addObservation(decodedKeyword, decodedLgAbstract, categoriesList);	
					}
				}
					
				else {
					addObservation(decodedKeyword, decodedLgAbstract, null);
				}
			}
		}
	}
	
	



			/**
			 * <p>Method that adds a new observation/data point representing a 
			 * document or a document of a document, to the validation set</p>
			 * @param title  title of the abstract
			 * @param content content for which the taxonomy has to be classified
			 * @param labeledTaxonomy  taxonomy provided as label (supervised learning)
			 * @param categoriesList  keywords representing categories for the document
			 */
	
	protected void addObservation(	final String 		title,  
									final String 		content, 
									final List<String> 	categoriesList) {
		
			/*
			 * Validation is to be performed on content of minimum length
			 */
		if( content.length() > CTaxonomyModel.MIN_VALID_CONTENT_LENGTH) {
			try {
				CDbpediaSemanticService hypernymsGenerator = new CDbpediaSemanticService();
			
			//INFO
				if(title != null) {
					CLogger.info("\nKEYWORD: " + title, CLogger.TAXONOMY_TRAIN_TRACE);
				}
			
				NLikelihood selection = new NLikelihood();
				ATaxonomyNode[] bestTaxonomyLineage = hypernymsGenerator.getRelevantClasses(title, content, categoriesList, selection);
				
				StringBuilder results = null;
				if( bestTaxonomyLineage != null) {
				
					results = new StringBuilder("\n");
					results.append(content);
					results.append("\nBest taxonomy=(");
					results.append(selection.getClassId());
					results.append( CEnv.FIELD_DELIM);
					results.append(selection.getLikelihood());
					results.append(")\n");
					results.append(CTaxonomyModel.convertClassesToLineage(bestTaxonomyLineage));
					results.append("/");
					results.append(title);
				}
			
				synchronized (lock ) {
					if( results != null) {
						if(resultsBuffer == null) {
							resultsBuffer = new StringBuilder();
						}
						resultsBuffer.append(results.toString());
						resultsBuffer.append("\n\n");
					}
				
					displayProgress();
				}
			}
			catch( SemanticAnalysisException e) {
				CLogger.error(e.toString());
			}
		}
	}

}

 // ---------------------------------------------  EOF -------------------------------

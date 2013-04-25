// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.CText;
import com.c24x7.models.taxonomy.CTaxonomyModelStats.NTaxonomyClassStats;
import com.c24x7.mapred.IDataSets;
import com.c24x7.nlservices.CDbpediaSemanticService;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;



			/**
			 * <p>Class to train the taxonomy (or hypernyms) model using Naive Bayesian.</p>
 			 * @author Patrick Nicolas
			 * @date 03/02/2012
			 */
public class CTaxonomyTrainingSets extends ATaxonomyDataSets {
	private static final String CATEGORY_PREPARED_STATEMENT = "SELECT lgAbstract FROM 24x7c.dbpedia WHERE label=?;";
		
	protected CSqlPreparedStmt		  		_pCatStmt 	= null;
	private static List<NTaxonomyClassStats> observationsList = null;

	


	public static List<NTaxonomyClassStats> getObservationsList() {
		return observationsList;
	}
	
	public IDataSets create(int startIndex, int endIndex) {
		return new CTaxonomyTrainingSets(startIndex, endIndex);
	}
		
	
	public CTaxonomyTrainingSets() { }
	
	
	public void close() {
		super.close();
		if( _pCatStmt != null ) {
			_pCatStmt.close();
		}
	}
	
	
	
		/**
		 * <p>Create a generic sets of observations for training of the taxonomy model features.</p>
		 * @param indexStart index of the first Wikipedia record used in training or validation
		 * @param indexEnd  index of the last Wikipedia record used in training or validation
		 */
	protected CTaxonomyTrainingSets(int startIndex, int endIndex) {
		super(startIndex, endIndex);
		
		if( observationsList == null) {
			observationsList = new LinkedList<NTaxonomyClassStats>();
		}
		_pCatStmt =  new CSqlPreparedStmt(CATEGORY_PREPARED_STATEMENT);
	}


		/**
		 * <p>Method that adds a new observation/data point representing a 
		 * document or a document of a document, to the training set.</p>
		 * @param title  title of the abstract
		 * @param content content for which the taxonomy has to be classified
		 * @param labeledTaxonomyLineages  labeled taxonomy lineage (supervised learning)
		 * @param categoriesList  keywords representing categories for the document
		 */
	@Override
	protected void addObservation(	final String 		label,  
									final String 		lgAbstract, 
									final String[] 		labeledTaxonomyLineages, 
									final List<String> 	categoriesList) {
		
			/*
			 * walks through the list of categories and their stems, to collect all 
			 * the taxonomy lineages associated with the abstract of each valid
			 * category
			 */		
		try {
			CDbpediaSemanticService semService = new CDbpediaSemanticService();
			semService.excludeTitle();
		

			CText document = semService.extract(lgAbstract, label, categoriesList);	
			if( document != null) {
				List<ATaxonomyNode[]> taxonomyClassesList = document.getTaxonomyClassesList();
				
				if( taxonomyClassesList != null && taxonomyClassesList.size() > 0) {				
					/*
					 * Compute the statistics related to the frequency, weight of hypernyms (or 
					 * taxonomy instances) and the number of reference of categories in the 
					 * the hypernyms of the label content.
					 */
					generateStats(labeledTaxonomyLineages,taxonomyClassesList);
				}
				
				synchronized (lock ) {
					displayProgress();
				}
			}
		}
		catch( SemanticAnalysisException e) {
			CLogger.error(e.toString());
		}
	}
			

	
	public void saveResults(int[] counters) { }

	
					// --------------------------
					// Private Supporting Methods
					// --------------------------

	
	private void generateStats(	final String[] 				 labeledTaxonomyLineages, 
								final List<ATaxonomyNode[]> taxonomyClassesList) {
		
		for( int k = 0; k < labeledTaxonomyLineages.length; k++  ) {
			generateStat(labeledTaxonomyLineages[k], taxonomyClassesList);;
		}
	}

				
	
	private void generateStat(	final String 				 labeledTaxonomyLineage, 
								final List<ATaxonomyNode[]> taxonomyClassesList) {


			/*
			 * Compute the distance between any of the taxonomy lineages in the
			 * document and the labeled taxonomy lineage and select the most
			 * relevant and least relevant lineage.
			 */
		
		for( ATaxonomyNode[] taxonomyClasses : taxonomyClassesList) {
			int classNumber = CTaxonomyModel.getInstance().computeClass(taxonomyClasses, labeledTaxonomyLineage);
			synchronized (lock ) {
				observationsList.add(new NTaxonomyClassStats(taxonomyClasses, classNumber));
			}
		}
				
		//INFO
		if( CLogger.isLoggerInfo()) {
			StringBuilder buf = new StringBuilder("\n\n(*)");
			buf.append(labeledTaxonomyLineage);
			buf.append("\n");
	
			ATaxonomyNode[] bestTaxonomyClasses = null;
			double maxSimilarity = 0.0;
			for( ATaxonomyNode[] taxonomyClasses : taxonomyClassesList) {
				int classNumber = CTaxonomyModel.getInstance().computeClass(taxonomyClasses, labeledTaxonomyLineage);
				double similarity = CTaxonomyModel.getInstance().computeSimilarity(taxonomyClasses, labeledTaxonomyLineage);
				
				if( maxSimilarity < similarity) {
					maxSimilarity = similarity;
					bestTaxonomyClasses = taxonomyClasses;
				}
				
				if( classNumber == CTaxonomyModel.NUM_CLASSES-1) {
					buf.append("\n");
		
					String simStr = String.valueOf(similarity);
					int len = (simStr.length() < 4) ? simStr.length() : 4;
					buf.append(simStr.substring(0, len));
					if(len < 4) {
						buf.append("0");
					}
					buf.append(" ");
					buf.append(CTaxonomyModel.convertClassesToLineage(taxonomyClasses) );
				}
			}
			buf.append("\nBest lineage training:\n");
			String simStr = String.valueOf(maxSimilarity);
			int len = (simStr.length() < 4) ? simStr.length() : 4;
			buf.append(simStr.substring(0, len));
			if(len < 4) {
				buf.append("0");
			}
			buf.append("  ");
			buf.append(CTaxonomyModel.convertClassesToLineage(bestTaxonomyClasses) );
			
			CLogger.info(buf.toString(), CLogger.TAXONOMY_TRAIN_TRACE);
		}
	}
	
	
	
	protected String extractLgAbstract(final String category) throws SQLException {
		_pCatStmt.set(1, category);		
		ResultSet rs = _pCatStmt.query();
		
		String content = null;
		String lgAbstract = null;

		if( rs.next() ) {
			lgAbstract = rs.getString("lgabstract");
		}
		if( lgAbstract != null && lgAbstract.length() > 256) {
			content = CStringUtil.decodeLatin1(lgAbstract);
		}
		
		return content;
	}
}

// ------------------------- EOF ---------------------------------------
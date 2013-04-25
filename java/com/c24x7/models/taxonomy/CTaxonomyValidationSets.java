// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;
import com.c24x7.models.taxonomy.CTaxonomyModel.NLikelihood;
import com.c24x7.mapred.IDataSets;
import com.c24x7.nlservices.CDbpediaSemanticService;




			/**
			 * <p>Class to create validation sets for the Taxonomy classifier. As for
			 * the training for taxonomy model, validation leverages the Wikipedia
			 * references database.</p>
			 * 
			 * @author Patrick Nicolas
			 * @date 03/13/2012
			 */
public class CTaxonomyValidationSets extends ATaxonomyDataSets {
	private static final float THRESHOLD 	= 0.85F;
	public static final String SCORE_LABEL 	= "Sc";
	public static final String RATE_LABEL  	= "Rt";
	public static final String COUNT  		= "Ct";
	

	protected static final String VALIDATION_FILE 	= CEnv.trainingDir + "validation/taxonomy_validation";
	
	private static StringBuilder resultsBuffer = null;
	protected static boolean summaryOutput = false;
	protected static double score = 0.0F;
	private static int fileCounter = 0;
	private Map<String, Float>  _instancesWeightsMap = new HashMap<String, Float>(); 
	private Map<String, String> _taxonomyLineagesMap = new HashMap<String, String>(); 
	
		/**
		 * <p>Create a generic sets of observations for the validation of the 
		 * taxonomy model features.</p>
		 * @param indexStart index of the first Wikipedia record used in training or validation
		 * @param indexEnd  index of the last Wikipedia record used in training or validation
		 */
	protected CTaxonomyValidationSets(int startIndex, int endIndex) {
		super(startIndex, endIndex);
	}
	
	
	public IDataSets create(int startIndex, int endIndex) {
		return new CTaxonomyValidationSets(startIndex, endIndex);
	}
		

	
		/**
		 * <p>Retrieve the name of the file, that contains the results of the validation run</p>
		 * @return name of the file containing results of validation.
		 */
	public void collectResults(int[] count) {
		if( resultsBuffer == null) {
			resultsBuffer = new StringBuilder();
		}
		
		if( count != null && count.length > 1) {
			
			float similarity = 0.0F;
			for(String title : _instancesWeightsMap.keySet()) {
				resultsBuffer.append("\n\n");
				resultsBuffer.append(title);
				resultsBuffer.append(" ");
				similarity = _instancesWeightsMap.get(title).floatValue();
				if( similarity > THRESHOLD) {
					count[1]++;
				}
				score += similarity;
				resultsBuffer.append(_instancesWeightsMap.get(title));
				resultsBuffer.append("\n");
				if( _taxonomyLineagesMap.containsKey(title)) {
					resultsBuffer.append(_taxonomyLineagesMap.get(title));
				}
			}
			count[0] += _instancesWeightsMap.size();
		}
	}
	
	
	/**
	 * <p>Retrieve the name of the file, that contains the results of the validation run</p>
	 * @return name of the file containing results of validation.
	 */
	protected String getResultsFile() {
		return VALIDATION_FILE;
	}
	

		/**
		 * <p>Save the results of the validation run into file.</p>
		 * @param resultsStr textual table of results of the validation
		 * @param count array that contains the number of Wikipedia entries and the number of successful matches.
		 * @return true if the validation results are saved, false otherwise.
		 */
	public void saveResults(int[] count) {
		saveResults(fileCounter++, count);
	}
	
	
		/**
		 * <p>Save the results of the validation run into file.</p>
		 * @param resultsStr textual table of results of the validation
		 * @param fileId file identifier for a specific validation run
		 * @param count array that contains the number of Wikipedia entries and the number of successful matches.
		 * @return true if the validation results are saved, false otherwise.
		 */
	protected boolean saveResults(int fileId, int[] count) {
		boolean isResultsSaved = false;
		
		try {
			StringBuilder output = new StringBuilder();
			
			output.append(COUNT);
			output.append(CEnv.KEY_VALUE_DELIM);
			output.append(count[0]);
			output.append("\n");
			output.append(SCORE_LABEL);
			output.append(CEnv.KEY_VALUE_DELIM);
			output.append(score/count[0]);
			output.append("\n");
			output.append(RATE_LABEL);
			output.append(CEnv.KEY_VALUE_DELIM);
			output.append(((double)count[1]/count[0]));
			output.append("\n");
			output.append(CTaxonomyModel.getInstance().paramsDescription());
			
			if(!summaryOutput) {
				output.append(resultsBuffer.toString());
			}
	
			CFileUtil.write(CTaxonomyModel.getInstance().getFileName(VALIDATION_FILE) , output.toString());
			isResultsSaved = true;
		}
		catch( IOException e) {
			CLogger.error("Cannot store validation results " + e.toString());
		}
		
		return isResultsSaved;
	}
	


			/**
			 * <p>Method that adds a new observation/data point representing a 
			 * document or a document of a document, to the validation set</p>
			 * @param title  title of the abstract
			 * @param content content for which the taxonomy has to be classified
			 * @param labeledTaxonomy  taxonomy provided as label (supervised learning)
			 * @param categoriesList  keywords representing categories for the document
			 */
	@Override
	protected void addObservation(	final String 		title,  
									final String 		content, 
									final String[] 		labeledTaxonomyLineages, 
									final List<String> 	categoriesList) {
		
			/*
			 * Validation is to be performed on content of minimum length
			 */
		if( content.length() > CTaxonomyModel.MIN_VALID_CONTENT_LENGTH) {
			try {
				CDbpediaSemanticService hypernymsGenerator = new CDbpediaSemanticService();
				hypernymsGenerator.excludeTitle();
				
				//INFO
				if(title != null) {
					CLogger.info("\nKEYWORD: " + title, CLogger.TAXONOMY_TRAIN_TRACE);
				}
				
				NLikelihood selection = new NLikelihood();
				ATaxonomyNode[] bestTaxonomyLineage = hypernymsGenerator.getRelevantClasses(title, content, categoriesList, selection);
				
				double similarity = 0.0,
				       maxSimilarity = -1.0;
				
				if( bestTaxonomyLineage != null) {
					
					/*
					 * If the extraction of the best candidate or most
					 * relevant set of taxonomy instances succeed, then compare it to
					 * to the labeled taxonomy and compute the similarity.
					 */
					
					for(String labeledTaxonomyLineage : labeledTaxonomyLineages) {
						//INFO
						CLogger.info(labeledTaxonomyLineage, CLogger.TAXONOMY_TRAIN_TRACE);
						
						similarity = CTaxonomyModel.getInstance().computeSimilarity(bestTaxonomyLineage, labeledTaxonomyLineage);
						if( maxSimilarity < similarity) {
							maxSimilarity = similarity;
						}
					}
	
					if( _instancesWeightsMap.containsKey(title) ) {
						float existingSimilarity =  _instancesWeightsMap.get(title).floatValue();
						if(maxSimilarity > existingSimilarity) {
							_instancesWeightsMap.put(title, Float.valueOf((float)maxSimilarity));
						}
					}
					else {
						_instancesWeightsMap.put(title, Float.valueOf((float)maxSimilarity));
					}
					
					StringBuilder results = new StringBuilder("\n");
					results.append(content);
					results.append("\nBest taxonomy=(");
					results.append(selection.getClassId());
					results.append(CEnv.FIELD_DELIM);
					results.append(selection.getLikelihood());
					results.append(")\n");
										
					int k = 0;
					for(String labeledTaxonomyLineage : labeledTaxonomyLineages) {
						results.append(k++);
						results.append(" ");
						results.append(labeledTaxonomyLineage);
						results.append("\n");
					}
					results.append("- ");
					results.append(CTaxonomyModel.convertClassesToLineage(bestTaxonomyLineage));
					results.append("/");
					results.append(title);
	
					_taxonomyLineagesMap.put(title, results.toString());
				}
			}
			catch( SemanticAnalysisException e) {
				CLogger.error(e.toString());
			}

			synchronized (lock ) {
				displayProgress();
			}
		}
	}
}

// --------------------------------------  EOF --------------------------------------------------
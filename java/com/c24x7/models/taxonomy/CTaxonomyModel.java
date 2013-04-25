// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.exception.InitException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.models.metrics.CLineagePathDistance;
import com.c24x7.models.metrics.ILineageDistance;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;



		/**
		 * <p>Implement the features set of the taxonomy model. The model (or classifier)
		 * is created through training (CTaxonomyClass train). This class is used to
		 * hold the mean values of the model features (parameters) during training and
		 * the classifier parameters during validation and real time execution.</p>
		 * @see com.c24x7.models.leaners.CTaxonomyClass
		 * 
		 * @author Patrick Nicolas
		 * @date 02/01/2012
		 */
public final class CTaxonomyModel {
		/**
		 * File that contains the model features or parameters...
		 */
	public static final float EPS	= 1.0E-8F;
	public static final String 	MODEL_FILE 					= CEnv.modelsDir + "semantic/wordnet_model";
	public static final int		MIN_VALID_CONTENT_LENGTH 	= 450;
	public static final int 		MAX_NUM_TAXONOMY_CLASSES 	= 19;
	public static final int 		MIN_NUM_TAXONOMY_CLASSES 	= 4;
	public static final float	STD_DEV_FACTOR				= 0.98F;

	protected static final int NUM_CLASSES 			= 12;
	private static final int CLASS_NUMBER_INDEX 	= 0;
	private static final int CLASS_COUNT_INDEX 		= 1;
	private static final int AVE_LENGH_INDEX		= 2;
	private static final int WEIGHTS_MEAN_INDEX 	= 3;
	
	private static CTaxonomyModel instance = null;
	public static int numClasses = NUM_CLASSES;
	
	
	public static class NLikelihood {
		private int 	_classId = -1;
		private double 	_likelihood = 0.0F;
		
		protected NLikelihood() { }
		protected NLikelihood(int classId, double likelihood) {
			_classId = classId;
			_likelihood = likelihood;
		}
		
		public final int getClassId() {
			return _classId;
		}
		
		public final double getLikelihood() {
			return _likelihood;
		}
		
		public void set(int classId, double likelihood) {
			_classId = classId;
			_likelihood = likelihood;
		}
	}
	
			/**
			 * <p>Class that implement a model for a taxonomy instance </p>
			 * @author Patrick Nicolas
			 * @date 02/17/2012
			 */
	private class NTaxonomyLineageModel {	
		private float	_priorProbability	= 0.0F;
		private float	_lineageLength		= 0.0F;
		private float[]	_lineageWeights 	= null;
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(String.valueOf(_lineageLength));
			for(int k = 0; k < _lineageWeights.length; k++) {
				buf.append("\n");
				buf.append(_lineageWeights[k]);
			}
			
			return buf.toString();
		}
		
		
		private NTaxonomyLineageModel(	int classCount,
										float 	lineageLength,
										float[] lineageWeights) {
			
			_priorProbability = classCount;
			_lineageLength = lineageLength;
			_lineageWeights = lineageWeights;
		}

		
		private void computePriorProbability(int totalCount) {
			_priorProbability /= totalCount;
		}
		

		private float getPriorProbability() {
			return (float)Math.log(_priorProbability);
		}
		
		private double getLineageLikelyhood(final ATaxonomyNode[] taxonomyClasses) {
			double 	distance 	= 0.0,
					maxDistance = 0.0;

			for( int k = 4; k <  taxonomyClasses.length && k < _lineageWeights.length; k++) {
				distance += taxonomyClasses[k].getWeight() - _lineageWeights[k];
				maxDistance += _weightsRange[k];
			}
			
			double prob = STD_DEV_FACTOR + distance/maxDistance;
			if(prob > 1.0) {
				prob = 1.0;
			}

			return Math.log(prob);
		}
	}
	
	
	public static class NModelParams {
		private boolean 		 _hasCategories 	= false;
		private ILineageDistance _lineageDistance = null;
		
		public NModelParams() {
			this(false, new CLineagePathDistance());
		}

		public NModelParams(boolean hasCategories, ILineageDistance lineageDistance) {
			_hasCategories = hasCategories;
			_lineageDistance = lineageDistance;
		}
		
		public void addCategories() {
			_hasCategories = true;
		}
		
		public boolean hasCategories() {
			return _hasCategories;
		}
		
		public void setLineageDistance(ILineageDistance newLineageDistance) {
			_lineageDistance = newLineageDistance;
		}
		
		public final ILineageDistance getLineageDistance()  {
			return _lineageDistance;
		}
		
		public String getFileDescription() {
			StringBuilder buf = new StringBuilder(_lineageDistance.getLabel());
			buf.append("_");
			buf.append(numClasses);
			if( _hasCategories ) {
				buf.append("_C");
			}
			
			return buf.toString();
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append( _hasCategories ? "\n; Source=abstract & categories\n" : "\n; Source=abstract only\n");
			buf.append("; Distance=");
			buf.append(_lineageDistance.getLabel());

			return buf.toString();
		}
	}
	
	public static void init() throws InitException {
		init(null);
	}
	
		/**
		 * <p>Initialize the features for the taxonomy model by loading 
		 * the parameters from files.
		 * @return true if the parameters are properly loaded, false otherwise
		 */
	public static void init(NModelParams modelParams) throws InitException {
			instance = new CTaxonomyModel(modelParams);
		try {
			instance.loadModel();
		}
		catch( ArrayIndexOutOfBoundsException e) {
			throw new InitException("Cannot initialize Wordnet model");
		}	
	}
	
	
	public static CTaxonomyModel getInstance() {
		return instance;
	}
	
	public static CTaxonomyModel getInstance(NModelParams modelParams) {
		instance = new CTaxonomyModel(modelParams);
		return instance;
	}
	

	private NModelParams 			_modelParams 		= null;
	private NTaxonomyLineageModel[] _lineageModelClasses = null;
	private float[]		 			_weightsRange		= null; 


	public double getLikelihood(final ATaxonomyNode[] taxonomyClasses) {
		return computeLikelihood(taxonomyClasses).getLikelihood();
	}
	
	
		/**
		 * <p>Compute the probability of a set of taxonomy classes (or taxonomy lineage) is
		 * representative of a document.</p>
		 * @param taxonomyClasses array of taxonomy classes to be evaluated  
		 * @throws IllegalArgumentException if the array of taxonomy instances is undefined.
		 * @return Likely hood parameters.
		 */
	
	public NLikelihood computeLikelihood(final ATaxonomyNode[] taxonomyClasses) {
		if( taxonomyClasses == null) {
			throw new IllegalArgumentException("Cannot compute similarity of undefined taxonomy classes");
		}
		
		double taxonomyLikelihood = 0.0,
		       maxTaxonomyLikelihood = -Double.MAX_VALUE;
		
		int bestClassNumber = -1;
		for( int k = 0; k < _lineageModelClasses.length; k++) {

			if( _lineageModelClasses[k] != null) {
				taxonomyLikelihood = _lineageModelClasses[k].getLineageLikelyhood(taxonomyClasses);
				taxonomyLikelihood += _lineageModelClasses[k].getPriorProbability();
				
				if(maxTaxonomyLikelihood < taxonomyLikelihood) {
					bestClassNumber = k;
					maxTaxonomyLikelihood = taxonomyLikelihood;
				}
			}
		}
		
		return new NLikelihood(bestClassNumber, maxTaxonomyLikelihood);
	}

	
	public double computeSimilarity(ATaxonomyNode[] taxonomyClasses, String labeledTaxonomyLineage) {
		return _modelParams.getLineageDistance().computeSimilarity(taxonomyClasses, labeledTaxonomyLineage);
	}
	
	
	public int computeClass(ATaxonomyNode[] taxonomyClasses, String labeledTaxonomyLineage) {
		double similarity = _modelParams.getLineageDistance().computeSimilarity(taxonomyClasses, labeledTaxonomyLineage);
		
		return (int)(similarity*numClasses - EPS);
	}
	
	
	public static String convertClassesToLineage(final ATaxonomyNode[] taxonomyClasses) {
		StringBuilder buf = new StringBuilder();
		int lastClassIndex = taxonomyClasses.length-1;
		
		for( int k = 0; k < lastClassIndex; k++) {
			buf.append(taxonomyClasses[k].getLabel());
			buf.append("/");
		}
		buf.append(taxonomyClasses[lastClassIndex].getLabel());
		
		return buf.toString();
	} 
	
	public boolean hasCategories() {
		return _modelParams.hasCategories();
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int k = 0; k < _lineageModelClasses.length; k++ ) {
			buf.append("Class Number:");
			buf.append(k);
			buf.append("\n");
			buf.append(_lineageModelClasses[k].toString());
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	
				// -----------------------------
				// Private Supporting Methods
				// ----------------------------
	
	
	private CTaxonomyModel(NModelParams modelParams) { 
		_lineageModelClasses = new NTaxonomyLineageModel[numClasses];
		_modelParams = modelParams != null ? modelParams : new NModelParams();
	}
	
	
	protected boolean saveModel(final String content) {
		boolean success = false;
		
		try {
			success = CFileUtil.write(getModelFileName(), content);
		}
		catch( IOException e) {
			CLogger.error("Cannot save taxonomy model " + e.toString());
		}
		return success;
	}
	
	
	private boolean loadModel() {
		boolean succeed = false;
		List<String[]> fieldsList = new LinkedList<String[]>();
		int classNumber = -1;
		
		try {
			if( CFileUtil.readFields(getModelFileName(), "##", fieldsList, WEIGHTS_MEAN_INDEX+1) ) {
			
				float lineageLength = 0.0F;
				int countPerClass = 0,
				    totalCount = 0;
				float[] lineageWeights = null;
				
				String[] weightsStr = null;
				NTaxonomyLineageModel newLineageClass = null;
				float[] maxWeightsMean = new float[MAX_NUM_TAXONOMY_CLASSES];
				float[] minWeightsMean = new float[MAX_NUM_TAXONOMY_CLASSES];
				for( int k = 0; k < MAX_NUM_TAXONOMY_CLASSES; k++) {
					minWeightsMean[k] = 1.0F;
				}
				
					/*
					 * load the model for different lengths of taxonomy instances.
					 */
				for(String[] fields : fieldsList) {
					classNumber = Integer.valueOf(fields[CLASS_NUMBER_INDEX]);
					countPerClass = Integer.valueOf(fields[CLASS_COUNT_INDEX]);
					totalCount += countPerClass;
	
					lineageLength = Float.valueOf(fields[AVE_LENGH_INDEX]);
					if(fields[WEIGHTS_MEAN_INDEX].length() < 5) {
						break;
					}
						
					weightsStr = fields[WEIGHTS_MEAN_INDEX].split(" ");
					lineageWeights = new float[weightsStr.length];
					for( int k =0; k < weightsStr.length; k++) {
						lineageWeights[k] = Float.valueOf(weightsStr[k]);
						if( minWeightsMean[k] > lineageWeights[k] ) {
							minWeightsMean[k] = lineageWeights[k];
						}
						if( maxWeightsMean[k] < lineageWeights[k] ) {
							maxWeightsMean[k] = lineageWeights[k];
						}
					}
					
					newLineageClass = new NTaxonomyLineageModel(countPerClass, lineageLength, lineageWeights);
					_lineageModelClasses[classNumber] = newLineageClass;

					succeed = true;
				}
				
				_weightsRange = new float[MAX_NUM_TAXONOMY_CLASSES];
				for( int k = 0; k < MAX_NUM_TAXONOMY_CLASSES; k++) {
					_weightsRange[k] = 2.0F*(maxWeightsMean[k] -  minWeightsMean[k]);
					if( _weightsRange[k] < EPS) {
						_weightsRange[k] = EPS;
					}
				}
				
				for( NTaxonomyLineageModel lineageModelClass : _lineageModelClasses) {
					if( lineageModelClass != null) {
						lineageModelClass.computePriorProbability(totalCount);
					}
				}
			}
		}
		catch( ArrayIndexOutOfBoundsException e) {
			CLogger.error("Error for " + classNumber + " " + e.toString());
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		
		return succeed;
	}
	
	public String getModelFileName() {
		return getFileName(MODEL_FILE);
	}
	
	
	public String paramsDescription() {
		return _modelParams.toString();
	}
	
	public String getFileName(final String baseFileName) {
		StringBuilder fileName = new  StringBuilder(baseFileName);
		fileName.append(_modelParams.getFileDescription());
		
		return fileName.toString();
	}
}

// --------------------------  EOF -------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.taxonomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.c24x7.models.ATaxonomyNode;
import com.c24x7.mapred.IDataSets;
import com.c24x7.mapred.IModelStats;
import com.c24x7.util.logs.CLogger;



		/**
		 * <p>Class that implements the computation of statistics for 
		 * the training of the taxonomy classifier/model. The statistics are collected
		 * during training with the following model features:<br>
		 * - Relative weight (adjusted TF-IDF value) of the label or N-Gram.<br>
		 * - Relative frequency of each class of the taxonomy associated to the label.<br>
		 * - Relative weight of each class of the taxonomy.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 02/12/2012
		 * @see com.c24x7.models.learners.taxonomy.CTaxonomyModel
		 */
public final class CTaxonomyModelStats implements IModelStats {
	private final static String MODEL_DESCRIPTION = 
		";Parameters for the Taxonomy classifier - Patrick Nicolas - Feb 2012\n" +
		"; Class number\n" + 
		"; Size of training sets for this class number\n" +
		"; Ratio of length of labeled taxonomy lineage/mean length fo all other taxonomy lineages\n" +
		"; Ratio of weight of labeled classes/Mean weights all other classes\n" +
		"; Standard deviation of ratio of weight of labeled classes/Mean weights all other classes";
	
	private final static String CONTENT	= "Content";


		/**
		 * <p>Minimum number of observations required to compute the taxonomy
		 * model features accurately.</p>
		 */
	private static final int MIN_NUM_OBSERVATIONS = 0;
	
		/**
		 * <p>Class that compute the frequency and weights for
		 * a taxonomy branch (or array of instances.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/26/2012
		 */
	protected static class NTaxonomyClassWeights {
		private float[] _weights 			= null;
		private float[]	_weightsVariance		= null;		
		
		protected NTaxonomyClassWeights() {
			_weights = new float[CTaxonomyModel.MAX_NUM_TAXONOMY_CLASSES];
		}
		
		protected NTaxonomyClassWeights(float[] weights) {
			_weights = weights;
		}
		
		public float getLength() {
			return _weights.length;
		}
		
		
		protected void sum(float[] weights) {
			for( int k = 0; k < weights.length; k++) {
				_weights[k] += weights[k];
			}
		}
		
		protected void sum(NTaxonomyClassWeights fw) {
			if( fw != null && _weights != null) {
				int len = weightsLength(fw.getWeights());
				for(int k = 0; k < len; k++) {
					_weights[k] += fw.getWeights()[k];
				}
			}
		}
		
		
		protected void variance(NTaxonomyClassWeights fw, int numObs) {
			if( fw != null) {
				if( _weightsVariance == null) {
					_weightsVariance = new float[CTaxonomyModel.MAX_NUM_TAXONOMY_CLASSES];
				}
				
				int len = weightsLength(fw.getWeights());
				for( int k = 0; k < len; k++) {
					float deviation = fw.getWeights()[k] - _weights[k];
					_weightsVariance[k] += deviation*deviation;
					_weightsVariance[k] /= numObs;
				}
			}
		}
	
		protected void mean(int counter) {
			if( _weights != null) {
				for( int k = 0; k < _weights.length; k++) {
					_weights[k] /= counter;
				}
			}
		}

		protected float[] getWeights() {
			return _weights;
		}

		protected float[] getWeightsVariance() {
			return _weightsVariance;
		}
		
		protected String printTrace(final String label) {
			StringBuilder traceBuf = new StringBuilder();
			
			if( _weights != null) {
				traceBuf.append(label);
				traceBuf.append("\nWeights: ");
				for( int k = 0; k < _weights.length; k++) {
					traceBuf.append(_weights[k]);
					traceBuf.append(" ");
				}
			}
			return traceBuf.toString();
		}
		

		protected String print() {
			StringBuilder buf = new StringBuilder();
			int lastIndex = _weights.length -1;
			if( lastIndex != -1 && _weights != null) {
				for( int k = 0; k  < lastIndex; k++) {
					buf.append(_weights[k]);
					buf.append(" ");
				}
				buf.append(_weights[lastIndex]);
			}
			
			buf.append("\n;");
			if( lastIndex != -1 && _weights != null && _weightsVariance != null) {
				for( int k = 0; k  < lastIndex; k++) {
					buf.append(Math.sqrt(_weightsVariance[k]));
					buf.append(" ");
				}
				buf.append(Math.sqrt(_weightsVariance[lastIndex]));
			}
			buf.append("\n##\n");
			return buf.toString();
		}
		
		private static String printNull() {
			StringBuilder buf = new StringBuilder("X");
			buf.append("\n;");
			buf.append("X");
			buf.append("\n##\n");
			
			return buf.toString();
		}
		
		
		private int weightsLength(float[] fwWeights) {
			return (_weights.length < fwWeights.length) ? _weights.length : fwWeights.length;
		}
	}
	
			/**
			 * <p>Create a statistics entry for a set of taxonomy classes associated to 
			 * a keyword (N-Gram). The data include the relative, normalized weight of the keyword, 
			 * the distribution of the relative, normalized frequencies over the classes of the
			 * taxonomy and the distribution of the relative, normalized weights over those taxonomy
			 * classes.<br> The constructor initializes the mean and standard deviation values for
			 * the set of observations created during training. </p>
			 * 
			 * @author Patrick Nicolas
			 * @date 02/11/2012
			 */
	protected static class NTaxonomyClassStats {
		private int		_count 			= 1;
		private float	_lineageLength	= 0.0F;
		private int		_classNumber	= -1;
		
		private NTaxonomyClassWeights _fw = null;
		
		private NTaxonomyClassStats(int classNumber) {
			_classNumber = classNumber;
		}
		
		protected NTaxonomyClassStats(ATaxonomyNode[] tClasses, int classNumber) {
			float[] weights = new float[tClasses.length];
			
			for( int k = 0; k < tClasses.length; k++) {
				weights[k] = tClasses[k].getWeight();
			}
			_fw = new NTaxonomyClassWeights(weights);
			_lineageLength = tClasses.length;
			_classNumber = classNumber;
		}

		
		protected NTaxonomyClassStats(float[] classesWeights, float lineageLength, int classNumber) {
			
			if( classesWeights == null) {
				throw new IllegalArgumentException("Cannot initialize Taxonomy Model");
			}
			_count = 1;
			_fw = new NTaxonomyClassWeights(classesWeights);
			_lineageLength = lineageLength;
			_classNumber = classNumber;
		}
		
		
		private int getClassNumber() {
			return _classNumber;
		}
		

		protected NTaxonomyClassWeights getFW() {
			return _fw;
		}

		
		private float getLineageLength() {
			return _lineageLength;
		}


			/**
			 * <p>Compute the sum of model features or parameters values derived from 
			 * observations generated during the training of the Taxonomy classifier. The
			 * model features set includes the normalized TF-IDF weight of the keyword, 
			 * the normalized frequencies of the taxonomy classes and the normalized
			 * weights of the taxonomy classes.</p>
			 * 
			 * @param newStats new statistics and observations for which to compute sums.
			 */
		private void computeSums(final NTaxonomyClassStats newStats) {
			if( newStats == null) {
				throw new IllegalArgumentException("Cannot compute sum of taxonomy classes stats");
			}
			
			_count++;
			if(_fw == null) {
				_fw = new  NTaxonomyClassWeights();
				_count = 1;
			}
			_fw.sum(newStats.getFW());
			_lineageLength += newStats.getLineageLength();
		}
		
			/**
			 * <p>Compute the standard deviation of model features or parameters values derived from 
			 * observations generated during the training of the Taxonomy classifier. The
			 * model features set includes the normalized TF-IDF weight of the keyword, 
			 * the normalized frequencies of the taxonomy classes and the normalized
			 * weights of the taxonomy classes.</p>
			 * 
			 * @param newStats new statistics and observations for which to compute sums.
			 */
		private void computeVariance(final NTaxonomyClassStats stats) {
			if( stats == null) {
				throw new IllegalArgumentException("Cannot compute standard deviation of taxonomy classes stats");
			}
			
			if(_fw != null) {
				_fw.variance(stats.getFW(), _count);
			}
		}
		
		
			/**
			 * <p>Compute the mean of model features or parameters values derived from 
			 * observations generated during the training of the Taxonomy classifier. The
			 * model features set includes the normalized TF-IDF weight of the keyword, 
			 * the normalized frequencies of the taxonomy classes and the normalized
			 * weights of the taxonomy classes.</p>
			 */
		private void computeMean() {
			_lineageLength /= _count;
			if(_fw != null) {
				_fw.mean(_count);
			}
		}
		

		
		/**
		 * <pGenerate a trace for the statistics for the Taxonomy classes.</p>
		 * @return textual description of the statistics.
		 */
		public String trace() {
			StringBuilder buf = new StringBuilder();
			buf.append(_fw.printTrace(CONTENT));
	
			return buf.toString();
		}
		
		
			/**
			 * <p>Generate the results string to be stored into the model file.</p>
			 * @return statistics for this taxonomy model statistics 
			 */
		private String saveModel() {				
			StringBuilder buf = new StringBuilder();

			buf.append(_count);
			buf.append("\n##\n");
			buf.append(_lineageLength);
			buf.append("\n##\n");
			if( _fw != null ) {
				buf.append(_fw.print());
			}
			else {
				buf.append(NTaxonomyClassWeights.printNull());
			}
			
			return buf.toString();
		}
	}
	
	
	private Map<Integer, List<NTaxonomyClassStats>> _taxonomyClassesMap = null;
	

	/**
	 * <p>Create a statistics set for a taxonomy classifier or model. This 
	 * constructor is used to compute the mean and standard deviation of the
	 * model parameters values collected during training.</p>
	 */
	public CTaxonomyModelStats() {
		super();
		_taxonomyClassesMap = new HashMap<Integer, List<NTaxonomyClassStats>>();
	}
	
			/**
			 * <p>Compute the mean and standard deviation of the model features data
			 * (or observations) generated during training. Those model features set
			 * includes the normalized TF-IDF weight of the keyword, 
			 * the normalized frequencies of the taxonomy classes and the normalized
			 * weights of the taxonomy classes.</p>
			 * 
			 * @param observationsList list of observations collect during the training or validation.
			 * @exception IllegalArgumentException if the observations list is undefined.
			 */
	
	public boolean compute(IDataSets dataSet) {
		List<NTaxonomyClassStats> observationsList = CTaxonomyTrainingSets.getObservationsList();
			
		if( observationsList == null) {
			throw new IllegalArgumentException("Cannot compute model parameters from undefined observations");
		}
		
		NTaxonomyClassStats[] modelStats = null;
		if( observationsList.size() >= MIN_NUM_OBSERVATIONS ) {

			List<NTaxonomyClassStats> classStatsList = null;
			int classNumber = -1;
			Integer classNumberInt = null;
			
				/*
				 * Load the statistics collected during training.
				 */
			for( NTaxonomyClassStats observation : observationsList) {
				classNumber = observation.getClassNumber();
				classNumberInt = Integer.valueOf(classNumber);
					
				if(_taxonomyClassesMap.containsKey(classNumberInt)) {
					classStatsList = _taxonomyClassesMap.get(classNumberInt);
					classStatsList.add(observation);
				}
				else {
					classStatsList = new ArrayList<NTaxonomyClassStats>();
					classStatsList.add(observation);
					_taxonomyClassesMap.put(classNumberInt, classStatsList);
				}
			}
			
			
			modelStats = new NTaxonomyClassStats[CTaxonomyModel.numClasses];
			for(int k = 0; k < modelStats.length; k++) {
				modelStats[k] = new NTaxonomyClassStats(k);
			}
			
				/*
				 * Compute the absolute mean for this set of training observations..
				 */
			for( Integer classNumInt : _taxonomyClassesMap.keySet() ) {
				classStatsList = _taxonomyClassesMap.get(classNumInt);
				classNumber = classNumInt.intValue();
				for( NTaxonomyClassStats classStats : classStatsList) {
					modelStats[classNumber].computeSums(classStats);
				}
			}
			
				/*
				 * Compute the mean for those observations.
				 */
			for(int k = 0; k <  modelStats.length; k++) {
				modelStats[k].computeMean();
			}
						
				/*
				 * Compute the standard deviation for those observations.
				 */
			for( Integer classNumInt : _taxonomyClassesMap.keySet() ) {
				classStatsList = _taxonomyClassesMap.get(classNumInt);
				classNumber = classNumInt.intValue();
				for( NTaxonomyClassStats classStats : classStatsList) {
					modelStats[classNumber].computeVariance(classStats);
				}
			}

		}
		if(modelStats == null || !saveModel(modelStats) ) {
			CLogger.error("Failed to save model ");
		}
		
		return true;
	}



	
				// ---------------------------
				//  Private Supporting Methods
				// --------------------------
	
	
	private boolean saveModel(NTaxonomyClassStats[] modelStats) {
		StringBuilder buf = new StringBuilder(MODEL_DESCRIPTION);	
		buf.append(CTaxonomyModel.getInstance().paramsDescription());
		buf.append("\n");
		
		for( int k = 0; k < modelStats.length; k++) {
			buf.append(k);
			buf.append("\n");
			buf.append("##");
			buf.append("\n");
			buf.append(modelStats[k].saveModel());
		}
		return CTaxonomyModel.getInstance().saveModel(buf.toString());
	}
	
}

// -------------------------- EOF ----------------------------------------------

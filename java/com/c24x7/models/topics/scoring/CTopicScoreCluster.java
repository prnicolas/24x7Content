/*
 * Copyright (C) 2010-2012  Patrick Nicolas
 */
package com.c24x7.models.topics.scoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.exception.ClassifierException;
import com.c24x7.math.clustering.CDataPoint;
import com.c24x7.math.clustering.CKMeansClustering;
import com.c24x7.models.topics.CTopicClassifier;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;


		/**
		 * <p>Topic scoring class that relies on the K-Means
		 * clustering algorithm.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date July 8, 2012 1:10:42 PM
		 */
public final class CTopicScoreCluster extends ATopicScore {
		/**
		 * Number of clusters used in the classifier 
		 */
	public static final int NUM_CLUSTERS = 5;
	
		/**
		 * Maximum number of iterations for this K-Means
		 * clustering algorithm to converge.
		 */
	public static final int MAX_KMEANS_ITERATIONS 	= 150;
	private static final String LABEL = "K-Means Clustering";
	private static final String TOPICS_CLUSTERS_FILE	= CEnv.modelsDir + "semantic/topics_model_clustering";

	private int 				_index 			= 0;
	private List<CDataPoint> 	_dataPointsList = null;
	private double[] 		 	_maxValues 		= null;
	private CKMeansClustering 	_clusters 		= null;
	private int					_bestClusterId	= -1;
	
	/**
	 * <p>Load the model features associated with each cluster.</p>
	 * @throws IOException if the model parameters are not available
	 */
	@Override
	public void loadModel() throws IOException {
		/*
		 * Load the parameters from the model file.
		 */
		Map<String, String> parametersMap = new HashMap<String, String>();
		CFileUtil.readKeysValues(TOPICS_CLUSTERS_FILE, parametersMap);
		
		String[] clusterValues = null;
		double[] values = null;
		
		/*
		 * Extract the model features as a list of array of 
		 * floating values..
		 */
		List<double[]> clusterData = new LinkedList<double[]>();
		
		double scoreValue = 0.0;
		for( String clusterValuesStr : parametersMap.values()) {
			clusterValues = clusterValuesStr.split(CEnv.FIELD_DELIM);
			
			/*
			 * load all the model features for each cluster
			 */
			values = new double[clusterValues.length];
			for( int k = 0; k < clusterValues.length; k++) {
				values[k] = Double.parseDouble(clusterValues[k]);
			}
			
			/*
			 * Track the cluster with the highest score as
			 * the cluster that contains the most relevant topic.
			 */
			if(values[0] > scoreValue) {
				scoreValue = values[0];
				_bestClusterId = clusterData.size();
			}
			clusterData.add(values);
 		}
		
		_clusters = new CKMeansClustering(clusterData);
	}

	
		/**
		 * <p>Retrieve the type of classifier.
		 */
	@Override
	public String getType() {
		return LABEL;
	}

	
	/**
	 * <p>Add a new observation vector (floating point values) to this 
	 * unsupervised scoring algorithm. The components of the vector is normalized.</p>
	 * @param data new observations vector
	 * @throws IllegalArgumentException if the vector is undefined or incomplete.
	 */
	@Override
	public void addData(double[] data) {
		if( data == null || data.length != CTopicClassifier.SIZE_LABELED_OBSERVATION) {
			throw new IllegalArgumentException("Cannot add undefined data as input to Naive Bayes");
		}
		
		/*
		 * Format the dynamic array to hold observed data
		 */
		double[] newData = Arrays.copyOf(data, CTopicClassifier.SIZE_LABELED_OBSERVATION);
		
		if( _maxValues == null) {
			_maxValues = new double[CTopicClassifier.SIZE_LABELED_OBSERVATION];
		}
		for( int k = 0; k < data.length; k++) {
			if(data[k] > _maxValues[k]) {
				_maxValues[k] = data[k];
			}
		}
		if( _dataPointsList == null) {
			_dataPointsList = new ArrayList<CDataPoint>();
		}
		
		_dataPointsList.add(new CDataPoint(newData, _index));
		_index++;
	}

	
	@Override
	public int train() throws ClassifierException {
		CKMeansClustering kmeanClustering = null;
		
		if( _dataPointsList.size() > 0) {
			
			/*
			 * normalize the data points so the floating point value
			 * belongs to [0,1] segment.
			 */
			for( CDataPoint dataPoint : _dataPointsList) {
				dataPoint.normalize(_maxValues);
			}
			
			/*
			 * Iterate through the clusters 
			 */
			kmeanClustering = new CKMeansClustering(NUM_CLUSTERS, MAX_KMEANS_ITERATIONS, _dataPointsList);
			kmeanClustering.train();
			
			/*
			 * Store the parameters for the cluster model into file.
			 */
			try {
				StringBuilder buf =  new StringBuilder("\n; Clustered taxonomy classes candidates\n");
				buf.append(kmeanClustering.toString());

				CFileUtil.write(TOPICS_CLUSTERS_FILE, buf.toString());
			}
			catch(IOException e) {
				throw new ClassifierException("Cannot save results of clustering in file " + e.toString());
			}
		}
		
		return _dataPointsList.size() ;
	}
	
	

	@Override
	public double score(double[] values) {
		return _bestClusterId == _clusters.classify(values) ? 2.0 : 0.0;
	}

}

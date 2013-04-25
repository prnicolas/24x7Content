// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.math.clustering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.math.clustering.CKMeansCluster.NCentroid;




		/**
		 * <p>Class that implements the K-Means unsupervised learning algorithm. The algorithm
		 * is composed of a list of cluster and initial set of normalized data points.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 12/11/2011
		 */
public final class CKMeansClustering {
	private final static int 	MAX_ITERATIONS 							= 1000;
	private final static int 	MIN_NUM_CONVERGING_ITERATIONS 			= 12;
	private final static float 	CONVERGENCE_CRITERIA 					= 0.015F; 
	
	private CKMeansCluster[] 	_clusters				= null;
	private int 				_maxIterations 			= MAX_ITERATIONS;
	private CDataPoint[] 		_dataPointsList 		= null;	
	private double 				_totalDistance 			= 0.0;
	private int					_convergenceCounter 	= 0;
	private	String[]			_labels 				= null;
	private float				_convergenceCriteria 	= CONVERGENCE_CRITERIA;
	private int					_minNumConvergingIterations = MIN_NUM_CONVERGING_ITERATIONS;
	private NCentroid[]			_centroids				= null;
	
	
		/**
		 * <p>Create a K-Means clustering algorithm for a list of predefined
		 * centroid vectors (or coordinates). This constructor is called during
		 * real-time classification.</p>
		 * @param centroidDataList list of centroid values (vector of floating point values between 0 & 1.
		 */
	public CKMeansClustering(List<double[]> centroidDataList) {
		_centroids = new NCentroid[centroidDataList.size()];
		
		int index = 0;
		for( double[] centroidValues : centroidDataList) {
			_centroids[index] = new NCentroid();
			_centroids[index].set(centroidValues);
			index++;
		}
	}
	
	
	
		/**
		 * <p>Create a KMeans Clustering model with k clusters, a maximum number of
		 * iterations and a series of data points.</p>
		 * @param numClusters  Initial number of clusters
		 * @param dataPointsList list of data points
		 */
	public CKMeansClustering(int numClusters, final CDataPoint[] dataPointsList) {
		_clusters = new CKMeansCluster[numClusters];
		for (int i = 0; i < numClusters; i++) {
			_clusters[i] = new CKMeansCluster(i);
	    }
		_dataPointsList = dataPointsList;

	}
	
			/**
			 * <p>Create a KMeans Clustering model with k clusters, a maximum number of
			 * iterations and a series of data points. This constructor is called during
			 * training of the model.</p>
			 * @param numClusters  Initial number of clusters
			 * @param dataPointsList list of data points
			 */
	public CKMeansClustering(int numClusters, 
							 final List<CDataPoint> dataPointsList) {
		this(numClusters, MAX_ITERATIONS, dataPointsList);
	}

	
			/**
			 * <p>Create a KMeans Clustering model with k clusters, a maximum number of
			 * iterations and a series of data points.  This constructor is called during
			 * training of the model.</p>
			 * @param numClusters  Initial number of clusters
			 * @param iterations maximum number of iterations for clustering
			 * @param dataPointsList list of data points
			 */
	public CKMeansClustering(int 	numClusters,  
							 int 	iterations, 
							 final List<CDataPoint> dataPointsList) {
		
		_clusters = new CKMeansCluster[numClusters];
		for (int i = 0; i < numClusters; i++) {
			_clusters[i] = new CKMeansCluster(i);
	    }
		_maxIterations = iterations;
		_dataPointsList = dataPointsList.toArray(new CDataPoint[0]);
	}



			
	
	public final CKMeansCluster[] getClusters() {
		return _clusters;
	}
	
	
	
	public final List<double[]> getCentroidsData() {
		List<double[]> centroidDataList = null;
		
		if(_clusters != null && _clusters.length > 0) {
			centroidDataList = new LinkedList<double[]>();
		
			for( CKMeansCluster cluster : _clusters) {
				centroidDataList.add(cluster.getCentroid().getX());
			}
		}
		
		return centroidDataList;
	}
	
	
			/**
			 * <p>Retrieve the list of all the data points for each cluster.</p>
			 * @return list of list (per cluster) of data points 
			 */
	public final List<List<CDataPoint>> getClusterResults() {
		List<List<CDataPoint>> datapointsLists = new ArrayList<List<CDataPoint>>();
		for( CKMeansCluster cluster : _clusters) {
			datapointsLists.add(cluster.getDataPoints());
		}
		
		return datapointsLists;
	}
	
	
	
	public void setLabels(final String[] labels) {
		_labels = labels;
	}
	
	
	
	public void setConvergenceCriteria(float convergenceCriteria) {
		_convergenceCriteria = convergenceCriteria;
	}
	
	public void setMinNumConvergingIterations(int minNumConvergingIterations) {
		_minNumConvergingIterations = minNumConvergingIterations;
	}

	
	
		/**
		 * <p>Implements the K-Means unsupervised learning algorithms. The algorithm
		 * iterates to minimize the total sum of squares of distance between each cluster
		 * data points and its centroid.</p>
		 */
	public int train() {
		int numIterations = _maxIterations;
		initialize();
		
		int k = 0;
		
		boolean inProgress = true;
		while(inProgress) {
			for(CKMeansCluster cluster : _clusters ) {
				cluster.attach(_dataPointsList[k]);
				if( ++k >= _dataPointsList.length) {
					inProgress = false;
					break;
				}
			}
		}
		
	        /*
	         * Initial computation of the total sum of distance
	         * and compute the centroid for each cluster.
	         */
		computeTotalDistance();
		
		for(CKMeansCluster cluster : _clusters ) {
			cluster.computeCentroid();
		}
		
	        /*
	         * computation of the total sum of distance
	         */	        		
		computeTotalDistance();
		
			/*
			 * Compute the least sum of squares within the 
			 * number of maximum of iterations.
			 */
		List<CDataPoint> dataPointsList = null;	
		CKMeansCluster bestCluster = null;
		
		for (int i = 0; i < _maxIterations; i++) {
	           
			for(CKMeansCluster cluster : _clusters ) {	
					/*
					 * We need to close the list of data points 
					 * for this cluster as the content of those
					 * lists have been changed.
					 */
				dataPointsList = new ArrayList<CDataPoint>();
				for( CDataPoint point : cluster.getDataPointsList()) {
					dataPointsList.add(point);
				}
				
				for( CDataPoint point : dataPointsList) {
					double minDistance = Double.MAX_VALUE,
					       distance = 0.0;
					bestCluster = null;
					
					/*
					 * compute the best Cluster for each point.
					 */
					for(CKMeansCluster cursor : _clusters ) {
						distance =  point.computeDistance(cursor.getCentroid());
						if( minDistance >  distance) {
							minDistance = distance;
							bestCluster = cursor;
						}
					}
					
					updateDataPoints(point, cluster, bestCluster);
				}
			}
			
				/*
				 * If no exchange of data points happens between
				 * the clusters, then start counting convergence attempts.
				 */
			if( _convergenceCounter >= _minNumConvergingIterations ) {
				numIterations= i;
				break;
			}
		}
		
		return numIterations;
	}
	
	
		/**
		 * <p>Classify the set of observations by extracting the cluster
		 * id containing the set of observations.
		 * @param observation
		 * @return cluster id as a floating point value.
		 */
	public double classify(double[] observation) {
		double bestScore = Double.MAX_VALUE,
		       distance = 0.0;
		int clusterId = -1;
		
		for(int k = 0; k < _centroids.length; k++) {
			distance = _centroids[k].computeDistance(observation);
			if( distance < bestScore) {
				bestScore = distance;
				clusterId = k;
			}
		}
		return clusterId;
	}


	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if( _labels != null) {
			for( String label : _labels) {
				buf.append("; ");
				buf.append(label);
				buf.append("\n");
			}
			buf.append("; --------------------------\n");
		}
	
		for( CKMeansCluster cluster : _clusters ) {
			buf.append(cluster.printCentroid());
			buf.append("\n");
		}
		buf.append("\n");
	
		return buf.toString();
	}
	
	
	
						// ------------------------------
						// Private Supporting Methods
						// ----------------------------
				
	private void computeTotalDistance() {
		float totalDistance = 0.0F;
		for(CKMeansCluster cluster : _clusters ) {
			totalDistance += cluster.getSumSquares();
		}
		
		double error = _totalDistance - totalDistance;
		if( error < 0.0) {
			error = -error;
		}
		if( error < _convergenceCriteria) {
			_convergenceCounter++;
		}
		else {
			_convergenceCounter = 0;
		}
		_totalDistance = totalDistance;
	}

	
	private void initialize() {
		double[] params = getParameters();
		int numVariables = params.length>>1;
		
		double[] range = new double[numVariables];
		for( int k = 0, j = numVariables; k <  numVariables; k++, j++ ) {
			range[k] = params[k] - params[j];
		}
		    		
		double[] x = new double[numVariables];
		int sz_1 = _clusters.length+1, 
		    m = 1;

		for(CKMeansCluster cluster : _clusters) {
			for( int k = 0, j = numVariables; k <  numVariables; k++, j++ ) {
				x[k] = ((range[k]/sz_1)*m) + params[j];
			}
			cluster.setCentroid(x);
			m++;
		}
	}
	
	
	
	private double[] getParameters() {
		int numVariables = _dataPointsList[0].getNumVariables();
		
		double[] params = new double[(numVariables<<1)];
		for( int k = 0, j = numVariables; k < numVariables; k++, j++) {
			params[k] = Double.MIN_VALUE;
			params[j] = Double.MAX_VALUE;
		}

		for(CDataPoint point : _dataPointsList) {
			
			for(int k = 0, j = numVariables; k < numVariables; k++, j++) {
				if( point.get(k) > params[k]) {
					params[k] = point.get(k);
				}
				else if( point.get(k) < params[j]) {
					params[j] = point.get(k);
				}
			}
		}
		
		return params;
	}	
	
	
	private void updateDataPoints(	CDataPoint 		point, 
									CKMeansCluster 	cluster, 
									CKMeansCluster 	bestCluster) {
		boolean update = bestCluster != null && bestCluster != cluster;
		
		if( update ) {
			bestCluster.attach(point);
			cluster.detach(point);
			
			for(CKMeansCluster cursor : _clusters ) {
				cursor.computeCentroid();
			}
			computeTotalDistance();
		}
	}
	
}

// ------------------------------------------  EOF -----------------------------------

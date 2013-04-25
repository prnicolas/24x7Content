// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.math.clustering;

import com.c24x7.math.clustering.CKMeansCluster.NCentroid;


		/**
		 * <p>Class that contains a data entry to be clustered using KMeans 
		 * minimization of Least Square. A data entry is composed of a 
		 * coordinates (x,y) and an algorithm to compute the distance between 
		 * this data point and the cluster centroid.</p>
		 * @author Patrick Nicolas
		 * @date 12/14/2011
		 */
public final class CDataPoint {
	private static NDistance distanceMetrics = new NEuclideanDistance();
	
	/**
	 * <p>Set-up the distance metrics used to evaluate distance
	 * between vector within clusters.</p>
	 * @param newDistanceMetrics distance metrics used in creating clusters.
	 */
	public static void setDistanceMetrics(final NDistance newDistanceMetrics) {
		distanceMetrics = newDistanceMetrics;
	}
	
		/**
		 * <p>Interface for the methods to compute the distance between a 
		 * data point and the cluster centroid.</p>
		 * @author Patrick Nicolas
		 * @date 12/16/2011
		 */
	protected interface NDistance {
			/**
			 * <p>Compute the distance between a data point (x,y) and a cluster centroid.</p>
			 * @param x  x-coordinate of the data point
			 * @param y  y-coordinate of the data point
			 * @param centroid cluster centroid
			 * @return distance between the data point and the centroid
			 */
		public double compute(double[] x, NCentroid centroid);
	}
	
	
			/**
			 * <p>Class that implements the Manhattan distance between a 
			 * data point and the cluster centroid.</p>
			 * @author Patrick Nicolas
			 * @date 12/16/2011
			 */
	protected static class NManhattanDistance implements NDistance {
		/**
		 * <p>Compute the Manhattan distance between a data point (x,y) and a cluster centroid.</p>
		 * @param x  x-coordinate of the data point
		 * @param y  y-coordinate of the data point
		 * @param centroid cluster centroid
		 * @return distance between the data point and the centroid
		 */
		public double compute(double[] x, NCentroid centroid) {
			double sum = 0.0;
			double xx = 0.0;
			
			for( int k = 0; k < x.length; k++) {
				xx = x[k] - centroid.get(k);
				if( xx < 0.0) {
					xx = -xx;
				}
				sum += xx;
			}
			
			return sum;
		}
	}
	
	
	
			/**
			 * <p>Class that implements the Euclidean distance between a 
			 * data point and the cluster centroid.</p>
			 * @author Patrick Nicolas
			 * @date 12/16/2011
			 */
	protected static class NEuclideanDistance implements NDistance {
		/**
		 * <p>Compute the Euclidean distance between a data point (x,y) and a cluster centroid.</p>
		 * @param x  x-coordinate of the data point
		 * @param y  y-coordinate of the data point
		 * @param centroid cluster centroid
		 * @return distance between the data point and the centroid
		 */
		public double compute(double[] x, NCentroid centroid) {
			double sum = 0.0;
			double xx = 0.0;
			
			for( int k = 0; k < x.length; k++) {
				xx = x[k] - centroid.get(k);
				sum += xx*xx;
			}
			
	        return Math.sqrt(sum);
		}	
	}
		
	
	
	private double[] _x 	= null;
	private int		_index 	= -1;

	
		/**
		 * <p>Create a data point with the coordinate x,y and an arbitrary distance metrics</p>
		 * @param x array or vector of floating point values
		 * @param index or rank of the observation
		 */
	public CDataPoint(double[] x, int index) {
		_x = x;
		_index = index;
	}
	
		/**
		 * <p>Create a data point with the coordinate x,y and an arbitrary distance metrics</p>
		 * @param x x-coordinate of the data point
		 * @param y y-coordinate of the data point
		 * @param index or rank of the observation
		 */
	public CDataPoint(int x, int y, int index) {
		_x = new double[2];
		_x[0] = x;
		_x[1] = y;
		_index = index;
	}


		/**
		 * <p>Compute the distance between the data point and the centroid.</p>
		 * @param centroid Cluster centroid
		 * @return distance between data point and centroid
		 */
	public double computeDistance(final NCentroid centroid) {
		return distanceMetrics.compute(_x, centroid);
	}
	
	
		/**
		 * <p>Normalize the model features (or parameters) against the maximum
		 * value for each of the feature or model parameters.</p>
		 * @param maxValues array of maximum values for the model features.
		 */
	public void normalize(double[] maxValues) {
		for( int k = 0; k < _x.length; k++) {
			_x[k] /= maxValues[k];
		}
	}

		/**
		 * <p>Access the kth coordinate of the data vector.</p>
		 * @param k index of the variable
		 * @return  kth coordinate
		 */
	public final double get(int k) {
		return _x[k];
	}
	
	public final int getNumVariables() {
		return _x.length;
	}

	
	/**
	 * <p>Get index of this data point.</p>
	 * @return index of this data point
	 */
	public final int getIndex() {
		return _index;
	}
	
	
	/**
	 * <p>Display the ordered list of data points.</p>
	 * @return characters string of floating point values.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_index);
		for( int k = 0; k < _x.length; k++) {
			buf.append(" ");
			buf.append(_x[k]);
		}
		
		return buf.toString();
	}
}

// ---------------------------------------------  EOF ----------------------------------
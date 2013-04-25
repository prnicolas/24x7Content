// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.math.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.c24x7.util.CEnv;


			/**
			 * <p>Implements a cluster of data point with centroid for the KMeans
			 * algorithm. A cluster is composed of a centroid, a list of data point and 
			 * an index.</p>
			 * @author Patrick Nicolas
			 * @date 12.19.2011
			 */

public final class CKMeansCluster {
	
			/**
			 * <p>Nested class that implements the attributes and methods for computing
			 * a cluster centroid. A centroid is defined by its x &  y coordinates.</p>
			 * @author Patrick Nicolas
			 * @date 12/15/2011
			 */
	protected static class NCentroid {
		private double[] _x = null;
		
		
		protected NCentroid() {}
		
		protected void set(double[] x) {
			_x = x;
		}
		
			/**
			 * <p>Create a centroid with initial values.</p>
			 * @param x initial value for the centroid variables.
			 */
		protected NCentroid(double[] x) {
			_x = new double[x.length];
			for( int k = 0; k < x.length; k++) {
				_x[k] = x[k];
			}
		}
		
		
		protected double computeDistance(double[] values) {
			double sum = 0.0,
			       diff = 0.0;
			
			/*
			 * The first data is the score for the cluster 
			 * and should not be counted.
			 */
			for( int k = 1; k < _x.length; k++) {
				diff = _x[k] - values[k-1];
				sum += diff*diff;
			}
			
			return sum;
		}

			/**
			 * <p>Compute the x and y coordinate of the centroid of a cluster.</p>
			 * @param cluster cluster for which the centroid is to be computed.
			 */
		protected void compute(final List<CDataPoint> dataPointsList)  {
			double[] x = new double[_x.length];
			Arrays.fill(x, 0.0);
					
			for( CDataPoint point : dataPointsList ) {
				for(int k =0; k < x.length; k++) {
					x[k] += point.get(k);
				}
			}
		        
			int numPoints = dataPointsList.size();
			for(int k =0; k < x.length; k++) {
				_x[k] = x[k]/numPoints;
			}
		}

		protected double get(int rank) {
			return _x[rank];
		}
		
		protected double[] getX() {
			return _x;
		}

		
			/**
			 * <p>Textual representation of a cluster centroid.</p>
			 * @return string of coordinates.
			 */
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			int lastDataIndex = _x.length-1;
			
			for( int k = 0; k < lastDataIndex; k++) {
				buf.append(_x[k]);
				buf.append(CEnv.FIELD_DELIM);
			}
			buf.append(_x[lastDataIndex]);
			buf.append("\n");
			
			return buf.toString();
		}
	}
	
	private int 			 _index 		= -1;
	private NCentroid 		 _centroid 		= null;	
	private double 	 		 _sumSquares 	= 0.0;
	private List<CDataPoint> _dataPointsList = null;

		/**
		 * <p>Create a Cluster for KMeans algorithm with an identifier. A cluster is composed
		 * of a series of data points, a centroid and an index.</p>
		 * @param index identifier for this cluster
		 */
	public CKMeansCluster(int index) {
		_index = index;
		_dataPointsList = new ArrayList<CDataPoint>();
	}

		/**
		 * <p>Create a new centroid for this cluster.</p>
		 * @param x x-coordinate for this centroid
		 * @param y y-coordinate for this centroid
		 */
	public void setCentroid(double[] x) {
		_centroid = new NCentroid(x);
	}


		/**
		 * <p>Access the centroid of this cluster.</p>
		 * @return cluster centroid
		 */
	public NCentroid getCentroid() {
		return _centroid;
	}

	
		/**
		 * <p>Access the centroid of this cluster.</p>
		 * @return cluster centroid
		 */
	public void computeCentroid() {
		_centroid.compute(_dataPointsList);
		
		for( CDataPoint point : _dataPointsList ) {
			point.computeDistance(_centroid);
		}
		computeSumOfSquares();
	}

	    
		/**
		 * <p>Add or attach a new data point to this cluster.</p>
		 * @param point to be added to this cluster
		 */
	public void attach(final CDataPoint point) { 
		point.computeDistance(_centroid);
		_dataPointsList.add(point);
		computeSumOfSquares();
	}

			/**
			 * <p>Detach or remove a new data point from this cluster.</p>
			 * @param point to be removed from this cluster
			 */
	public void detach(final CDataPoint point) {
		_dataPointsList.remove(point);
		computeSumOfSquares();
	}


	public final List<CDataPoint> getDataPointsList() {
		return _dataPointsList;
	}

		/**
		 * <p>Compute the sum of the squares for all the data points.
		 */
	public void computeSumOfSquares() { 
		_sumSquares = 0.0;
	        
	    for( CDataPoint point : _dataPointsList) {
	    	_sumSquares += point.computeDistance(_centroid);
	    }
	}

	public double getSumSquares() {
		return _sumSquares;
	}

	public final int getIndex() {
		return _index;
	}
	public final String getName() {
		StringBuilder buf = new StringBuilder();
		buf.append("Cluster_");
		buf.append(_index);
		return buf.toString();
	}

	public List<CDataPoint> getDataPoints() {
		return _dataPointsList;
	}
	

	public String printCentroid() {
		StringBuilder buf = new StringBuilder(getName());
		buf.append(CEnv.KEY_VALUE_DELIM);
		buf.append(_centroid.toString());
		_index++;
		return buf.toString();
	}
	
		/**
		 * <p>Textual representation of a cluster.<p>
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getName());
		buf.append(" ");
		buf.append(_centroid.toString());
		buf.append(" SumSquares=");
		buf.append(_sumSquares);
		
		for(CDataPoint point : _dataPointsList) {
			buf.append("\n");
			buf.append(point.toString());
		}
		
		return buf.toString();		
	}
}

// --------------------------  EOF ---------------------------------
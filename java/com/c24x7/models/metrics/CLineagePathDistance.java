// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.metrics;


import com.c24x7.models.ATaxonomyNode;
import com.c24x7.util.CEnv;





			/**
			 * <p>Implement the distance between two taxonomy lineages using the
			 * simple path algorithm. The algorithm matches each node or class in the
			 * taxonomy lineage using either a fuzzy or strict match (default fuzzy match).
			 * The algorithm traverse the taxonomy lineage from the leaf (taxonomy instance 
			 * or semantic taxonomyInstance).
			 * @author Patrick Nicolas
			 * @date 04/11/2012
			 */

public class CLineagePathDistance implements ILineageDistance {
	public final static double STRICT_DISTANCE 	= 1.00;
	public final static double FUZZY_DISTANCE 	= 0.93;
	private final static String LABEL = "_P";
	private static CLineagePathDistance instance = null;
	
			/**
			 * <p>Return the singleton for this distance metrics.</p>
			 * @return instance of the path distance algorithm
			 */
	public static CLineagePathDistance getInstance() {
		if( instance == null) {
			instance = new CLineagePathDistance();
		}
		
		return instance;
	}
	
	
	private boolean _fuzzyMatch = true;
	
		/**
		 * <p>Set the algorithm to match taxonomy node (or class) as a strict 
		 * comparison of the taxonomy node label.</p>
		 */
	public void setStrictMatch() {
		_fuzzyMatch = false;
	}
	
	
		/**
		 * <p>Compute the similarity (or complementary value of the actual distance using
		 * path algorithm) between a taxonomy lineage (hypernyms hierarchy) defined as
		 * an array of taxonomy classes or nodes and lineage defined as a '/' delimited string.
		 * @param  taxonomyClasses array of taxonomy class defining the first lineage
		 * @param labeledTaxonomy the second taxonomy lineage defined as a single characters string
		 * @return floating value in the range of 0, 1
		 */
	@Override
	public double computeSimilarity(final ATaxonomyNode[] taxonomyClasses, final String labeledTaxonomy) {
		String[] labeledTaxonomyClasses = labeledTaxonomy.split("/");
		return computeSimilarity(taxonomyClasses, labeledTaxonomyClasses);
	}
		
	/**
	 * <p>Compute the similarity (or complementary value of the actual distance using
	 * path algorithm) between a taxonomy lineage (hypernyms hierarchy) defined as
	 * an array of taxonomy classes or nodes and lineage defined as an array of 
	 * taxonomy class labels.
	 * @param  taxonomyClasses array of taxonomy class defining the first lineage
	 * @param labeledTaxonomyClasses taxonomy lineage defined as an array of labels of the second lineage.
	 * @return floating value in the range of 0, 1
	 */
	
	public double computeSimilarity(final ATaxonomyNode[] taxonomyClasses, 
									final String[] labeledTaxonomyClasses) {
		double similarity = 0.0;
		int maxPathLen = taxonomyClasses.length + labeledTaxonomyClasses.length-2;
		
		int j = 0;
		for(int k = taxonomyClasses.length-1; k >= 0; k--) {
			for( j = labeledTaxonomyClasses.length-1; j >= 0; j--) {
				similarity = getSimilarity(taxonomyClasses, labeledTaxonomyClasses, k, j);
				if( similarity > 0.0) {
					break;
				}
			}
			
			if(similarity > 0.0) {
				break;
			}
		}
		
		similarity /= maxPathLen;
		return similarity;
	}

	
	
	@Override
	public String getLabel() {
		return LABEL;
	}
	
	
	
	protected double getSimilarity(	final ATaxonomyNode[] taxonomyClasses, 
									final String[] labeledTaxonomyClasses, 
									int k, 
									int j) {
		double similarity = 0.0;
		
		if(strictMatch(taxonomyClasses[k].getLabel(), labeledTaxonomyClasses[j])) {
			similarity = k+j;
		}
		else if( _fuzzyMatch &&
				(k == taxonomyClasses.length-1 || j == labeledTaxonomyClasses.length-1) &&
				looseMatch(taxonomyClasses[k].getLabel(), labeledTaxonomyClasses[j])) {
			similarity = FUZZY_DISTANCE*(k+j);
		}

		return similarity;
	}
	

	
	
	protected boolean strictMatch(final String leafClass, final String labeledLeafClass) {
		return (labeledLeafClass.compareTo(leafClass) ==0);
	}
	
	private boolean looseMatch(final String leafClass, final String labeledLeafClass) {
		boolean found = false; 
	
		String[] labeledLeafTerms = labeledLeafClass.split(CEnv.FIELD_DELIM);
		String[] leafTerms = leafClass.split(CEnv.FIELD_DELIM);
		
		if( labeledLeafTerms.length > 1 || leafTerms.length > 1) {
			for( int j = 0; j < labeledLeafTerms.length; j++) {
				for( int i = 0; i < leafTerms.length; i++) {
					if(leafTerms[i].trim().compareTo(labeledLeafTerms[j]) == 0) {
						found = true;
						break;
					}
				}
			}
		}
		
		return found;
	}

}

// ---------------------------   EOF -------------------------------
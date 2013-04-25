package com.c24x7.models.metrics;

import com.c24x7.models.ATaxonomyNode;




			/**
			 * <p>Class that computes the semantic distance between two
			 * taxonomy lineages by computing the overlap (ratio)of the
			 * classes (or nodes) contained in those taxonomy lineages.</p>
			 * @author Patrick Nicolas
			 * @date 04/13/2012
			 * @see com.c24x7.models.metrics.ILineageDistance
			 */
public final class CLineageSimpleDistance implements ILineageDistance {
	private final static String LABEL = "_S";

	@Override
	public double computeSimilarity(final ATaxonomyNode[] taxonomyClasses, final String labeledTaxonomy) {
		String[] labeledTaxonomyClasses = labeledTaxonomy.split("/");
		int commonInstances = 0;
		
		int leafClassIndex = taxonomyClasses.length -1;
		int labeledLeafClassIndex = labeledTaxonomyClasses.length -1;
			
		/*
		 * Strict comparison between taxonomy and labeled classes except leaf classes.
		 */
		for( int k = 1; k < labeledLeafClassIndex; k++) {
			int j = 1;
			for( ; j < leafClassIndex; j++) {
				if(taxonomyClasses[j].getLabel().compareTo(labeledTaxonomyClasses[k]) == 0 ) {
					break;
				}
			}
			if( j < leafClassIndex) {
				commonInstances++;
			}
		}

		return (double)2.0*commonInstances/(labeledTaxonomyClasses.length + taxonomyClasses.length -4);
	}
	
	@Override
	public String getLabel() {
		return LABEL;
	}
}

// ---------------------------- EOF -------------------------
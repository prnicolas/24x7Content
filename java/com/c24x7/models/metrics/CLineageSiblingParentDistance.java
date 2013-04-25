package com.c24x7.models.metrics;

import com.c24x7.models.ATaxonomyNode;




public class CLineageSiblingParentDistance extends CLineagePathDistance {
	private final static String LABEL = "_SP";
	
	@Override
	public double computeSimilarity(ATaxonomyNode[] taxonomyClasses, String labeledTaxonomy) {
		
		String[] labeledTaxonomyClasses = labeledTaxonomy.split("/");
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
				for( int m = k-1, n = j-1; m >= 0 && n >=0; m--, n--) {
					if( strictMatch(taxonomyClasses[m].getLabel(), labeledTaxonomyClasses[n]) ){
						similarity += m-k+1;
						break;
					}
				}
				break;
			}
		}
		
		similarity /= maxPathLen;
		return similarity;

	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return LABEL;
	}

}

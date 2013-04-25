package com.c24x7.models.metrics;

import com.c24x7.models.ATaxonomyNode;


public interface ILineageDistance {
	public double computeSimilarity(final ATaxonomyNode[] taxonomyClasses, final String labeledTaxonomy);
	public String getLabel();
}

// ---------------------- EOF -------------------------------------------
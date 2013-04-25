/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics.scoring;

import java.io.IOException;

import com.c24x7.exception.ClassifierException;



		/**
		 * <p>Generic base class for extracting the most relevant
		 * topics from a collection of taxonomy classes using 
		 * different algorithms such as linear regression, Multinomial
		 * Naive-Bayes or K-Means clustering.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date July 8, 2012 1:13:13 PM
		 */
public abstract class ATopicScore {
	protected static final String WEIGHT_PARAM_LABEL 		= "Weight";
	protected static final String POS_VARIANCE_PARAM_LABEL 	= "PosVariance";
	protected static final String ORDER_PARAM_LABEL 		= "Order";
	protected static final String CONSTANT_PARAM_LABEL		= "Constant";
	
	protected static final String[] LABELS = {
		CONSTANT_PARAM_LABEL, 
		WEIGHT_PARAM_LABEL,  
		POS_VARIANCE_PARAM_LABEL, 
		ORDER_PARAM_LABEL, 
	};
	
	public ATopicScore() { } 
	
	abstract public void loadModel() throws IOException;
	abstract public String getType();
	abstract public void addData(double[] data);
	abstract public int train()  throws ClassifierException;
	abstract public double score(double[] values);
}

// ---------------------------  EOF  -----------------------------------

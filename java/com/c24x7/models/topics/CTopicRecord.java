/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics;


import com.c24x7.models.ATaxonomyNode;
import com.c24x7.util.CEnv;




			/**
			 * <p>Class that defined a topics record associated with a root
			 * class and associated taxonomy objects, extracted from a document.</p>
			 * 
			 * @author Patrick Nicolas         24x7c 
			 * @date May 13, 2012 9:31:59 PM
			 */

public final class CTopicRecord  {
	private String 	_nodeLabel			= null;
	private double 	_nodeWeight 		= 0.0;
	private double 	_nodePosVariance 	= 0.0;

		/**
		 * <p>Create a Topics Model for a taxonomy root class and its
		 * associate taxonomy objects.
		 * @param weight weight of the taxonomy root class
		 * @param sentenceVariance variance of the sentence indices of the taxonomy root class.
		 */
	public CTopicRecord(final ATaxonomyNode node, double nodePosVariance) { 
		_nodeLabel = node.getLabel();
		_nodeWeight = node.getWeight();
		_nodePosVariance =  nodePosVariance;
	}
	
	public final String getNodeLabel() {
		return _nodeLabel;
	}
		

		/**
		 * <p>Create a record (textual representation) for this topics model.</p>
		 * @return a record of the weight and sentences indices variance of the root taxonomy class and its taxonomy objects.
		 */
	@Override 
	public String toString() {
		StringBuilder buf = new StringBuilder("\n; ");
		buf.append(_nodeLabel);
		buf.append("\n");
		buf.append(_nodeWeight);
		buf.append(CEnv.FIELD_DELIM);
		buf.append(_nodePosVariance);
		
		return buf.toString();
	}

}

//---------------------------------- EOF -------------------------------------------

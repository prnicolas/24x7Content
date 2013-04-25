// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;

import com.c24x7.util.CEnv;
import com.c24x7.util.collections.CGraph.INode;



		/**
		 * <p>Generic abstract class for all taxonomy nodes, classes or instances (taxonomyInstances).
		 * A taxonomy node contains a label, a weight and array of indexes to sentences or paragraphs.</p>
		 * @author Patrick Nicolas
		 * @date 05/21/2012
		 */
public abstract class ATaxonomyNode implements INode, Comparable<ATaxonomyNode> {
	protected String	_label 		= null;		
	protected short		_textIndex	= 0;
	protected float		_weight 	= CEnv.UNINITIALIZED_FLOAT;
	
	
	/**
	 * <p>Update the current weight of this taxonomy class.</p>
	 * @param weight new weight value to be added to the current weight.
	 * @return update weight for this taxonomy class.
	 */
	abstract public float applyKirchoff(float weight);
	
	public void setLevel(short level) { }
	
	public int getLevel() {
		return -1;
	}
	
	public void setTextIndex(short textIndex) {
		_textIndex = textIndex;
	}
	
	public short getTextIndex() {
		return _textIndex;
	}
	
	
	/**
	 * <p>Create taxonomy node instance for undefined label and weight
	 */
	public ATaxonomyNode() { }
	
		/**
		 * <p>Create taxonomy node instance for specific label and weight
		 * @param label label of the taxonomy node (name of class or instance)
		 * @param weight weight of the taxonomy node.
		 */
	public ATaxonomyNode(final String label, float weight) {
		_label = label;
		_weight = weight;
	}
	
	
	
	/**
	 * <p>Retrieve the label of this taxonomy node (class or instance).</p>
	 * @return label of the taxonomy node
	 */
	public final String getLabel() {
		return _label;
	}
	
	
	public void normalize(float maxWeight) {
		_weight /= maxWeight;
	}


	
	/**
	 * <p>Retrieve the weight of this taxonomy node (class or instance).</p>
	 * @return weight of the taxonomy node
	 */
	public float getWeight() {
		return _weight;
	}
	
		/**
		 * <p> Implements the compareTo method from the Comparable interface. Two
		 * Taxonomy nodes are identical when they have identical label or identifier.</p>
		 * @param node taxonomy node to compare to
		 * @return 0 if the two taxonomy nodes have the same label, not 0 otherwise or if the argument is null.
		 */
	public int compareTo(ATaxonomyNode node) {
		return (node != null) ? _label.compareTo(node.getLabel()) : -1;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(_label);
		buf.append("(");
		buf.append(_weight);
		buf.append(")");

		return buf.toString();
	}
}

// ------------------------  EOF -------------------------------------------------
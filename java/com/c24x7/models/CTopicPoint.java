/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models;

import com.c24x7.math.utils.CIntArray;





public final class CTopicPoint {
	public static final int WEIGHT_SCALE = 20;
	private String 	_label 			= null;
	private int		_weight 		= -1;
	private int[]	_sentencesIndexes = null;
	private int		_topicIndex		= -1;
		
	public CTopicPoint(ATaxonomyNode node) {
		if(node == null ) {
			throw new IllegalArgumentException("Cannot create a topic point from an undefined taxonomy node");
		}
		_label = node.getLabel();
		_weight = (int)Math.floor(WEIGHT_SCALE*node.getWeight());
	}
		
	public final String getLabel() {
		return _label;
	}
		
	public int getWeight() {
		return _weight;
	}
		
	public void setSentencesIndexes(int[] sentencesIndexes) {
		_sentencesIndexes = sentencesIndexes;
	}
		
		
	public int[] getSentencesIndexes() {
		return _sentencesIndexes;
	}
		
	public void setTopicIndex(int topicIndex) {
		_topicIndex = topicIndex;
	}
		
	public int getTopicIndex() {
		return _topicIndex;
	}
	
	public void getDataPoint(CIntArray intArray) {
		for(int sentenceIndex : _sentencesIndexes ) {
			intArray.add(sentenceIndex);
			intArray.add(_topicIndex);
			intArray.add(_weight);
		}
	}
	
		/**
		 * <p>Print the content of this Topics point with the 
		 * label (top semantic class name), relative weight and 
		 * indices of associated sentences.</p>
		 * @return textual description of this topic point
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_label);
		buf.append(" ");
		buf.append(_weight);
		buf.append(" ");

		if( _sentencesIndexes != null) {
			buf.append("(");
			for( int k = 0; k < _sentencesIndexes.length-1; k++) {
				buf.append(_sentencesIndexes[k]);
				buf.append(",");
			}
			buf.append(_sentencesIndexes[_sentencesIndexes.length-1]);
			buf.append(") ");
		}
		buf.append(_topicIndex);
		
		return buf.toString();
	}
}


// ------------------------  EOF -------------------------------------------------
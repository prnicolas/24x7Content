// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;



		/**
		 * <p>Class that defines a taxonomy instance. A taxonomy is associated with either
		 * a taxonomyInstance noun or a category keyword.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/11/2012
		 */
public final class CTaxonomyClass extends ATaxonomyNode  {
	private short _level = -1;
	
	public CTaxonomyClass(String label) {
		super(label, 0.0F);
	}
	
	

	/**
	 * <p>Update the current weight of this taxonomy class.</p>
	 * @param weight new weight value to be added to the current weight.
	 * @return update weight for this taxonomy class.
	 */
	@Override
	public float applyKirchoff(float weight) {
		_weight += weight;
		return _weight;
	}
	
	
	
	@Override
	public void setLevel(short level) {
		_level = level;
	}
	
	@Override
	public int getLevel() {
		return _level;
	}

	

	
	public static String printTaxonomy(ATaxonomyNode[] classes) {
		StringBuilder buf = new StringBuilder();
		int lastClassIndex = classes.length-1;
		for(int k = 0; k < lastClassIndex; k++) {
			buf.append(classes[k].getLabel());
			buf.append("/");
		}
		buf.append(classes[lastClassIndex].getLabel());
		
		return buf.toString();
	}
	
}


// -------------------------------  EOF ------------------------------------

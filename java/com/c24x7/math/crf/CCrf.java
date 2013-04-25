/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.math.crf;

import java.io.IOException;

import com.c24x7.util.logs.CLogger;

import iitb.CRF.CRF;
import iitb.CRF.FeatureGenerator;
import iitb.Model.FeatureGenImpl;



public class CCrf {
	private FeatureGenerator _featuresGenerator = null;
	private CRF				 _model = null;
	
	
	public CCrf() {
		initialize();
	}
	
	public void train() {
		String modelSpec = null;
		try {
			_featuresGenerator = new FeatureGenImpl(modelSpec, 4, false);
		}
		catch( IOException e) {
			CLogger.info(e.toString());
		}
		catch( Exception e) {
			CLogger.info(e.toString());
		}
	}
	
	
	private void initialize() {
		
	}
}

// ----------------------------  EOF -----------------------------------

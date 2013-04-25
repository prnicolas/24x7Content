/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.models.topics.scoring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.ClassifierException;
import com.c24x7.math.lsregression.CLSRegression;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;



		/**
		 * <p>Class that implements the scoring to taxonomy class as 
		 * candidate to topics.</p>
		 * @author Patrick Nicolas         24x7c 
		 * @date June 22, 2012 2:40:20 PM
		 */
public class CTopicScoreMRegression extends ATopicScore {
	private static final String TOPICS_MODEL_FILE 	= CEnv.modelsDir + "semantic/topic_model_regression";
	private static final String LABEL 				= "Multiple LS Regression";
	
	private CLSRegression 	_regression = new CLSRegression();
	private double[] 		_parameters = null;
	
	
	public CTopicScoreMRegression() {
		super();
	}
	
	
	@Override
	public void loadModel() throws IOException {
		Map<String, String> parametersMap = new HashMap<String, String>();
		CFileUtil.readKeysValues(TOPICS_MODEL_FILE, parametersMap);
		
			/*
			 * If the model file was correctly formatted, 
			 * retrieve the parameters of the topic model.
			 */		
		if( parametersMap.containsKey(WEIGHT_PARAM_LABEL) &&
			parametersMap.containsKey(POS_VARIANCE_PARAM_LABEL) &&
			parametersMap.containsKey(ORDER_PARAM_LABEL) ) {
			
			_parameters = new double[5];
			_parameters[0] = Float.parseFloat(parametersMap.get(CONSTANT_PARAM_LABEL));
			_parameters[1] = Float.parseFloat(parametersMap.get(WEIGHT_PARAM_LABEL));
			_parameters[3] = Float.parseFloat(parametersMap.get(ORDER_PARAM_LABEL));
			_parameters[2] = Float.parseFloat(parametersMap.get(POS_VARIANCE_PARAM_LABEL));
		}
		else {
			throw new IOException("Cannot load LS Regression Topic Score Model");
		}
	}
		
	public String getType() {
		return LABEL;
	}
	
	public void addData(double[] data) {
		_regression.addData(data);
	}
	
	
	@Override
	public int train() throws ClassifierException  {
		int numTrainingSamples = _regression.compute();
		try {
			writeModel(_regression.getRSquareStats());
		}
		catch( IOException e) {
			throw new ClassifierException("Cannot train Topic classifier. " + e.toString());
		}
		
		return numTrainingSamples;
	}
	
	public final double[] getParameters() {
		return _regression.getParameters();
	}
	
	
	public double score(double[] values) {
		double sum = 0.0;
		for(int k = 0; k < values.length; k++) {
			sum += _parameters[k]*values[k];
		}
		
		return sum + _parameters[5];
	}
	
	

	private void writeModel(double rStats) throws IOException {
		StringBuilder buf = new StringBuilder("; -----");
		buf.append(LABEL);
		buf.append( " ----------------------\nR stats");
		buf.append(rStats);
		buf.append("\n;\n");
		for(int k = 0; k < LABELS.length; k++) {
			buf.append("\n");
			buf.append(LABELS[k]);
			buf.append(CEnv.KEY_VALUE_DELIM);
			buf.append(_parameters[k]);
		}
		
		CFileUtil.write(TOPICS_MODEL_FILE, buf.toString());
	}
}


// --------------------------------------  EOF -----------------------------
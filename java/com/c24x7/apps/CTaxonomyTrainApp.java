// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;


import com.c24x7.exception.InitException;
import com.c24x7.models.taxonomy.CTaxonomyClassifier;
import com.c24x7.models.taxonomy.CTaxonomyModel;
import com.c24x7.models.taxonomy.CTaxonomyModel.NModelParams;
import com.c24x7.models.metrics.CLineagePathDistance;
import com.c24x7.models.metrics.CLineageSiblingParentDistance;
import com.c24x7.models.metrics.CLineageSimpleDistance;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Command line application to train the taxonomy model.</p>
		 * @see com.c24x7.models.learners.taxonomy.CTaxonomyClass
		 * @author Patrick Nicolas
		 * @date 02/02/2012
		 */
public final class CTaxonomyTrainApp {
	
	
	private final static int[] TRAINING_RANGE = {
		32000, 33000
	};
	private final static int[] VALIDATION_RANGE =  {
		610000, 620000
	};
	
	private final static int[] TEST_RANGE =  {
		925000, 928000
	};


			/**
			 * <p>Main method for the command line application to
			 * compute the taxonomy model features using a Bayesian 
			 * approach.</p>
			 * @param args
			 */
	public static void main(String[] args) {
		try {
			CEnv.init();
			if( args != null && args.length > 0) {
			
					/*
					 * Launch the training phase.
					 */
				CTaxonomyClassifier classifier = null;
				if(args[0].compareTo("-train")==0) {
					NModelParams modelParams = new NModelParams();
					modelParams.addCategories();

					CTaxonomyModel.getInstance(modelParams);
					
					classifier = new CTaxonomyClassifier(1);
					classifier.train();
				}
				
				/*
				 * Launch the validation phase.
				 */
				else if(args[0].compareTo("-validate")==0) {
					NModelParams modelParams = new NModelParams();
					
					try {
						CTaxonomyModel.init(modelParams);
						classifier = new CTaxonomyClassifier(1);
						classifier.validate(VALIDATION_RANGE);
						int numSamples = classifier.getNumSamples();
						CLogger.info("Taxonomy validation done with " + numSamples + " samples", CLogger.TAXONOMY_TRAIN_TRACE);
					}
					catch( InitException e) {
						CLogger.error(e.toString());
					}
					
				}
				
				/*
				 * Launch the test phase.
				 */
				else if(args[0].compareTo("-test")==0) {
					NModelParams modelParams = initModelParams(args[1], args[2]);
					
					try {
						CTaxonomyModel.init(modelParams);
						classifier = new CTaxonomyClassifier(1);
						classifier.test(TEST_RANGE);
						int numSamples = classifier.getNumSamples();
						CLogger.info("Taxonomy validation done with " + numSamples + " samples", CLogger.TAXONOMY_TRAIN_TRACE);
					}
					catch( InitException e) {
						CLogger.error(e.toString());
					}

				}
			}
			else {
				CLogger.info("Command line: CTaxonomyTrainApp {-train,-validate,-all}");
			}
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
	}
	
	public static NModelParams initModelParams(String arg, String arg2) {
		NModelParams modelParams = new NModelParams();
		if(arg.compareTo("S0") == 0) {
			modelParams.setLineageDistance(new CLineageSimpleDistance());
		}
		else if(arg.compareTo("SC") == 0) {
			modelParams.setLineageDistance(new CLineageSimpleDistance());
			modelParams.addCategories();
		}
		
		else if(arg.compareTo("P0") == 0) {
			modelParams.setLineageDistance(new CLineagePathDistance());
		}
		else if(arg.compareTo("PC") == 0) {
			modelParams.setLineageDistance(new CLineagePathDistance());
			modelParams.addCategories();
		}
		
		else if(arg.compareTo("SP") == 0) {
			modelParams.setLineageDistance(new CLineageSiblingParentDistance());
		}
		
		if( arg2 != null) {
			CTaxonomyModel.numClasses = Integer.valueOf(arg2);
		}
		
		return modelParams;
	}
}

// ---------------------------  EOF -------------------------------------------
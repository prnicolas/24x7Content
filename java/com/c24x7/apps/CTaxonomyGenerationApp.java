// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;

import com.c24x7.exception.InitException;
import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.taxonomy.CTaxonomyModel;
import com.c24x7.models.taxonomy.CTaxonomyModel.NModelParams;
import com.c24x7.nlservices.CDbpediaSemanticService;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;



public final class CTaxonomyGenerationApp {
	
	public static void main(String[] args) {
		if( args != null && args.length > 1) {
			
			int startIndex = Integer.parseInt(args[0]);
			final int length = Integer.parseInt(args[1]);

			System.out.println("Launch CTaxonomyGenerationApp " + String.valueOf(startIndex) + " " + String.valueOf(length));
			
			try {
				CEnv.init();
				/*
				 * Create taxonomy lineage for Wikipedia entries without categories
				 * and using Lineage Path Distance algorithm.
				 */
				
				NModelParams modelParams = new NModelParams();
				CTaxonomyModel.init(modelParams);
			
					/*
					 * First Thread to populate the taxonomy fields in dbpedia database
					 */
				CDbpediaSemanticService taxonomyGenerator1 = new CDbpediaSemanticService();
				taxonomyGenerator1.generate(startIndex, startIndex+length-1);
					
					/*
					 * First Thread to populate the taxonomy fields in dbpedia database
					 */
				startIndex += length;
				CDbpediaSemanticService taxonomyGenerator2 = new CDbpediaSemanticService();
				taxonomyGenerator2.generate(startIndex, startIndex+length-1);
			}
			catch( InitException e) {
				CLogger.error(e.toString());
			}
			catch( SemanticAnalysisException e) {
				CLogger.error(e.toString());
			}

		}
		else {
			System.out.println("CTaxonomyGenerationApp incorrect arguments");
		}
	}
}

// --------------------------- EOF ---------------------------------------------------------

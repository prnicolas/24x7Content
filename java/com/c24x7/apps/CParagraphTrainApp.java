// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.apps;

import java.io.IOException;


import com.c24x7.models.web.CWebContentClassifier;
import com.c24x7.util.logs.CLogger;

			/**
			 * <p>Command line application to train the different models 
			 * for detecting specialized paragraphs or section in a document such as 
			 * copyright, references or non-related content.</p>
 			 * @author Patrick Nicolas
			 * @date 11/16.2011
			 */

public final class CParagraphTrainApp {

	public static void main(String[] args) {
		CWebContentClassifier webClassifier = new CWebContentClassifier();
		
		try {
			webClassifier.train();
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
	}
}

// ----------------------------------  EOF ------------------------------

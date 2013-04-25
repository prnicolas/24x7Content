/*
 *  Copyright (C) 2010-2012  Patrick Nicolas
 */
package com.c24x7.semantics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;



			/**
			 * <p>Class to encapsulate the cutoff classes for Word Net</p>
			 * @author Patrick Nicolas
			 * @date 05/29/2012
			 */
public final class CTaxonomyFilter {
	private static final String TAXONOMY_CUTOFF_CLASSES = CEnv.configDir + "taxonomycutoff";
	
	private static CTaxonomyFilter instance = null;
	
	public static void init() throws InitException {
		instance = new CTaxonomyFilter();
		try {
			instance.load();
		}
		catch( IOException e) {
			throw new InitException("Cannot load taxonomy cutoff classes " + e.toString());
		}	
	}
	
		/**
		 * <p>Retrieve the singleton that extracts the taxonomy classes
		 * (hypernyms) of lower order.
		 * @return instance of the taxonomy cutoff class.
		 */
	public static CTaxonomyFilter getInstance() {
		return instance;
	}
	
	
	private Map<String, String> _taxonomyCutoffMap = new HashMap<String, String>();
	
	
		/**
		 * <p>Test if the class which a specified name is valid and has a low order.</p>
		 * @param className name of the class
		 * @throws IllegalArgumentException if the argument is undefined or null
		 * @return true if the taxonomy class is valid
		 */
	public boolean contains(final String className) {
		if( className == null ) {
			throw new IllegalArgumentException("Cannot filter out undefined taxonomy class " + className);
		}
		
		return _taxonomyCutoffMap.containsKey(className);
	}
	
	
	
							//	----------------
							//  Private Methods
							// ------------------
	
	private CTaxonomyFilter() {	}
	
	private void load() throws IOException {
		CFileUtil.readKeysValues(TAXONOMY_CUTOFF_CLASSES, _taxonomyCutoffMap);
	}
	
}

// --------------------------  EOF -------------------------------------
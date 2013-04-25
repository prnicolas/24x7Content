// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.content;


import com.c24x7.CWorkflow;


		/**
		 * <p>Base class for content manipulated by multiple Natural Language
		 * Processing services</p>
		 * @author Patrick Nicolas
		 * @date 11/13/2010
		 */
public class AContent {
	private static int numSeeds = 0;
	
	/**
	 * Minimum number of characters in the seed 
	 */
	public final static int MIN_NUM_CHARS_SEED = 32;

	private String	_seedStr = null;
	
	public AContent() {
		
	}
	
			/**
			 * <p>Create a Content object with a seed.</p>
			 * @param seedStr seed string
			 */
	public AContent(final String seedStr) {
		if( seedStr == null || seedStr.length() < MIN_NUM_CHARS_SEED) {
			throw new IllegalArgumentException("Seed string is missing");
		}
		_seedStr = seedStr;
		numSeeds++;
	}
	
	/**
	 * <p>Retrieve the statistics for this output object, if the statistic collector
	 * object has been created in the workflow. The statistics are computed and dumped 
	 * into a CLogger object (log file).</p>
	 * @param stats statistics object
	 */
	public void getStats(CWorkflow.NStats stats) {
		if(stats != null) {
			stats.setNumSeeds(numSeeds);
		}
	}
	
	public final String getSeed() {
		return _seedStr;
	}
	
	@Override
	public String toString() {
		return _seedStr;
	}
}

// ------------------  EOF ----------------------------------------
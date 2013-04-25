// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.content;

import java.util.ArrayList;
import java.util.List;

import com.c24x7.CWorkflow;



		/**
		 * <p>Define the content generated by the NLG engine as a specialized Content object containing
		 * the original seed and the generated sentences</p>
		 * @see com.c24x7.nlservices.content.AContent
		 * @author Patrick Nicolas
		 * @date 11/21/2010
		 */
public final class CRawOutput extends AContent {
	
	private List<String> _elements = new ArrayList<String>();
	
			/**
			 * <p>
			 * @param seedStr
			 */
	public CRawOutput(final String seedStr) {
		super(seedStr);
	}
 
	/**
	 * <p>Generic method to retrieve content</p>
	 * @return list of content
	 */
	public final List<String> getContent() {
		return _elements;
	}

	public void add(final String sentence) {
		_elements.add(sentence);
	}


		/**
		 * <p>Retrieve the statistics for this output object, if the statistic collector
		 * object has been created in the workflow. The statistics are computed and dumped 
		 * into a CLogger object (log file).</p>
		 * @param stats statistics object
		 */
	@Override
	public void getStats(CWorkflow.NStats stats) {
		if(stats != null) {
			stats.setNumChars(getNumChars());
			stats.setNumSentences(getNumSentences());
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(String content : _elements) {
			buf.append(content);
		}
		
		return buf.toString();
	}
	
	
	
					// ---------------------------
					//  Private Methods
					// -------------------------------
	
	/**
	 * <p>Retrieve the number of sentence for this content</p>
	 * @return number of sentences
	 */
	private final int getNumSentences() {
		return _elements.size();
	}


	/**
	 * <p>return the number of characters of content generated</p>
	 * @return number of characters for all the generated content
	 */
	private final int getNumChars() {
		int numChars = 0;
		for(String el : _elements) {
			numChars += el.length();
		}

		return numChars;
	}
}

// -------------------  EOF ------------------------------------
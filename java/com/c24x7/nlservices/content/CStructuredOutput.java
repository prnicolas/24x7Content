// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.content;

import com.c24x7.CWorkflow;
import com.c24x7.util.CXMLConverter;



		/**
		 * <p>Class to break down generated raw content in title, messages, tags,
		 * body/description. The generated structure is used to create social content</p>
		 * @author Patrick Nicolas
		 * @date 11/17/2010
		 */
public final class CStructuredOutput extends AContent {
	private final static String DELIM = "[^&]";
	private String _title 		= null;
	private String _description = null;
	private String[] _selection	= null;
	private float  _similarity	= 0.0F;
	
	
	
			/**
			 * <p>Create a structured output object with a raw string extracted from 
			 * the cache.</p>
			 * @param rawStr raw string
			 */
	public CStructuredOutput(final String rawStr) {
		int indexSeparator = rawStr.indexOf(DELIM);
		_title = rawStr.substring(0, indexSeparator);
		_description = rawStr.substring(indexSeparator + DELIM.length());
	}
	
	/**
	 * <p>Create a Structured output with a title, description</p>
	 * @param elements array that contains the title and description extracted from the generated output
	 */
	public CStructuredOutput(final String[] elements) { 
		super();
		_title = elements[0];
		_description = elements[1];		
		_selection = elements[2].split(":");
	}
	
			/**
			 * <p>Create a Structured output with a title, message and description</p>
			 * @param seed original seed content
			 * @param title title extracted from the generated output
			 * @param description original description
			 */
	public CStructuredOutput(final String seed, 
							 final String title, 
							 final String description,
							 float similarity) { 
		super(seed);
		
		if( title == null || description == null) {
			throw new IllegalArgumentException("\ntitle or description of the generated content is undefined");
		}
		_title = title;
		_description = description;
		_similarity = similarity;
	}
	
	
	public final String[] getSelection() {
		return _selection;
	}
	
	public String setSelection() {
		String selectionStr = "";
		
		if(_selection.length > 0) {
			StringBuilder buf = new StringBuilder(_selection[0]);
			for(int j = 1; j < _selection.length; j++) {
				buf.append("$$");
				buf.append(_selection[j]);
			}
			selectionStr = buf.toString();
		}
		
		return selectionStr;
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
			stats.setTitle(_title);
			stats.setNumCharsTitle(_title.length());
			stats.setSimilarity(_similarity);
		}
	}
	
			/**
			 * <P>Retrieve the social message component of a structured content generated by the NLG engine</p>
			 * @return message of the content
			 */
	public final String getTitle() {
		return _title;
	}
	
			/**
			 * <P>Retrieve the description of a structured content generated by the NLG engine</p>
			 * @return body or description of the generated content
			 */
	public String getDescription() {
		return _description;
	}

	public final String intern() {
		StringBuilder buf = new StringBuilder(_title);
		buf.append(DELIM);
		buf.append(_description);
		return buf.toString();
	}
		
	/**
	 * <p>Generated a textual representation of the Statistical object.</p>
	 * return XML representation of this object
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(CXMLConverter.put(CXMLConverter.TITLE_TAG, _title));
		buf.append(CXMLConverter.put(CXMLConverter.DESC_TAG, _description));
			
		return CXMLConverter.put(CXMLConverter.CONTENT_TAG, buf.toString());
	}
}
// ---------------------------  EOF ----------------------------
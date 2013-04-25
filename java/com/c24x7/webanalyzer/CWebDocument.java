package com.c24x7.webanalyzer;

import java.util.LinkedList;
import java.util.List;

public final class CWebDocument {
	private static final int MIN_NUM_CHARACTERS = 8;
	private String 		 _copyright = null;
	private List<String> _referencesList = null;
	private List<String> _paragraphsList = null;
	
	public CWebDocument() {}
	
	public CWebDocument(final String content) {
		List<String> originalContent = new LinkedList<String>();
		originalContent.add(content);
	}
	
	public void setCopyright(String copyright) {
		_copyright = copyright;
	}
	
	public void addReferences(String reference) {
		if( reference != null && reference.length() > MIN_NUM_CHARACTERS ) {
			if( _referencesList == null) {
				_referencesList = new LinkedList<String>();
			}
			_referencesList.add(reference);
		}
	}
	
	public void addParagraph(String paragraph) {
		if( paragraph != null && paragraph.length() > MIN_NUM_CHARACTERS ) {
			if(_paragraphsList == null) {
				_paragraphsList = new LinkedList<String>();
			}
			_paragraphsList.add(paragraph);
		}
	}
	
	public final String getCopyright() {
		return _copyright;
	}
	
	public final List<String> getReferencesList() {
		return _referencesList;
	}
	public final List<String> getParagraphsList() {
		return _paragraphsList;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if( _copyright != null) {
			buf.append(_copyright);
			buf.append("\n");
		}
		if( _paragraphsList != null) {
			for( String paragraph : _paragraphsList) {
				buf.append("\n\n");
				buf.append(paragraph);
			}
		}
		return buf.toString();
	}
}


// ------------------------  EOF -----------------------------------

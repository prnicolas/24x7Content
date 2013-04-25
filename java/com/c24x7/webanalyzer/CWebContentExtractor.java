// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.webanalyzer;



import com.c24x7.models.web.CWebContentClassifier;



			/**
			 * <p>Generic class to analyze and classifier the main section
			 * of a web page or a web site.</p>
			 * @author Patrick Nicolas
			 * @date 11/14/2011
			 */

public final class CWebContentExtractor {
	protected static final int MIN_NUM_CHARS = 512;
		
			
	public static class NARule {
		public CWebDocument extract(final String content) { 
			return new CWebDocument(content);
		}
	}

				/**
				 * <p>Extractor using syntactic rule.</p>
				 * @author Patrick Nicolas
				 * @date 11/12/2011
				 */
	public static class NHtmlSyntaxRule extends NARule {
		protected static final int DEFAULT_MIN_PARAGRAPH_SIZE = 196;
		protected static final int DEFAULT_MIN_LINE_LENGTH 	= 32;
		
		protected int _minParagraphSize = DEFAULT_MIN_PARAGRAPH_SIZE;
		protected int _minLineLength 	  = DEFAULT_MIN_LINE_LENGTH;
			
		public NHtmlSyntaxRule() {
			this(DEFAULT_MIN_PARAGRAPH_SIZE, DEFAULT_MIN_LINE_LENGTH);
		}
		
		public NHtmlSyntaxRule(int minParagraphSize, int minLineLength) {
			_minLineLength = minLineLength;
			_minParagraphSize = minParagraphSize;
		}
		
					/**
					 * <p>Extract the paragraph from a document using
					 * some predefined and heuristic syntactical rules.</p>
					 * @param content of the document to analyze
					 * @return document 
					 */
		public CWebDocument extract(final String content) {
			CWebDocument doc = null;
			
			if( content != null && content.length() > MIN_NUM_CHARS) {
				doc = new CWebDocument();
				String cursor = content;
				String line = null;
				int indexNL = -1;
				boolean beginParagraph = false;
				int numEmptyLines = 0;
				StringBuilder buf = null;
					
						/*
						 * Extract the different paragraph from the content page.
						 */
				while(true ) {
					indexNL = cursor.indexOf("\n");
					if( indexNL == -1) {
						break;
					}
					line = cursor.substring(0, indexNL);
					line = line.trim();
					if( line.length() < 4) {
						numEmptyLines++;
						if( numEmptyLines > 1) {
							beginParagraph = false;
						}
					}
						/*
						 * Maybe the beginning of a paragraph..
						 */
					else if( (numEmptyLines > 2) && (line.length() > _minLineLength) ) {
						if( buf != null ) {
							classify(buf.toString().trim(), doc);
						}
						numEmptyLines = 0;
						buf = new StringBuilder(line);
						buf.append(" ");
						beginParagraph = true;
					}
					else if(beginParagraph ) {
						buf.append(line);
						buf.append(" ");
					}
		
					cursor = cursor.substring(indexNL+1);
				}
					/*
					 * If this is not a HTML display..
					 */
				if( buf != null) {
					classify(buf.toString().trim(), doc);
				}
				else {
					doc.addParagraph(content);
				}
			}
			return doc;
		}
		
		
		
		protected void classify(final String content, CWebDocument doc) {
			boolean isSpecialParagraph = true;			
						/*
						 * Attempt to classify this section of content with
						 * an existing class of paragraph
						 */
			CWebContentClassifier webContentClassifier = new CWebContentClassifier();
			webContentClassifier.classify(content);
			
						/*
						 * If this paragraph or document section is not 
						 * specialized or classified, then this is a genuine
						 * content section of the document.
						 */
			if( !isSpecialParagraph && content.length() > _minParagraphSize) {
				doc.addParagraph(content);
			}
		}
	}
}

// ---------------------- EOF --------------------------------------------
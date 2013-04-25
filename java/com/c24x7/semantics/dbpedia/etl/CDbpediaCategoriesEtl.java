// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;


import com.c24x7.semantics.dbpedia.etl.CDatasetExtractor;
import com.c24x7.semantics.dbpedia.etl.CDatasetExtractor.NHyphenResConverter;
import com.c24x7.util.CEnv;
import com.c24x7.util.string.CStringUtil;




/**
 * <p>Extraction of categories information from dbpedia data sets.</p>
 * @author Patrick Nicolas
 * @date 07/22/2011
 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
 */
public final class CDbpediaCategoriesEtl extends ADbpediaEtl {
	public static final String FIELD_DELIM = "#";
	public static final String DBPEDIA_FILE = CEnv.datasetsDir + "dbpedia/article_categories_en.nt";
	public static final int FIELD_LIMIT = 1023;
	
	protected String 		_prevLabel = " ";
	protected StringBuilder _catBuffer = null;
	
	
	public CDbpediaCategoriesEtl() {
		_extractor = new CDatasetExtractor(new NHyphenResConverter(), new NHyphenResConverter());
	}
	
		/**
		 * <p>Retrieve the name of the file that contains the dump of dbpedia artifact.</p>
		 * @return name of the file containing this specific dbpedia artifact.
		 */
	public String getDbpediaFile() {
		return DBPEDIA_FILE;
	}
			
			/**
			 * <p>Execute the SQL statement associated with this field extracted from the dbpedia data sets.<br>
			 * First field to be extracted is the long abstract field.</p>
			 * @see com.c24x7.nlservices.finders.etl.CTaxonomyExtractorEtl.NAEtl#executeEtl(java.lang.String, java.sql.Statement)
			 * @param newLine line extracted from the dbpedia data set
			 * @param stmt SQL statement used to update the table
			 */
	public boolean map(String newLine) {
		boolean success = false;
		
				/*
				 * If the label was successfully extracted, then
				 */
		String label = _extractor.extractLabel(newLine);
			
		if( label != null ) {
			label = CStringUtil.decodeLatin1(label);
			
				/*
				 * Extract the category information from the line in the
				 * data set then remove the label 'Category:'
				 */
			String categoryStr = _extractor.extractResource(newLine, true);
			int indexCategoryLabel = categoryStr.indexOf(CEnv.KEY_VALUE_DELIM);
			if( indexCategoryLabel != -1) {
				String category = categoryStr.substring(indexCategoryLabel+1).trim();
				category = CStringUtil.decodeLatin1(category);
				
				if( _prevLabel.compareTo(label) == 0) {
					_catBuffer.append(FIELD_DELIM);
					_catBuffer.append(category);
				}
				else {
					if( _catBuffer != null) {
						
						String encodedCategories = CStringUtil.encodeLatin1(_catBuffer.toString());
						if( encodedCategories.length() >= FIELD_LIMIT) {
							encodedCategories = encodedCategories.substring(0, FIELD_LIMIT);
						}
						/*
						StringBuilder sqlUpdate = new StringBuilder("UPDATE 24x7c.dbpedia SET categories=\'");
						sqlUpdate.append(encodedCategories);
						sqlUpdate.append("\' WHERE label=\'");
						sqlUpdate.append(CStringUtil.encodeLatin1(_prevLabel));
						sqlUpdate.append("\';");
						try {
							success = stmt.execute(sqlUpdate.toString());
						}
						catch( SQLException e) { 
							CLogger.error(e.toString());
						}
						*/
					}
					_prevLabel = label;
					_catBuffer = new StringBuilder(category);
				}
			}

		}
		return success;
	}
}

// -------------------------------- EOF --------------------------------------------------

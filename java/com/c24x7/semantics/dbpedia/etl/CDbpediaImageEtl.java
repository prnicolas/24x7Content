// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;


import com.c24x7.util.CEnv;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>class that extract thumbnail image url from the relevant dbpedia dataset.</p>
		 * @author Patrick Nicolas
		 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
		 * @date 10/19/2011
		 */
public final class CDbpediaImageEtl extends ADbpediaEtl {
	protected static String DBPEDIA_FILE 		 = CEnv.datasetsDir + "/bpedia/images_en.nt";
	protected static String DBPEDIA_IMG_MARKER = "<http://xmlns.com/foaf/0.1/depiction>";
	protected static String DBPEDIA_IMG_PREFIX = "http://upload.wikimedia.org/wikipedia/commons/";
	
	protected String _imageUrl = null;
		
	public CDbpediaImageEtl() {
		super();
	}
		
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
		int indexImageUrlIndex = newLine.indexOf(DBPEDIA_IMG_MARKER);
					/*
					 * Make sure the line is related to the thumb nail image.
					 */
		if( indexImageUrlIndex != -1) {
			_imageUrl = _extractor.extractImage(newLine.substring(indexImageUrlIndex + DBPEDIA_IMG_MARKER.length()+2));				
			int indexResource = newLine.indexOf(DBPEDIA_RES_MARKER);
			if( indexResource != -1) {
					
				String label = _extractor.extractLabel(newLine);
				if( label != null && _imageUrl != null) {
					StringBuilder sqlUpdate = new StringBuilder("UPDATE 24x7c.dbpedia SET thumbnail=\'");
					sqlUpdate.append(_imageUrl);
					sqlUpdate.append("\' WHERE label=\'");
					sqlUpdate.append(CStringUtil.encodeLatin1(label));
					sqlUpdate.append("\';");
					/*
					try {
						success = stmt.execute(sqlUpdate.toString());
					}
					catch( SQLException e) { }
					*/
					_imageUrl = null;
				}
			}
		}
		return success;
	}
}

// ------------------------------- EOF ---------------------------------

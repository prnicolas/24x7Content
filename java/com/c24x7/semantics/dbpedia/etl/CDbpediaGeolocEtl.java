// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;


import com.c24x7.util.CEnv;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>
		 * Class that extract geographic (geo location) information related to a label or part of speech which is a DBpedia entry.</p>
		 * @author Patrick Nicolas
		 * @see com.c24x7.semantics.dbpedia.etl.ADbpediaEtl
		 * @date 10/19/2011
		 */
public final class CDbpediaGeolocEtl extends ADbpediaEtl {
	protected final String DBPEDIA_GEOLOC_MARKER = "www.georss.org/georss/point>";
	public static String DBPEDIA_FILE = CEnv.datasetsDir + "dbpedia/geo_coordinates_en.nt";
	protected final String GEOLOC_DELIM = "\"";

	public CDbpediaGeolocEtl() {
		super();
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
		int indexGeoLocMarker = newLine.indexOf(DBPEDIA_GEOLOC_MARKER);
		
			/*
			 * Make sure the line contains the actual coordinates
			 */
		if( indexGeoLocMarker != -1) {
			String label = _extractor.extractLabel(newLine);
				/*
				 * if label has been clearly identify, then 
				 * extract the coordinates...
				 */
			if( label != null) {
				int indexCoordStart = newLine.indexOf(GEOLOC_DELIM);
				int indexCoordEnd = newLine.lastIndexOf(GEOLOC_DELIM);
					
				if( indexCoordStart != -1 && indexCoordEnd != -1 && indexCoordStart < indexCoordEnd) {
					String geoLocStr = newLine.substring(indexCoordStart+1, indexCoordEnd);
					geoLocStr = geoLocStr.replace(" ", CEnv.FIELD_DELIM);
					StringBuilder buf = new StringBuilder("SELECT id,type from 24x7c.dbpedia WHERE label=\'");
					buf.append(CStringUtil.encodeLatin1(label));
					buf.append("\';");
					
					/*
					try {
						
						ResultSet rs = stmt.executeQuery(buf.toString());
						String type = null;
						long id = -1L;
						
						while( rs.next() ) {
							id = rs.getInt("id");
							type = rs.getString("type");
						}
				
						if( type != null && type.compareTo("Place") == 0) {
							StringBuilder sqlUpdate = new StringBuilder("UPDATE 24x7c.dbpedia SET geoloc=\'");
							sqlUpdate.append(geoLocStr);
							sqlUpdate.append("\' WHERE id=");
							sqlUpdate.append(id);
							sqlUpdate.append(";");
							
							success = stmt.execute(sqlUpdate.toString());
						}
						
					}
					catch( SQLException e) { }
					*/
				}
			}
		}
	
		return success;
	}
}

// ------------------------------------- EOF --------------------------------

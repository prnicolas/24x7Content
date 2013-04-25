package com.c24x7.semantics.dbpedia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.util.CDbUtil;
import com.c24x7.util.string.CStringUtil;



public final class CDbpediaResources {
	
	public class NSummaryLabel  {
		protected static final String IMAGE_URL_ROOT = "http://upload.wikimedia.org/wikipedia/commons/";
		
		protected String _label      = null;
		protected String _shAbstract = null;
		protected String _lgAbstract = null;
		protected String _geoLoc	 = null;
		protected String _thumbnail	 = null;
		protected String _ontology   = null;

			/**
			 * <p>Create a DBpedia data information structure with a minimum non null label and
			 * short abstract. A Dbpedia record is defined by a label, short and long summary
			 * (or abstract), thumbnail image if available, geolocation coordinates for location
			 * information and ontology (taxonomy branch) </p>
			 * @param label label or keywords for the dbpedia/wikipedia entry
			 * @param lgAbstract long abstract content. 
			 * @param abstractIndex index of the last character of short abstract in the long abstract
			 */
		public NSummaryLabel(final String label, final String lgAbstract, int abstractIndex) {
			_label = label;
			_lgAbstract = lgAbstract;
			_shAbstract = lgAbstract.substring(0, abstractIndex);
		}
		

		
				/**
				 * <p>Retrieve the short abstract for this Dbpedia entry</p>
				 * @return short abstract for the DBpedia entry.
				 */
		public final String getShAbstract() {
			return _shAbstract;
		}
					
				/**
				 * <p>Retrieve the long abstract for this Dbpedia entry</p>
				 * @return long or extended abstract for the DBpedia entry.
				 */
		public final String getLgAbstract() {
			return _lgAbstract;
		}
		
				/**
				 * <p>Set up the ontology for this Dbpedia record. The ontology string
				 * is broken down into a hierarchy of type.</p>
				 * @param ontology ontology string as defined in Wikiipedia
				 */
		public void setOntology(final String ontology) {
			_ontology = ontology;
		}
				
		
		public final String getOntology() {
			return _ontology;
		}
		
		public void setThumbnail(final String imgUrl) {
			if( imgUrl != null ) {
				StringBuilder buf = new StringBuilder(IMAGE_URL_ROOT);
				buf.append("thumb/");
				buf.append(imgUrl);
				buf.append("/200px-");
				buf.append(imgUrl);
				
				_thumbnail = buf.toString();
			}
		}
		
		public final String getThumbnail() {
			return _thumbnail;
		}
		
		public void setGeoLoc(final String geoLoc) {
			_geoLoc = geoLoc;
		}
		
		public final String getGeoLoc() {
			return _geoLoc;
		}
		
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_label);
			buf.append(": ");
			buf.append(_ontology);
			buf.append("\n");
			buf.append(_thumbnail);
			buf.append("\n");
			buf.append(_geoLoc);
			buf.append("\n");
			buf.append(_shAbstract);
			buf.append("\n");
			buf.append(_lgAbstract);
	
			return buf.toString();
		}
	}
	
	
			/**
			 * <p>Extract the dbpedia entry from the database according to a predefined label.</p>
			 * @param stmt  query statement
			 * @param label label or part of the speech extracted from input text
			 * @param tableType  type of dbpedia table 
			 * @return dbpedia record
			 * @throws SQLException if the query fails...
			 */
	public NSummaryLabel getSummaryLabel(	Statement stmt, 
											final String label, 
											Character tableType) throws SQLException {
		
		String sqlStatement = null;
		
			/*
			 * If this is a redirects (or dbpedia entry alias) 
			 */
		if(tableType.charValue() == CLookup.DPBEDIA_ENTRY_ALIAS) {
				/*
				 * First try to find a match on the dbpedia aliases database...
				 */
			sqlStatement = CDbUtil.getSelectStmt("24x7c.dbpedia_aliases", "label", label, "resourceid");
			ResultSet rs = stmt.executeQuery(sqlStatement);
			
			long resourceId = -1L;
			while( rs.next() ) {
				resourceId = rs.getInt("resourceid");
			}
				/*
				 * If this keyword is an alias then get the retrieve the record.
				 */
			if( resourceId != -1L) {
				StringBuilder buf = new StringBuilder("SELECT abstractindx, lgabstract, thumbnail, geoloc, type, subtype FROM 24x7c.dbpedia WHERE id=");
				buf.append(resourceId);
				buf.append(";");
				sqlStatement = buf.toString();
			}
			
		}
			/*
			 * Otherwise, access the 
			 */
		else {
			sqlStatement = CDbUtil.getSelectStmt("24x7c.dbpedia", "label", label, "abstractindx, lgabstract, thumbnail, geoloc, type, subtype");
		}
				
		return getSummaryRecord(stmt, sqlStatement, label);
	}
		
		
			/**
			 * <p>Extract the dbpedia entry from the database according to a predefined label.</p>
			 * @param stmt  query statement
			 * @param label label or part of the speech extracted from input text
			 * @return dbpedia record
			 * @throws SQLException if the query fails...
			 */
	public NSummaryLabel getSummaryLabel(Statement stmt, final String label) throws SQLException {
		
			/*
			 * First try to find a match on the dbpedia aliases database...
			 */
		String sqlStatement = CDbUtil.getSelectStmt("24x7c.dbpedia", "label", label, "abstractindx, lgabstract, thumbnail, geoloc, type");
		return getSummaryRecord(stmt, sqlStatement, label);
	}

		protected NSummaryLabel getSummaryRecord(Statement stmt, 
					final String sqlStatement, 
					final String label) throws SQLException {
		NSummaryLabel summaryLabel = null;
		ResultSet rs = stmt.executeQuery(sqlStatement);
		
		String result = null;
		int index = -1;
		
		while (rs.next() ) {
			result = rs.getString("lgabstract");
			index = rs.getInt("abstractindx");
			if( result != null && index != -1) {
				summaryLabel = new NSummaryLabel(label, CStringUtil.decodeLatin1(result), index);
			}
		
			result = rs.getString("type");
			if( result != null) {
				summaryLabel.setOntology(CStringUtil.decodeLatin1(result));
			}
			result = rs.getString("geoloc");
			if( result != null) {
				summaryLabel.setGeoLoc(CStringUtil.decodeLatin1(result));
			}
			result = rs.getString("thumbnail");
			if( result != null ) {
				summaryLabel.setThumbnail(CStringUtil.decodeLatin1(result));
			}
		}
		
		return summaryLabel;
	}


}

// ---------------------------  EOF -------------------------------------
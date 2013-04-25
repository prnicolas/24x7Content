package com.c24x7.semantics.dbpedia.etl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.db.CSqlPreparedStmt;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;





public class CDbpediaLabelsEtl extends ADbpediaEtl {
	private static final String DBPEDIA_FILE = CEnv.datasetsDir + "dbpedia/long_abstracts_en.nt";
	private static final String SELECT_DBPEDIA_LABEL = "SELECT id FROM 24x7c.dbpedia WHERE label=?;";
	private static final String MISSING_LABELS_FILE = CEnv.outputDir + "debug/missing_labels";
	
	protected CSqlPreparedStmt _preparedStmtEntry = null;
	private List<String> _missingLabelsList = null;
	
	public CDbpediaLabelsEtl() {
		_extractor = new CDatasetExtractor();
		_preparedStmtEntry = new CSqlPreparedStmt(SELECT_DBPEDIA_LABEL);
		_missingLabelsList = new ArrayList<String>();
	}
		
		/**
		* <p>Retrieve the name of the file that contains dbpedia artifact to be updated</p>
		* @return name of the file containing this dump of long or extended abstract.
		*/
	public String getDbpediaFile() {
		return DBPEDIA_FILE;
	}

	public boolean map(String newLine) throws SQLException {
		
		boolean success = false;

		String label = _extractor.extractLabel(newLine);
		if( label != null && label.length() > 2) {
			
			try {
				String[] terms = label.split(" ");
				
				if( terms.length <= 4) {
					String encodedLabel = CStringUtil.encodeLatin1(label);
					_preparedStmtEntry.set(1, encodedLabel);
					ResultSet rs = _preparedStmtEntry.query();
					
					if(!rs.next()) {
						_missingLabelsList.add(label);
						CLogger.info(label + " is missing");
					}
				}
			}
			
			catch (SQLException e) {
				success = false;
				CLogger.error("Cannot add long abstract " + e.toString());
			}
		}
		return success;
	}

	
	public boolean reduce() {

		if( _missingLabelsList.size() > 0) {
			StringBuilder buf = new StringBuilder("");
			for( String key : _missingLabelsList) {
				buf.append(key);
				buf.append("\n");
			}
			try {
				CFileUtil.write(MISSING_LABELS_FILE, buf.toString());
			} 
			catch(IOException e) {
				CLogger.error(e.toString());
			}
		}
		_preparedStmtEntry.close();
		return true;
	}

}

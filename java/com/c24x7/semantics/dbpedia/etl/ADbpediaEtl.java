// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.sql.SQLException;

import com.c24x7.util.CEnv;


		/**
		 * <p>Main interface for SQL statement (insert, update or select).</p>
		 * @author Patrick Nicolas
		 * @date 07/22/2011
		 */
public abstract class ADbpediaEtl {
	public static final String 	DBPEDIA_RES_MARKER = "<http://dbpedia.org/resource/";
	protected static String 		DBPEDIA_FILE = CEnv.modelsDir + "/dbpedia/labels_en.nt";

	protected CDatasetExtractor _extractor = null;

	
	public ADbpediaEtl() { }
	
		/**
		 * <p>Retrieve the name of the file that contains the dump of dbpedia artifact.</p>
		 * @return name of the file containing this specific dbpedia artifact.
		 */
	public String getDbpediaFile() {
		return DBPEDIA_FILE;
	}
	

		/**
		 * <p>Inserts or update this dbpedia artifact into the database.</p>
		 * @param stmt JDBC statement 
		 * @param label label or keyword for this dbpedia entry.
		 * @param content content or artifact for a dbpedia entry.
		 * @return true if the database has been successfully updated, false otherwise
		 * @throws SQLException
		 */
	public abstract boolean map(String newLine) throws SQLException;
	

	
	protected String validate(final String label) {
		String validLabel = null;
		
		if(label.length() > 2) {
			String[] terms = label.split(" ");
			if( terms.length <=  4) {
				if( areCharactersValid(label)) {
					int indexParenthesis = label.indexOf("(");
					if( indexParenthesis == -1) {
						validLabel = label;
					}
					else if( indexParenthesis > 4) {
						validLabel = label.substring(0, indexParenthesis-1);
					}
				}
			}
		}
		return validLabel;
	}
	
	private boolean areCharactersValid(final String label) {
		boolean passed = true;
		char[] chars = label.toCharArray();
		
		for( char character : chars ) {
			if(!isCharacterValid(character)) {
				passed = false;
				break;
			}
		}
		
		return passed;
	}
	
	private boolean isCharacterValid(char character) {
		int hexChar = (int)character;
		return ((hexChar > 0x40 && hexChar < 0x5B) ||	// Lower case characters
				(hexChar > 0x60 && hexChar < 0x7B) ||	// Upper case character	
				(hexChar > 0x2B && hexChar < 0x2F) || 	// Punctuation
				(hexChar == 0x5F || hexChar == 0x26 || hexChar == 0x20) || hexChar == 0x28 || hexChar == 0x29);
		
	}

	
	public boolean reduce() { return false; }
}

//---------------------------  EOF -------------------------------------------------
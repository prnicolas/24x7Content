// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.semantics.dbpedia.etl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.c24x7.semantics.dbpedia.etl.ADbpediaEtl;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>Class to configure the parameters to extract structured data
		 * from source data sets such as dbpedia, freebase...
		 * @author Patrick Nicolas
		 * @date 10/23/2011
		 */
public final class CDatasetExtractor {

	protected String 			_marker = null;
	protected int 				_markerLen = -1;
	protected AResConverter 	_resConverter = null;
	protected AResConverter 	_labelConverter = null;
	
			/**
			 * <p>Base converter that extract NGrams from a sequence of contiguous characters.</p>
			 * @author Patrick Nicolas
			 * @date 12/11/2011
			 */
	public static class AResConverter {
		public String convert(final String original) {
			return CStringUtil.decodeLatin1(original);
		}
		
		protected static String convertAndDecodeCompound(final String original) {
			String converted = null;
			
			if( original != null) {
				String decodedOriginal = CStringUtil.decodeLatin1(original);
				converted  = convertCompound(decodedOriginal);
			}
			return converted;
		}
				
		public static String convertCompound(final String decodedOriginal) {
			String converted = null;

			if( decodedOriginal != null ) {
				char[] chars = decodedOriginal.toCharArray();
				int hexChar = 0;
							
						/*
						 * Detect a second upper case letter in the compound name
						 */
				StringBuilder extractedBuf = new StringBuilder();
				extractedBuf.append(chars[0]);
					
				int lastCharIndex = chars.length-1;
				for( int j = 1; j < lastCharIndex; j++) {
					hexChar = chars[j];
						
							/*
							 * If the parser encounter a Upper case character which
							 * is not the last character... then create a compound
							 * name by inserting a blank or space character.
							 */
					if (hexChar > 0x40 && hexChar < 0x5B) {
						extractedBuf.append(" ");
					}
					extractedBuf.append(chars[j]);
				}
				extractedBuf.append(Character.toLowerCase(chars[lastCharIndex]));
				converted = extractedBuf.toString();
			}
			
			return converted;
		}
	}
	
	
			/**
			 * <p>Default converter that extract NGrams from a sequence of contiguous characters as
			 * follow  'Quantum_Mechanics' -> Quantum Mechanics'
			 * @author Patrick Nicolas
			 * @date 12/11/2011
			 */
	public static class NHyphenResConverter extends AResConverter {
		public String convert(final String original) {
			String decodedOriginal = CStringUtil.decodeLatin1(original);
			return (decodedOriginal != null) ? decodedOriginal.replace("_", " ") : null;
		}
	}
	
			/**
			 * <p>Default converter that extract NGrams from a sequence of contiguous characters as
			 * follow  'QuantumMechanics' -> Quantum Mechanics'
			 * @author Patrick Nicolas
			 * @date 12/11/2011
			 */
	public static class NUpperCaseResConverter extends AResConverter {
		public String convert(final String original) {
			return convertAndDecodeCompound(original);
		}
	}
	
			/**
			 * <p>Default converter that extract NGrams from a sequence of contiguous characters as
			 * either  'QuantumMechanics' or 'Quantum_Mechanics' -> Quantum Mechanics'
			 * @author Patrick Nicolas
			 * @date 12/11/2011
			 */
	public static class NHybridResConverter extends AResConverter {
		public String convert(final String original) {
			String decodedOriginal = CStringUtil.decodeLatin1(original);
			if(decodedOriginal != null) {
				decodedOriginal = (decodedOriginal.indexOf("_") == -1) ?  
						          convertCompound(original) : 
						          decodedOriginal.replace("_", " ");
			}
			return decodedOriginal;
		}
	}
	
	
	public CDatasetExtractor() {
		this(null);
	}
	
	public CDatasetExtractor(AResConverter resConverter) {
		this(new NHybridResConverter(), resConverter);
	}
	
	
	public CDatasetExtractor(AResConverter labelConverter, 
							 AResConverter resConverter) {
		_resConverter = resConverter;
		_labelConverter = labelConverter;
		_marker = ADbpediaEtl.DBPEDIA_RES_MARKER;
		_markerLen = ADbpediaEtl.DBPEDIA_RES_MARKER.length();
	}
	

	
			/**
			 * <p>Count the number of line in the datasets or file.</p> 
			 * @param fileName name of the file
			 * @param interval number of count between status display.
			 * @return number of lines or records
			 */
	public final long countRecords(	final String fileName, 
									Map<String, Object> map) {
		long counter = -1L;
		
		if( fileName != null ) {
			BufferedReader reader = null;
				
			try {
				FileInputStream fis = new FileInputStream(fileName);
				reader = new BufferedReader(new InputStreamReader(fis));
				String newLine = null;
				String extractedKeyword = null;
					
				while ((newLine = reader.readLine()) != null) {
					extractedKeyword = extractLabel(newLine);
					if( extractedKeyword != null ) {
						if( map != null ) {
							map.put(extractedKeyword, null);
						}
						counter++;
					}
				}
			}
			catch( IOException e) {
				CLogger.error("CExtractor.countRecords: " + e.toString());
			}
			finally {
				if( reader != null ) {
					try {
						reader.close();
					}
					catch( IOException e) {
						CLogger.error("CExtractor.countRecords: " + e.toString());
					}
				}
			}
		}
		return counter;
	}
	
	public String extractLabel(final String newLine) {
		return extract(newLine, false, _labelConverter);
	}
	
		
	
	public String extractResource(final String newLine, boolean lastIndex) {
		return extract(newLine, true, _resConverter);
	}
		
	
	
	public final String extractImage(final String lineSubStr) {
		String imageUrl = null;
		
		if( lineSubStr != null) {
			int indexEndImageUrl = lineSubStr.indexOf(">", CDbpediaImageEtl.DBPEDIA_IMG_PREFIX.length());
			if( indexEndImageUrl != -1) {
				imageUrl = lineSubStr.substring(CDbpediaImageEtl.DBPEDIA_IMG_PREFIX.length(), indexEndImageUrl);
			}
		}
		return imageUrl;
	}
		
	

	public final List<String> scanCharacter(final String fileName, int pos) {
		List<String> found = new LinkedList<String>();
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
		
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(Character.isLowerCase(line.charAt(pos))) {
					found.add(line);
				}
			}
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Failed to find parts of speech in " + fileName);
		}
		
		return found;

	}
	
	
					// --------------------------
					//  Private Supporting Methods
					// ---------------------------
	
	protected String extract(final String newLine, boolean lastIndex, AResConverter converter) {
		String extractedLabel = null;
		String label = null;
		
		int indexStartRes = (lastIndex ) ? newLine.lastIndexOf(_marker) : newLine.indexOf(_marker);
		if( indexStartRes != -1) {
			String resStr = newLine.substring(indexStartRes + _markerLen);
			
			int indexEndOntoStr = resStr.indexOf(">");
			if( indexEndOntoStr != -1) {
				label = resStr.substring(0, indexEndOntoStr);
				if(label.length() > 2 ) {
					extractedLabel = converter.convert(label);
				}
			}
		}
		return extractedLabel;
	}
}

// -------------------------  EOF ------------------------------------------------

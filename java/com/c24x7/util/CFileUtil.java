// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Class to read key-value pairs from content file or or write to key-value pairs to content file.</p> 
		 * @author Patrick Nicolas
		 * @date 06/23/2011
		 */
public final class CFileUtil {
	public static final char 	COMMENTS_FIRST_CHAR 	=';';

	
	public interface IValueExtractor {
		public void extract(final String key, final String value);
	}
	

			/**
			 * <p>Generic method to extract fields from a configuration file. The
			 * fields are defined as a text line delimited by a separator string.</p>
			 * @param fileName Name of the file
			 * @param delimiter line or field separator
			 * @param entriesList List of entries to be collected.
			 * @throws IOException if the file is improperly formatted.
			 */
	public static boolean readEntries(	final String	fileName, 
								  	 	final String	delimiter,
								  	 	List<String> 	entriesList) throws IOException, FileNotFoundException {
		
		BufferedReader reader = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
		
			int indexDelim = -1;
			
			while ((line = reader.readLine()) != null) {	
				if(line.length() > 0 && line.charAt(0) != COMMENTS_FIRST_CHAR) {
					if( delimiter != null) {
						
						StringBuilder buf = new StringBuilder();
						if(line.length() > 0 && line.charAt(0) != COMMENTS_FIRST_CHAR) {
							indexDelim = line.indexOf(delimiter);
							if( indexDelim != -1) {
								entriesList.add(buf.toString());
								buf = new StringBuilder();
							}
							else {
								buf.append(line);
								buf.append("\n");
							}
						}
					}
					else {
						entriesList.add(line.trim());
					}
				}
			}
		}
			
		finally {
			if( reader != null) {
				reader.close();
			}
		}
		
		return (entriesList.size() > 0);
	}

	

			/**
			 * <p>Generic method to extract fields from a configuration file. The
			 * fields are defined as a text line delimited by a separator string.</p>
			 * @param fileName Name of the file
			 * @param delimiter line or field separator
			 * @param fieldsList List of fields to be collected.
			 * @param numFields Number of fields to be collected
			 * @throws IOException if the file is improperly formatted.
			 */
	public static boolean readFields(	final String 	fileName, 
								  		final String 	delimiter,
								  		List<String[]> 	fieldsList,
								  		int 			numFields ) throws IOException, FileNotFoundException {
		
		boolean succeed = false;
		String[] fields = null;
		BufferedReader reader = null;
			
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
		
			int recordsCounter = 0, fieldsCounter = 0;
			int indexDelim = -1;
			while ((line = reader.readLine()) != null) {	
				
				if(line.length() > 0 && line.charAt(0) != COMMENTS_FIRST_CHAR) {
					line = line.trim();
					fieldsCounter = recordsCounter%numFields;
					indexDelim = line.indexOf(delimiter);
					
					if( indexDelim != -1) {
						recordsCounter++;
					}
					else {
						if( fieldsCounter == 0) {
							if( fields != null) {
								fieldsList.add(fields);
							}
							fields = new String[numFields];
						}
						fields[fieldsCounter] = line;
					}
				}
			}
			if( fields != null) {
				fieldsList.add(fields);
				succeed = true;
			}
		}
		finally {
			if( reader != null) {
				reader.close();
			}
		}
		
		return succeed;
	}





			/**
			 * <p>Generic method to extract fields from a configuration file. The
			 * fields are defined as multiple line delimited by a separator string.</p>
			 * @param fileName Name of the file
			 * @param delimiter line or field separator
			 * @param fieldsList List of fields to be collected.
			 * @param numFields Number of fields to be collected
			 * @throws IOException if the file is improperly formatted.
			 */
	public static boolean readBufferedFields(	final String 	fileName, 
								  				final String 	delimiter,
								  				List<String[]> 	fieldsList,
								  				int 			numFields ) throws IOException, FileNotFoundException {
		
		boolean succeed = false;

		BufferedReader reader = null;
			
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
	
			int recordsCounter = 0, 
				fieldsCounter = 0,
				indexDelim = -1;
			
			StringBuilder buf = new StringBuilder();
			String[] fields = new String[numFields];
			
			while ((line = reader.readLine()) != null) {	
				
				if(line.length() > 1 && line.charAt(0) != COMMENTS_FIRST_CHAR) {
					line = line.trim();
					
					indexDelim = line.indexOf(delimiter);
					if( indexDelim != -1) {
						fields[fieldsCounter] = buf.toString();
						if(fieldsCounter == 2) {
							fieldsList.add(fields);
							fields = new String[numFields];
						}
						recordsCounter++;
						fieldsCounter = recordsCounter%numFields;
						buf = new StringBuilder();
					}
					else {
						buf.append(line);
						buf.append("\n");
					}
				}
			}
		}
		finally {
			if( reader != null) {
				reader.close();
			}
		}
		
		return succeed;
	}
		
	
	
	
			
			/**
			 * <p>Read a content file with a predefined delimiter to generate a lookup table.</p>
			 * @param fileName name of the file that contain the delimited key value pairs
			 * @param map map of key values pair to update with the content of the file.
			 * @throws IOException if the content (key-value pairs) file is not properly formatted or the content file is not properly defined.
			 */
	public static void readKeysValues(	final String 		fileName, 
									  	Map<String, String> map) throws IOException {
		readKeysValues(fileName, CEnv.KEY_VALUE_DELIM, map);
	}

	
	
			/**
			 * <p>Read a content file with a predefined delimiter to generate a lookup table.</p>
			 * @param fileName name of the file that contain the delimited key value pairs
			 * @param delimiter delimiter used by the key values pair
			 * @param map map of key values pair to update with the content of the file.
			 * @throws IOException if the content (key-value pairs) file is not properly formatted or the content file is not properly defined.
			 */
	public static void readKeysValues(	final String 		fileName, 
									  	final String 		delimiter, 
									  	Map<String, String> map) throws IOException, FileNotFoundException {
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String 	line 		= null, 
					keyTerm 	= null, 
					valueTerm 	= null;
			
			int delimIndex = -1;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.length() > 1 && line.charAt(0) != COMMENTS_FIRST_CHAR) {
	
					if( delimiter != null ) {
						delimIndex = line.indexOf(delimiter);
						if( delimIndex == -1) {
							throw new IOException("Incorrectly format for content file: " + fileName + " @ " + line + " delimiter: " + delimiter);
						}
						keyTerm = line.substring(0, delimIndex);
						valueTerm = line.substring(delimIndex+1).trim();
						map.put(keyTerm, valueTerm);
					}
					else {
						map.put(line, null);
					}
				}
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
	}
	
			/**
			 * <p>Read a content file with a predefined delimiter to generate a lookup table.</p>
			 * @param fileName name of the file that contain the delimited key value pairs
			 * @param delimiter delimiter used by the key values pair
			 * @param map map of key values pair to update with the content of the file.
			 * @throws IOException if the content (key-value pairs) file is not properly formatted or the content file is not properly defined.
			 */
	public static void readKeysValues(	final String 		fileName, 
										IValueExtractor	valueExtractor) throws IOException {
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null, keyTerm = null, valueTerm = null;
			int delimIndex = -1;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				delimIndex = line.indexOf(CEnv.KEY_VALUE_DELIM);
				if( delimIndex == -1) {
					throw new IOException("Incorrectly format for content file: " + fileName);
				}
				keyTerm = line.substring(0, delimIndex);
				valueTerm = line.substring(delimIndex+1).trim();
				valueExtractor.extract(keyTerm, valueTerm);
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
	}


	
	/**
	 * <p>Read a content file into a string buffer.</p>
	 * @param fileName name of the file that contain the delimited key value pairs
	 * @return String content of the file
	 * @throws the content file is not properly defined.
	 */
	
	public static boolean read(final String fileName, StringBuilder buffer) throws IOException {
		boolean succeed = false;
		
		BufferedReader reader = null;
		SoftReference<StringBuilder> bufRef = new SoftReference<StringBuilder>(new StringBuilder());

		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				if( !line.equals("") && bufRef != null && bufRef.get() != null ) {
					bufRef.get().append(line);
					bufRef.get().append(" ");
				}
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		
		if (bufRef != null && bufRef.get() != null) {
			buffer.append(bufRef.get().toString());
			succeed = true;
		}
		
		return succeed;
	}
	
	
	
			/**
			 * <p>Write a content string into a file.</p>
			 * @param fileName name of the file the content is to be written
			 * @param content content string to be written into a file
			 * @throws IOException if file is not properly specified.
			 */
	public static boolean write(final String fileName, 
							 	final String content) throws IOException {
		
		boolean succeed = false;
		PrintWriter writer = null;
		FileOutputStream fos = null;

		try {	
			fos = new FileOutputStream(fileName);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
			writer.write(content, 0, content.length());
			succeed = true;
			writer.close();
			fos.close();
			
			if( writer.checkError()) {
				throw new IOException("Cannot save content");
			}
		}

		finally {
			if( writer != null ) {
				writer.close();

				if( writer.checkError()) {
					throw new IOException("Cannot save content");
				}		
				fos.close();
			}
		}
		
		return succeed;
	}
	
	
				/**
				 * <p>Count the number of line in the datasets or file.</p> 
				 * @param fileName name of the file
				 * @param interval number of count between status display.
				 * @return number of lines or records
				 */
	public static final long countLines(final String fileName, long interval) {
		long counter = 0L;
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));

			while (reader.read() != -1) {
				counter++;
				if( counter % interval == 0) {
					CLogger.info(String.valueOf(counter));
				}
			}
		
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Cannot count records for " + fileName + ": " + e.toString());
		}
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) {
					CLogger.error("Cannot count records for " + fileName + ": " + e.toString());
				}
			}
		}
		
		return counter;
	}
	
	
		/**
		 * <p>Access the line of configuration file that contains the part of speech.</p>
		 * @param fileName  file name to scan
		 * @param l
		 * @return
		 */
	public static List<String> find(final String fileName, final String expression) {
		return find(fileName, expression, -1, null);
	}
	
			/**
			 * <p>Access the line of configuration file that contains the part of speech.</p>
			 * @param fileName  file name to scan
			 * @param partOfSpeech part of speech
			 * @return list of 
			 */
	public static List<String> find(final String fileName, final String expression, long[] count) {
		return find(fileName, expression, -1, count);
	}

	
	
			/**
			 * <p>Access the line of configuration file that contains the part of speech.</p>
			 * @param fileName  file name to scan
			 * @param partOfSpeech part of speech
			 * @return
			 */
	public static List<String> find(final String fileName, final String partOfSpeech, int pos, long[] count) {
		List<String> listOfRecords = new LinkedList<String>();
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
		
			int partOfSpeechIndex = -1;
			long counter = 0;
			long numLines = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.toLowerCase();
				partOfSpeechIndex = line.indexOf(partOfSpeech);
				if( pos != -1) {
					if(partOfSpeechIndex == pos) {
						listOfRecords.add(line);
						counter = numLines;
					}
				}
				else if( partOfSpeechIndex != -1) {
					listOfRecords.add(line);
					counter = numLines;
				}
				numLines++;
			}
			
			if( count != null) {
				count[0] = counter;
				count[1] = numLines;
			}
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Failed to find " + partOfSpeech + " in " + fileName);
		}
		
		return listOfRecords;
	}
	
	

	
	public static Map<String, String> find(final String fileName, final String[] partOfSpeeches, int pos) {
		Map<String, String> filterMap = new HashMap<String, String>();
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
		
			int partOfSpeechIndex = -1;

			String line = null;
			while ((line = reader.readLine()) != null) {
				for( String partOfSpeech : partOfSpeeches) {
					partOfSpeechIndex = line.indexOf(partOfSpeech);
					if( pos != -1) {
						if(partOfSpeechIndex == pos) {
							filterMap.put(partOfSpeech, line);
						}
					}
					else if( partOfSpeechIndex != -1) {
						filterMap.put(partOfSpeech, line);
					}
				}
				if( filterMap.size() == partOfSpeeches.length) {
					break;
				}
			}
			reader.close();
		}
		catch( IOException e) {
			CLogger.error("Failed to find parts of speech in " + fileName);
		}
		
		return filterMap;
	}
}

// --------------------------------------  EOF -------------------------------------


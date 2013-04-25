// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public final class CFastIO {
	
	
	public static String read(final String fileName) throws IOException {
		BufferedReader reader = null;
		StringBuilder buf = new StringBuilder();

		try {
			FileInputStream fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if( !line.equals("")) {
					buf.append(line);
				}
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		return buf.toString();
	}
	
	
	public static void write(final String fileName, 
							final String content) throws IOException {
		PrintWriter writer = null;
		FileOutputStream fos = null;

		try {	
			fos = new FileOutputStream(fileName);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
			writer.write(content, 0, content.length());
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
	}
		
}


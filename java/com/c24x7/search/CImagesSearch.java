// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.c24x7.exception.SearchException;
import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.CEnv;
import com.c24x7.util.CProfile;
import com.c24x7.util.logs.CLogger;



				/**
				 * <p>Class to manage the search for images related to a context and a keyword.</p>
				 * @author Patrick Nicolas
				 * @date 06/04/2011
				 */

public final class CImagesSearch extends AItemSearch {
	
	protected static NImageEntry[] IMAGE_ENTRY_0 		= new NImageEntry[0];
	public final static int MAX_NUM_IMAGES			= 16;
	public final static String DEFAULT_REFERER 		= "www.24x7content.com";
	public final static String URL_IMAGES_SEARCH 	= "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";
	public final static String KEY_STR 				= "ABQIAAAAoP1x4Uo0ttwH5Ds7BWyZwBQFpApVUwiP3wt_jIpAkrwdIbsA1RSqXrUEn8fMDzpcs0vNUPpNKhaWsQ";
			
			/**
			 * <p>Nested static public class that encapsulates the data for images
			 * to be incorporate in generated content.</p>
			 * @author Patrick Nicolas
			 * @date 04/18/2011
			 */
	public static class NImageEntry implements NIEntry {
		protected final static String FIELD_DELIMITER 	= "$$$";
		protected final static int MIN_IMG_SIZE 			= 120;
		protected final static int SIZE_IMG_DISPLAY 		= 90;
		protected final static int MAX_TUMBNAIL_HEIGHT 	= 112;
		protected final static int MIN_TUMBNAIL_HEIGHT 	= 68;
		
		protected String _title = null;
		protected String _url   = null;
		protected int[]  _size  = null;
		
				/**
				 * <p>Create a Image structure information object that contains
				 * the title, URL and dimension of the image.</p>
				 * @param cursor JSON stream about an image 
				 * @throws JSONException if JSON array is mal-formed.
				 */
		public NImageEntry(final JSONObject cursor) throws JSONException {
			_title = cursor.getString("titleNoFormatting");
			_url = cursor.getString("url");
			_size = new int[] {
				cursor.getInt("width"),
				cursor.getInt("height")
			};
		}
		
		
		public NImageEntry(final String url) {
			_url = url;
		}
		
		
		public final String[] resize() {
			return resize(SIZE_IMG_DISPLAY);
		}
		
				/**
				 * <p>Resize the image entry to fit one specific dimension
				 * @param maxSize maximum size or constraint for this image entry..
				 * @return two dimension array {width, height} for this image entry.
				 */
		
		public final String[] resize(final int width) {
			String[] newSize = null;
		
			if( _size != null ) {
				int ht = (int)((_size[1]*width)/(double)_size[0]);
				if( ht > MAX_TUMBNAIL_HEIGHT) {
					ht = MAX_TUMBNAIL_HEIGHT;
				}
				else if ( ht < MIN_TUMBNAIL_HEIGHT) {
					ht = MIN_TUMBNAIL_HEIGHT;
				}
				newSize = new String[] { String.valueOf(width), String.valueOf(ht) };
			}

			return newSize;
		}
		
		
		
		
						/**
						 * <p>Test whether the size of the image is big enough to be considered as a candidate for
						 * the generation of content..</p>
						 * @param jsonObjectSize JSON object that contains the size of the image
						 * @return true if image is large enough, false otherwise..
						 * @throws JSONException
						 */
		public static boolean isValidSize(final JSONObject jsonObjectSize)  throws JSONException {
			return (jsonObjectSize.getInt("width") > MIN_IMG_SIZE) && 
			       (jsonObjectSize.getInt("height") > MIN_IMG_SIZE);
		}
		
		public final String save() {
			StringBuilder buf = new StringBuilder("_url");
			buf.append(FIELD_DELIMITER);
			buf.append(_title);
			buf.append(FIELD_DELIMITER);
			buf.append(_size[0]);
			buf.append(FIELD_DELIMITER);
			buf.append(_size[1]);
			return buf.toString();
		}
		
		public void load(final String dbRecord) {
			if( dbRecord != null ) {
				String[] fields = dbRecord.split(FIELD_DELIMITER);
				if( fields != null && fields.length == 4) {
					try {
						_size[0] = Integer.parseInt(fields[2]);
						_size[1] = Integer.parseInt(fields[3]);
						_url = fields[0];
						_title = fields[1];
					}
					catch( NumberFormatException e ) {
						CLogger.error("Incorrect format " + dbRecord);
					}
				}
				else {
					CLogger.error("Cannot load " + dbRecord);
				}
			}
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder("\nImage: ");
			buf.append(_url);
			buf.append(", ");
			buf.append(_title);
			buf.append(", (");
			buf.append(_size[0]);
			buf.append(CEnv.FIELD_DELIM);
			buf.append(_size[1]);
			buf.append(")");
			
			return buf.toString();
		}
		
		public final int getWidth() {
			return _size[0];
		}
		
		public final int getHeight() {
			return _size[1];
		}
		
		public final String getUrl() {
			return _url;
		}
		
		public final String getTitle() {
			return _title;
		}
	}
	
	protected List<NImageEntry> _imagesList = null;
	protected String _keyword = null;

	

			/**
			 * <p>Create a image search method for a web site 'referrer' for a list of keywords. For 
			 * this constructor we assume that the frequency of those words are identical (equal to 1)</p>
			 * @param keyword array of search words (the frequency of those words are not taken into account
			 */
	public CImagesSearch(final String keyword) {
		_keyword = keyword;
	}
	
			
		
			/**
			 * <p>Retrieve the map of images from the Google services</p>
			 * @param model model of the document to update with the image search results.
			 */
	
	@Override
	public void getItems(CSummaryModel model) {
		CContentItemsMap searchResultsMap = model.getContentItemsMap();
		NImageEntry[] imgArray = _imagesList.toArray(IMAGE_ENTRY_0);
		searchResultsMap.add(_keyword, searchResultsMap.new NImages(imgArray));
	}
	

	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("\nImages for ");
		buf.append(_keyword);
		
		for( NImageEntry entry : _imagesList) {
			buf.append("\n");
			buf.append(entry.toString());
		}
		
		return buf.toString();
	}

	
			/**
			 * <p>Search all the images related to the list of keywords. This method 
			 * is called either synchronously or though the parent thread..</p>
			 * @throws SearchException if search query
			 */
	public void search() throws SearchException {
		CProfile.getInstance().time("Images search (S): ");
		String searchQueryStr = _keyword;

		for( int j = 0; j < MAX_NUM_IMAGES; j += 4) {
			search(searchQueryStr, j);
		}	
		CProfile.getInstance().time("Images search (E): ");
	}
	
			/**
			 * <p>Retrieve the list of images from the services.</p> 
			 * @return list of images
			 */
	public final List<NImageEntry> getImagesList() {
		return _imagesList;
	}
	
	
	
			/**
			 * <p>Return the results sets for the images search
			 * @param searchWords The search keyword
			 * @param pageIndex index for retrieving the page
			 * @return List of Images structure
			 * @throws SearchException if search failed.
			 */
	protected void search(final String searchWord, 
						final int pageIndex) throws SearchException {
		
		StringBuilder buf = new StringBuilder(URL_IMAGES_SEARCH);
		JSONObject json = null;
		
		try {
					/*
					 * Build the JSON request string..
					 */
			buf.append(URLEncoder.encode(searchWord, "UTF-8"));
			buf.append("&key=");
			buf.append(KEY_STR);
			buf.append("&start=");
			buf.append(String.valueOf(pageIndex));
			
			URL url = new URL(buf.toString());
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer", DEFAULT_REFERER);
			
					/*
					 * Retrieve the JSON response...
					 */
			String line = null;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			json = new JSONObject(builder.toString());
			json = json.getJSONObject("responseData");
			String[] names  = JSONObject.getNames(json);
			JSONArray array = json.getJSONArray(names[1]);
			
					/*
					 * Extract the list of images from the JSON stream..
					 */
			if( array.length() > 0) {
				_imagesList = new ArrayList<NImageEntry>();
				
				JSONObject cursor = null;
				for( int j = 0; j < array.length(); j++) {
					cursor = array.getJSONObject(j);
					if( cursor != null) {
						if( NImageEntry.isValidSize(cursor) ) {
							_imagesList.add(new NImageEntry(cursor));
						}
					}
				}
			}
		}
		catch( MalformedURLException e) {
			throw new SearchException("Images search for " + searchWord + " " + e.toString());
		}
		catch( IOException e) {
			throw new SearchException("Images search for " + searchWord + " " + e.toString());
		}
		catch( JSONException e) {
			throw new SearchException("Images search for " + searchWord + " JSON: " + json.toString());
		}
	}
	
}

// ----------------------  EOF --------------------------------------------
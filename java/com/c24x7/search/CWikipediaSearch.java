// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


import com.c24x7.exception.SearchException;
import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.CProfile;


public class CWikipediaSearch extends AItemSearch {
	protected static final String WIKIPEDIA_EXCEPTION 	= "Could not retrieve wikipedia for ";
	protected static final String WIKIPEDIA_URL_GET 		= "http://en.wikipedia.org/w/index.php?title=";
	protected static final String WIKIPEDIA_URL_SEARCH 	= "http://en.wikipedia.org/w/index.php?title=SpecialSearch&search=";
	
	protected String 			_keyword 		 = null;
	protected NWikipediaEntry _wikipediaEntry = null;
	
	public static class NWikipediaEntry {
		protected String _keyword = null;
		protected String _url = null;
		
		public NWikipediaEntry(final String keyword) throws SearchException {
			_keyword = keyword;
			_url = CWikipediaSearch.searchUrl(keyword);
		}
		
		public final String getKeyword() {
			return _keyword;
		}
		
		public final String getUrl() {
			return _url;
		}
	}
	
	
	public CWikipediaSearch(final String keyword) {
		_keyword = keyword;
	}
	
	
		/**
		 * <p>Retrieve the Wikipedia description for the keywords of the content</p>
	  	 * @param searchResultsMap reference to the search results map
	  	 */
	public void getItems(final CSummaryModel model) {
		CContentItemsMap searchResultsMap = model.getContentItemsMap();
		searchResultsMap.add(_keyword, searchResultsMap.new NWikipedia(_wikipediaEntry));
	}

	
		
	protected void search() throws SearchException {
		CProfile.getInstance().time("Wikipedia search (S): ");
		_wikipediaEntry = CWikipediaSearch.search(_keyword);
		CProfile.getInstance().time("Wikipedia search (E): ");
	}

			/**
			 * <p>Retrieve a Wikipedia document using its title</p>
			 * @param title title of the document
			 * @return HTML page with the following title
			 * @throws SearchException if the relevant document has not been found
			 */
	public static final String get(final String title) throws SearchException {
		if( title == null) {
			throw new IllegalArgumentException("Wikipedia title undefined");
		}
		String searchResults = null;
		
		StringBuilder buf = new StringBuilder(WIKIPEDIA_URL_GET);
		try {
			buf.append(URLEncoder.encode(title, "UTF-8"));
			
			searchResults =  retrieve(buf.toString());
			if( searchResults == null ) {
				int indexSpace = title.indexOf(" ");
				if( indexSpace != -1) {
					buf = new StringBuilder(WIKIPEDIA_URL_GET);
					buf.append(URLEncoder.encode(title.substring(0, indexSpace), "UTF-8"));
					searchResults = retrieve(buf.toString());
				}
			}
		}
		catch( UnsupportedEncodingException e) {
			throw new SearchException(WIKIPEDIA_EXCEPTION  + title + ": " + e.toString());
		}
		
		return searchResults;
	}
	
			/**
			 * <p>Retrieve a Wikipedia document using a search query</p>
			 * @param searchContent search content for the query
			 * @return HTML page with the following title
			 * @throws SearchException if the relevant document has not been found
			 */
	public static final NWikipediaEntry search(final String searchContent) throws SearchException  {
		return new NWikipediaEntry(searchContent);
	}
	
	
			/**
			 * <p>Search the Wikipedia URL for a specific content.</p>
			 * @param searchContent keyword or part of speech to search for.
			 * @return URL for the wikipedia entry.
			 * @throws SearchException
			 */
	public static final String searchUrl(final String searchContent) throws SearchException  {
		if( searchContent == null) {
			throw new IllegalArgumentException("Wikipedia keyword undefined");
		}
		String queryContent = searchContent.replace(" ", "+");
		StringBuilder buf = new StringBuilder(WIKIPEDIA_URL_SEARCH);
		buf.append(queryContent);
		
		return buf.toString();
	}
	
	
					// -------------------------
					// Private supporting methods
					// ---------------------------
	
	protected static final String retrieve(final String request) throws SearchException {
		String wikipediaResults = null;
		BufferedReader reader = null;
		
		try {
			URL url = new URL(request);
			URLConnection connection = url.openConnection();
	
			String line = null;
			StringBuilder builder = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			wikipediaResults = builder.toString();
			reader.close();
		}
		catch( MalformedURLException e ) {
			throw new SearchException(WIKIPEDIA_EXCEPTION + request + ": " + e.toString());
		}
		catch( IOException e) {
			throw new SearchException(WIKIPEDIA_EXCEPTION + request + ": " + e.toString());
		}
		finally {
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e) {
					throw new SearchException(WIKIPEDIA_EXCEPTION + request + ": " + e.toString());
				}
			}
		}
		
		return wikipediaResults;
	}
}


// -------------------------  EOF ------------------------------------
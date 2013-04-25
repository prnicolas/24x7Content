// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.nlservices;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.xml.sax.SAXException;

import com.c24x7.exception.SearchException;
import com.c24x7.output.html.CHTMLPage;
import com.c24x7.output.html.CHTMLStyle;
import com.c24x7.webanalyzer.CWebContentExtractor.NHtmlSyntaxRule;
import com.c24x7.webanalyzer.CWebDocument;
import com.c24x7.search.CWebSearch;
import com.c24x7.search.CWebSearch.NSearchItem;
import com.c24x7.util.logs.CLogger;

			/**
			 * <p>Analyzer class to process and classify user defined content.</p>
			 * @author Patrick Nicolas
			 * @date 04/19/2011
			 */
public final class CSearchService {
	private static final long 	DEFAULT_SLEEP_TIME 		= 550L;
	private static final int 	MAX_NUM_SEARCH_RESULTS 	= 25;
	
	private static Metadata metadata 	= new Metadata();
	private static ParseContext context = new ParseContext();

	private int				_numThreads 	= 0;
	private NSearchItem     _item 			= null;
	private List<CWebDocument>	_webPagesList	= null;
	private int 			_searchCount 	= MAX_NUM_SEARCH_RESULTS;
	private	boolean			_urlOnly		= false;
	private long 			_sleepTime 		= DEFAULT_SLEEP_TIME;
	private String			_error			= null;
	
	
		/**
		 * <p>Create an extractor with a specific configuration.</p>
		 * @param config configuration for the extractor.
		 */
	public CSearchService() {
		this(false, MAX_NUM_SEARCH_RESULTS);
	}
		
	
			/**
			 * <p>Create an extractor with a specific configuration.</p>
			 * @param config configuration for the extractor.
			 */
	public CSearchService(boolean urlOnly) {
		this(urlOnly, MAX_NUM_SEARCH_RESULTS);
	}
	
		/**
		 * <p>Create an extractor with a specific configuration.</p>
		 * @param config configuration for the extractor.
		 */
	public CSearchService(boolean urlOnly, int searchCount) {
		_searchCount = searchCount;
		_urlOnly = urlOnly;
	}

	
			/**
			 * <p>Access the error message collected during the execution
			 * of all the searching threads (one per URL).</p>
			 * @return description of the error
			 */
	public String getError() {
		return _error;
	}
	
		/**
		 * <p>Extracts the artifacts (map location, images, videos... ) references from the original content. 
		 * The references or part of the speech are used to build a model of the content as a set of 
		 * Key(Part of the speech)-Value pairs map of artifacts</p>
		 * @param searchString keyword or search string.
		 */
	
	public void execute(final String searchString) throws SearchException {
		if( searchString == null ) {
			throw new IllegalArgumentException("Cannot search with an undefined keyword or expression");
		}
				/*
				 * Extract abstract and URL from search engine
				 */
		CWebSearch yahooSearch = new CWebSearch(searchString, _searchCount);	
		yahooSearch.search();
		List<NSearchItem> items = yahooSearch.getYahooSearchItems();
			
				/*
				 * Parse the content of each URI target or pages.
				 */
		if( _urlOnly ) {
			_webPagesList = new LinkedList<CWebDocument>();
			getContent(items);
			_error = null;
			
				/*
				 * Launch a thread that wait for all the requests
				 * to be completed..
				 */
			new Thread(new Runnable() {
				public void run() {
					do {
						try {
							Thread.sleep(_sleepTime);
						}
						catch( InterruptedException e) { 
							_error = e.toString();
						}
					}while( !isCompleted());
				}
			}).start();
		}
	}
	
	
	
			/**
			 * <p>Extract the content from documents or items defined by their URI.</p>
			 * @param itemsList list of search result items.
			 */
	public void getContent(final List<NSearchItem> itemsList) {
		if( itemsList == null ) {
			throw new IllegalArgumentException("Cannot extract content from undefined URI");
		}
		
		for( NSearchItem item : itemsList) {
			_item = item;
			
			/*
			 * Starts a thread to extract content from
			 * each URL previously returned.
			 */
			new Thread(new Runnable() {
				public void run() {
					try {
						CWebDocument doc = getContent(_item.getUrl());
						if( doc != null) {
							synchronized(_webPagesList ) {
								_webPagesList.add(doc);
								_numThreads--;
							}
						}
					}
					catch( IOException e) { 
						_error = e.toString();
						CLogger.error(e.toString());
					}
					catch (SearchException e) {
						_error = e.toString();
						CLogger.error(e.toString());
					}
				}
			}).start();
			_numThreads++;
			CLogger.info("Started " + _numThreads + " threads");
		}
	}
		
		
	public final boolean isCompleted() {
		return (_numThreads == 0);
	}


	public String toHtml() {
		CHTMLPage htmlPage = new CHTMLPage(new CHTMLStyle());
		htmlPage.addCanvas();
//		htmlPage.write(_modelsList);
		
		return htmlPage.toString();
	}
	
						// ---------------------------
						// Private Supporting Methods
						// ---------------------------
	
	
	public final CWebDocument getContent(final String urlStr) throws IOException, SearchException  {
		CWebDocument extractedContent = null;
		DataInputStream htmlInput = null;
				/*
				 * Extract the content from this URL..
				 */
		try {
			BodyContentHandler contentHandler = new BodyContentHandler();
			URL url = new URL(urlStr);
			htmlInput = new DataInputStream(url.openStream());
			AutoDetectParser parser = new AutoDetectParser();
			parser.parse(htmlInput, contentHandler, metadata, context);
			String content = contentHandler.toString();
			extractedContent = new NHtmlSyntaxRule().extract(content);
		}
		
		catch( MalformedURLException e) {
			throw new SearchException(e.toString());
		}
		catch( TikaException e) {
			throw new SearchException(e.toString());
		}
		catch( SAXException e) {
			throw new SearchException(e.toString());
		}
		finally {
			if( htmlInput != null ) {
				htmlInput.close();
			}
		}
		return extractedContent;
	}


	protected Map<String, String> getLinks(Parser parser, 
											DataInputStream htmlInput) throws IOException {
		if( htmlInput == null ) {
			throw new NullPointerException("Cannot get links from undefined URI");
		}
		
		Map<String, String> linksMap = new HashMap<String, String>();
		LinkContentHandler contentHandler = new LinkContentHandler();
		
		try {
			parser.parse(htmlInput, contentHandler, metadata, context);
			List<Link> linksList = contentHandler.getLinks();
			if( linksList != null & linksList.size() > 0) {
				for(Link curLink : linksList) {
					if( !curLink.isImage()) {
						linksMap.put(curLink.getUri(), curLink.getTitle());
					}
				}
			}
		}
		catch( MalformedURLException e) {
			CLogger.error(e.toString());
		}
		catch( TikaException e) {
			CLogger.error(e.toString());
		}
		catch( SAXException e) {
			CLogger.error(e.toString());
		}

		return linksMap;
	} 
	
}


// ----------------------------------------  EOF ----------------------------------------
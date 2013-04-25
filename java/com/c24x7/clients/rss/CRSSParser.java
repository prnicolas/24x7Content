// Copyright (c) 2010-2011 Patrick Nicolas
package com.c24x7.clients.rss;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.namespace.QName;



		/**
		 * <p>Implement functionality to parse RSS feeds. The client code is 
		 * responsible for handling relevant exceptions.</p>
		 * @author Patrick Nicolas
		 */

public final class CRSSParser {
	public static final String ITEM = "item";
	
	private static Map<String, String> attrMap = null;
	static {
		attrMap = new HashMap<String, String>();
		attrMap.put(CRSSItem.TITLE, null);
		attrMap.put(CRSSItem.DESCRIPTION, null);
		attrMap.put(CRSSFeed.LANGUAGE, null);
		attrMap.put(CRSSMessage.GUID, null);
		attrMap.put(CRSSMessage.ORIG_LINK, null);
		attrMap.put(CRSSMessage.AUTHOR, null);
		attrMap.put(CRSSFeed.COPYRIGHT, null);
	}
	
	private URL 			_url = null;
	private XMLEventReader 	_eventReader = null;


		/**
		 * <p>Create a RSS parser for a specific URL</p>
		 * @param url source of the feed
		 * @throws MalformedURLException if URL is not valid
		 */
	public CRSSParser(final String url) throws MalformedURLException {
		_url = new URL(url);
	}

	
		/**
		 * <p>Parse a RSS XML feed stream</p>
		 * @return the RSS feed container
		 * @throws XMLStreamException if elements cannot be extracted
		 * @throws IOException if XML stream cannot be opened
		 */
	public CRSSFeed parse() throws XMLStreamException, IOException {
		CRSSFeed feed = null;
		
		String category = null;

			// First create a new XMLInputFactory and setup the event reader
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		_eventReader = inputFactory.createXMLEventReader(_url.openStream());
			
			// Read the XML document
		while (_eventReader.hasNext()) {
				
			XMLEvent event = _eventReader.nextEvent();	
				
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();	
				QName name = startElement.getName();
				category = name.getLocalPart();
					
				/*
				 * Create the feed container for further initialization
				 */
				if (category == ITEM) {
					if( feed == null) {
						feed = new CRSSFeed(attrMap);
					}
					event = _eventReader.nextEvent();
				}
				else {
					extractAttribute(category); 
				}
			} 
				
			/*
			 * If this is the last element, then extract the attributes
			 * of the feed container
			 */
			else if (event.isEndElement()) {
				if (event.asEndElement().getName().getLocalPart() == ITEM) {
					feed.getMessages().add(new CRSSMessage(attrMap));
				}
			}
		}
	
		return feed;
	}
	
	
	private void extractAttribute(final String category) throws XMLStreamException {
		if( attrMap.containsKey(category) ) {
			attrMap.put(category, _eventReader.nextEvent().asCharacters().getData());
		}
	}

}

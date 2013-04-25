// Copyright (c) 2010 Patrick Nicolas
package com.c24x7.clients.twitter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CIntMap;



/**
 * <p>
 * Implements the mapping between specific format of social networks
 * logs and the internal fields of a social message.
 * The set method implements the mapping and return a social message
 * object fully initialized.
 * </p>
 * @author Patrick Nicolas
 */
public final class CTwitterFields {
	public static enum SOCIAL_MESSAGE_SCOPE {
		LINKED_DOCUMENT_ONLY, ALL
	}
		/**
		 * Pattern used to extract date from Twitter messages
		 */
	public static final String DATE_PATTERN = "EEE MMM dd hh:mm:ss z yyyy";
	
	public static final String SOCIALNET_NAME = "Twitter";
	
	public static final long HTTP_TIME_OUT	= 2500L;
	public static final int SOURCE_INDEX 	= 0;
	public static final int MSG_ID_INDEX 	= 1;
	public static final int DATE_INDEX 		= 2;
	public static final int AUTHOR_INDEX 	= 3;
	public static final int FIELD_INDEX 	= 4;
	public static final int CONTENT_INDEX 	= 5;
	
	
				/**
				 * <p>
				 * Nested named class that encapsulates the filter
				 * of Twitter messages</p> 
				 * @author Patrick Nicolas
				 */
	public final class NFilter {
		private CIntMap	_filterMap = null;
		private boolean	_active	= false;
		private int		_fieldIndex = -1;
		
		public NFilter(final CIntMap filterMap, int fieldIndex) {
			_filterMap 	= filterMap;
			_fieldIndex = fieldIndex;
		}
		
		public void reset() {
			_active = false;					   
		}
		
		public void set(String field, int index) {
			if(_active == false) {
				_active = ((index == _fieldIndex) && _filterMap.containsKey(field));
			}
		}
		
		public final boolean isActive() {
			return _active;
		}
	}
	
	
	private CTweet 				_message 	= null;
	private NFilter 			_filter 	= null;
	private SOCIAL_MESSAGE_SCOPE _scope 	= SOCIAL_MESSAGE_SCOPE.ALL;
	

	public CTwitterFields() {
		this(SOCIAL_MESSAGE_SCOPE.ALL);
	}
	
	public CTwitterFields(SOCIAL_MESSAGE_SCOPE scope) { 
		_scope = scope;
	}
	
	public CTwitterFields(final CIntMap filterMap, int conditionalField) {
		this(filterMap, conditionalField, SOCIAL_MESSAGE_SCOPE.ALL);
	}
	
	public CTwitterFields(	final CIntMap filterMap, 
							int 		conditionalField, 
							SOCIAL_MESSAGE_SCOPE scope) {
		_filter = new NFilter(filterMap, conditionalField);
		_scope = scope;
	}
	
	
			/**
			 * <p>
			 * Main method that implement the conversion of a array
			 * of string into an object attributes. The completion of the
			 * last field returns the CSocial Message fully initialized.
			 * </p>
			 * @param index index of the array of string variables extracted from social networks
			 * @param fieldValue the string variable to be converted
			 * @return null if the message object has not been completed initialized,
			 * @throws socialnetException
			 */
	public CTweet set(final int    index, 
					  final String fieldValue)  {
		
		CTweet newMessage = null;
		
		try {
			switch( index ) {
				case SOURCE_INDEX:
					setSource();
					break;
				
				case MSG_ID_INDEX:
					setId(fieldValue);
					break;
				
				case DATE_INDEX: 
					setDate(fieldValue);
					break;
				
				case FIELD_INDEX:
					break;
					
				case AUTHOR_INDEX:
					setAuthor(fieldValue);
					break;
				
				case CONTENT_INDEX: 
					setContent(fieldValue);
					newMessage = _message;
			
				default:
					break;
			}
			setFilter(fieldValue, index);
			
		}
		catch( NumberFormatException e) {
			_message = null;
			CLogger.info(e.toString());
		}
		catch( ParseException e) {
			_message = null;
			CLogger.info(e.toString());
		}
		return newMessage;
	}
	
	
	
	


					// ----------------------
					// Private Field Update Method
					// ---------------------------
	
	private void setSource() {
		_message = new CTweet();
		_message.setSource(SOCIALNET_NAME);
		resetFilter();
	}
	
	private void setId(String fieldValue)  {
		if( _message != null ) {
			Long lFieldValue = Long.valueOf(fieldValue);
			_message.setId(lFieldValue.longValue());
		}
	}
	private void setAuthor(String fieldValue) {
		if( _message != null ) {
			_message.setAuthor(fieldValue);
		}
	}
	
	private void setDate(String fieldValue) throws ParseException {
		if( _message != null ) {
			Date date = new SimpleDateFormat(DATE_PATTERN).parse(fieldValue);
			_message.setDate(date);
		}
	}
	
		
	
	private void setContent(final String fieldValue) {
		
		if( _message != null && filter()) {
			String link = extractLink(fieldValue);
			/*
			 * If the link exists...
			 */
			if( link != null ) {	
				_message.setLink(link);
			}
			
					// If we only need to content of a web page
					// extracted using a link embedded into a tweet
					// then no need to setup the message..
			if(_scope == SOCIAL_MESSAGE_SCOPE.ALL ) {
				_message.setContent(fieldValue);
			}
		}
	}
	
	

	
	private static String extractLink(final String content) {
		String link = null;
		int indexLink = content.indexOf("http://");
		
		if( indexLink != -1) {
			link = content.substring(indexLink);
			int indexEndLink = link.indexOf(" ");
			if( indexEndLink != -1) {
				link = link.substring(0, indexEndLink);
			}
			link = link.trim();
		}
		return link;
	}
	
	
					// --------------------------------------
					//  Private conditional filtering methods
					// ---------------------------------------
	
	private void setFilter(String field, int index) {
		if(_filter != null) {
			_filter.set(field, index);
		}
	}
	
	private void resetFilter() {
		if(_filter != null) {
			_filter.reset();
		}
	}
	
	private final boolean filter() {
		return (_filter == null) || (_filter.isActive());
	}

}

// ----------------- EOF ---------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c24x7.util.logs.CLogger;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;



			/**
			 * <p>Generic class for Yahoo search engine results using BOSS</p>
			 * @author Patrick Nicolas
			 * @date 08/01/2011
			 * @see http://developer.yahoo.com/search/boss
			 */
public final class CWebSearch  {
	protected final static int 	DEFAULT_LIMIT = 25;
	protected final static String YAHOO_SEARCH_URL = "http://yboss.yahooapis.com/ysearch/web"; 
	protected static String consumerKey = "dj0yJmk9WlZNdkVLYTNmMFgyJmQ9WVdrOU5YWTFiemhRTXpBbWNHbzlNelUxTVRVNE9UWXkmcz1jb25zdW1lcnNlY3JldCZ4PTUz";
	protected static String consumerSecret = "3bdf2bd900f51a3902c442505bd72db9409c8c24";  
	
	protected class NHttpRequest {
		protected int  			_httpErrorCode = -1;
	    protected OAuthConsumer _consumer = null;  
	    
	    protected NHttpRequest() { 
	    	_consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);  
	    }  
	     
	    
	    protected HttpURLConnection getConnection(final String urlStr)  throws IOException { 
		    
	    	HttpURLConnection uc = null;
		               
	    	if (_consumer != null) {  
	    		try {   
	    			String signedUrlStr = _consumer.sign(urlStr);
	    			URL signedUrl = new URL(signedUrlStr);
	    			uc  = (HttpURLConnection)signedUrl.openConnection();  
	    			uc.connect();  
	    		} 
	    		catch (OAuthMessageSignerException e) {  
	    			throw new IOException("OAUTH signing error " + urlStr);  
	  	       	} 
	    		catch (OAuthExpectationFailedException e) {  
	    			throw new IOException("OAUTH expectation error " + urlStr);  
	    		}
	    		catch (OAuthCommunicationException e) {  
	    			throw new IOException("OAUTH communication error " + urlStr);  
	    		}      		
	    	}  
	    	return uc;  
	    } 
	    
	      
	    /** 
	     * Sends an HTTP GET request to a url 
	     * 
	     * @param url the url 
	     * @return - HTTP response code 
	     */  
	    protected String sendGetRequest(final String url) throws IOException {  	   
	    	String responseString = null;
	        _httpErrorCode = 500;
	        
	        try {  
	            HttpURLConnection uc = getConnection(url);  
	            if( uc != null ) {
	            	_httpErrorCode = uc.getResponseCode();  
	              
	            	if(200 == _httpErrorCode || 401 == _httpErrorCode || 404 == _httpErrorCode){  
	            		BufferedReader rd = new BufferedReader(new InputStreamReader(_httpErrorCode==200?uc.getInputStream():uc.getErrorStream()));  
	            		StringBuilder sb = new StringBuilder();  
	            		String line;  
	            		while ((line = rd.readLine()) != null) {  
	            			sb.append(line);  
	            		}  
	            		rd.close();  
	            		responseString = sb.toString(); 
	            	} 
	            	else {
	            		CLogger.error("Unauthorized access: " + _httpErrorCode);
	            	}
	            }
	         } 
	        catch (MalformedURLException ex) {  
	            throw new IOException( url + " is not valid");  
	        } 
	        return responseString;  
	    }  
	}
	
		/**
		 * <p>Class that contains the records results by Yahoo search engine.</p>
		 * @author Patrick Nicolas
		 * @date 08/01/2011
		 */
	public class NSearchItem {
		protected String _url = null;
		protected String _summary = null;
		
		public NSearchItem(final JSONObject input) throws JSONException {
			_url = input.getString("url");
			_summary = input.getString("summary");
		}
		
		public final String getUrl() {
			return _url;
		}
		public final String getSummary() {
			return _summary;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_url);
			buf.append("\n");
			buf.append(_summary);
			buf.append("\n");
			
			return buf.toString();
		}
	}
	
	
	protected NHttpRequest 		_httpRequest = null;  
	protected List<NSearchItem> _yahooSearchItems = null;
	protected String			_keyword = null;
	protected int				_start = 0;
	protected int				_count = DEFAULT_LIMIT;

				/**
				 * <p>Create a search configuration client for Yahoo search engine.</p>
				 */
	public CWebSearch(final String keyword) {
		this(keyword, DEFAULT_LIMIT);
	}
	
	public CWebSearch(final String keyword, int limit) { 
		_httpRequest = new NHttpRequest(); 
		_yahooSearchItems = new LinkedList<NSearchItem>();
		_keyword = keyword;
		_count = (limit < DEFAULT_LIMIT) ? limit : DEFAULT_LIMIT;
	}
	
	public void setStart(int start) {
		if( start < _count) {
			_start = start;
		}
	}
	

				/**
				 * <p>Initiate a search for documents related to the search string specified 
				 * in the constructor.</p>
				 */
	public void search() {  
				
		try {
			String searchString = _keyword;
		   
			StringBuilder params = new StringBuilder(YAHOO_SEARCH_URL);  
			params.append("?q=");
			String encString = searchString.replace(" ", "%20");
			params.append(encString);				
			if( _count < DEFAULT_LIMIT) {
				params.append("&count=");
				params.append(_count);
			}
			if(_start > 0) {
				params.append("&start=");
				params.append(_start);
			}
			String response = _httpRequest.sendGetRequest(params.toString()); 
			parseResponse(response);
		}
		catch( IOException e) {
			CLogger.error(e.toString());
		}
		catch(JSONException e) {
			CLogger.error(e.toString());
		}
	}
	
	public final  List<NSearchItem> getYahooSearchItems() {
		return _yahooSearchItems;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("\nSearch results:\n");
		for(NSearchItem item : _yahooSearchItems ) {
			buf.append(item.toString());
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	
					// --------------------------
					// Private Supporting Methods
					// --------------------------
	
	private void parseResponse(final String response) throws JSONException {

		if( response != null && response.length() > 32 ) {
			JSONObject json = new JSONObject(response);
			json = json.getJSONObject("bossresponse");
			String[] names  = JSONObject.getNames(json);
			JSONObject webObj = json.getJSONObject(names[1]);

			JSONArray array = webObj.getJSONArray("results");
			if( array.length() > 0) {
					
				JSONObject cursor = null;
				for( int j = 0; j < array.length(); j++) {
					cursor = array.getJSONObject(j);
					if( cursor != null) {
						_yahooSearchItems.add(new NSearchItem(cursor));
					}
				}
			}
		}
	}

}

// -----------------------  EOF ---------------------------------------

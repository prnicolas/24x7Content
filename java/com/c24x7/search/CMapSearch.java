// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;


import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.c24x7.exception.SearchException;
import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.CEnv;




			/**
			 * <p>Extracts the URL to display static and dynamic Google maps.</p>
			 * @author Patrick Nicolas
			 * @date 04/16/2011
			 */

public final class CMapSearch extends AItemSearch {
	 
	public enum EMapType {
		ROAD, SATELLITE, TERRAIN
	}
	
	public static Map<EMapType, String> mapTypeStr = new HashMap<EMapType, String>();
	static {
		mapTypeStr.put(EMapType.ROAD, "roadmap");
		mapTypeStr.put(EMapType.SATELLITE, "satellite");
		mapTypeStr.put(EMapType.TERRAIN, "terrain");
	}

	public final static int[]	MAP_SIZE = new int[] { 420, 360 };
	protected final static int    MAP_DISPLAY_WIDTH = 180;
	protected static final String GOOGLE_GEOCODE_URL 	 = "http://maps.googleapis.com/maps/api/geocode/json?address=";
	protected static final String GOOGLE_STATICMAP_URL = "http://maps.google.com/maps/api/staticmap?center=";
	protected final static String[] MARKER_COLOR = {
		"red", "blue", "green", "yellow", "white"
	};
	
				/**
				 * <p>Inner static class that encapsulates the extraction and generation
				 * of coordinates from an address
				 * @author Patrick Nicolas
				 * @date 04/16/2011
				 */
	public static class NCoordinates {
		protected final static String JSON_RESULTS 	= "results";
		protected final static String JSON_GEOMETRY 	= "geometry";
		protected final static String JSON_LOCATION 	= "location";
		protected final static String JSON_LAT 		= "lat";
		protected final static String JSON_LNG 		= "lng";
		
		
				/**
				 * <p>Extract the coordinates (latitude & longitude) from an address.</p>
				 * @param address physical address
				 * @return coordinates using the format "latitude,longitude" 
				 * @throws JSONException if the request or response was poorly formatted
				 * @throws IOException if connectivity to Google GeoCode service is lost
				 */
		public static String getCoordStr(final String address) throws JSONException, IOException {
			String[] coordinates = convert(address);
			StringBuilder buf = new StringBuilder();
			buf.append( coordinates[0]);
			buf.append(CEnv.FIELD_DELIM);
			buf.append( coordinates[1]);
			
			return buf.toString();
		}
		
				
		protected static String[] convert(final String address) throws JSONException, IOException {
			
			try {
				String convertedAddress = CMapSearch.convertAddress(address);
				StringBuilder buf = new StringBuilder(GOOGLE_GEOCODE_URL);
				buf.append(convertedAddress);
				buf.append("&sensor=false");
				
				URL url = new URL(buf.toString());
				URLConnection connection = url.openConnection();
				String line = null;
				StringBuilder builder = new StringBuilder();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}

				return extractCoordinates(builder.toString());
			}
				
			catch( MalformedURLException e) {
				throw new IOException("Map address: " + address + " error " + e.toString());
			}
		}

		
		protected static String[] extractCoordinates(final String jsonStream) throws JSONException {
			String[] coordinates = new String[2];
			
			JSONObject json = new JSONObject(jsonStream);
			JSONArray jsonArray = json.getJSONArray(JSON_RESULTS);			
			JSONObject jsonObj = null;
			
			for( int j = 0; j < jsonArray.length(); j++) {
				jsonObj = jsonArray.getJSONObject(j);
				
				if( jsonObj != null ) {
					String geoProp = jsonObj.getString(JSON_GEOMETRY);
					JSONObject jsonGeometry = new JSONObject(geoProp);
					String geoLoc = jsonGeometry.getString(JSON_LOCATION);
					JSONObject jsonLoc = new JSONObject(geoLoc);
					coordinates[0] = jsonLoc.getString(JSON_LAT);
					coordinates[1]= jsonLoc.getString(JSON_LNG);
					
					if( coordinates[0] == null || coordinates[1] == null ) {
						throw new JSONException("Could not extract coordinates: " + jsonStream);
					}
				}
			}
			return coordinates;
		}
	}
		

	protected String 				_sizeStr = null;
	protected Map<String, String> _markersCoord = null;
	protected String 		_mapUrl = null;
	protected String		_keyword = null;
	
	
	public CMapSearch() { }
	
	public CMapSearch(final String keyword) {
		this(keyword, String.valueOf(MAP_SIZE[0]), String.valueOf(MAP_SIZE[1]));
	}
	
	public CMapSearch(final String keyword,final String width, final String height) {
		_keyword = keyword;

		StringBuilder buf = new StringBuilder();
		buf.append(width);
		buf.append("x");
		buf.append(height);
		_sizeStr = buf.toString();
	}
	

	public void getItems(CSummaryModel model) {
		CContentItemsMap searchResultsMap = model.getContentItemsMap();
		searchResultsMap.add(_keyword, searchResultsMap.new NMaps(_mapUrl));
	}

	
				/**
				 * <p>Add a marker address on an existing map</p>
				 * @param key  Label for the marker
				 * @param markerAddress Physical address for the marker
				 * @throws SearchException
				 */
	public void addMarker(	final String key, 
							final String markerAddress) throws SearchException {
		if( markerAddress != null && markerAddress.length() > 5) {
			if( _markersCoord == null) {
				_markersCoord = new HashMap<String, String>();
			}
		
			try {
				_markersCoord.put(key, CMapSearch.NCoordinates.getCoordStr(markerAddress));
			}
			catch(IOException e) {
				throw new SearchException(e.toString());
			}
			catch( JSONException e) {
				throw new SearchException(e.toString());
			}
		}
	}
	
				/**
				 * <p>Extract the coordinates in string format "latitude,longitude" from a physical address.</p>
				 * @param address Physical address used for the location
				 * @return Coordinate using "latitude,longitude" format
				 * @throws SearchException
				 */
	public String getCoordStr(final String address) throws IOException {
		
		try {
			return NCoordinates.getCoordStr(address);
		}
		catch( JSONException e) {
			throw new IOException(e.toString());
		}
	}
	

	public static String[] getSize() {
		int nHeight =  (int)(MAP_SIZE[1] * MAP_DISPLAY_WIDTH/(double)MAP_SIZE[0]);		
		return new String[] { String.valueOf(MAP_DISPLAY_WIDTH), String.valueOf(nHeight) };
	}
	
	
	
	
	protected void search() throws SearchException {
		String convertedAddress = convertAddress(_keyword);
		_mapUrl = getUrlStaticMap(8, convertedAddress, mapTypeStr.get(EMapType.ROAD));
	}

	
	
	protected static String convertAddress(final String address) {
		String convertedAddress = null;
		
		if( address != null && address.length() > 5) {
		
			String[] addressElements = address.split(" ");
			if(addressElements == null) {
				throw new NullPointerException("Improper location address");
			}
		
			StringBuilder buf = new StringBuilder();
			int k = 0;
	
			for( String element : addressElements) {
				buf.append(element);
				if( ++k < addressElements.length ) {
					buf.append("+");
				}
				convertedAddress = buf.toString();
			}
		}
		
		return convertedAddress;
	}
	
	
	

				/**
				 * <p>Generate the URL of a static map using the following format:<br>
				 *  maps.google.com/maps/api/staticmap?center='address'<br>
				 *  &size='widthxheight'&markers=color:'color'%7CLabel:'Label'%7C'latitude,longitude'&sensor=false<br>
				 *  Example:<br>
				 *  http://maps.google.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=14&size=512x512&maptype=roadmap&markers=color:blue%7Clabel:S%7C40.702147,-74.015794&markers=color:green%7Clabel:G%7C40.711614,-74.012318&markers=color:red%7Ccolor:red%7Clabel:C%7C40.718217,-73.998284&sensor=false</p>
				 * @param zoom Zoom level for the map
				 * @return URL for the static map
				 * @throws SearchException
				 */
	protected String getUrlStaticMap(	final int zoom, 
									final String convertedAddress, 
									final String mapTypeStr) throws SearchException {
			
		String urlMap = null;
		
		try {
			StringBuilder buf = new StringBuilder(GOOGLE_STATICMAP_URL);
			buf.append(convertedAddress);
			buf.append("&zoom=");
			buf.append(String.valueOf(zoom));
			buf.append("&size=");
			buf.append(_sizeStr);
			buf.append("&maptype=");
			buf.append(mapTypeStr);
			buf.append("&markers=color:red%7Clabel:C%7C");
			buf.append(NCoordinates.getCoordStr(convertedAddress));
			int k = 1;
		
			if( _markersCoord != null ) {
				for(String key : _markersCoord.keySet()) {
					if( _markersCoord.containsKey(key)) {
						buf.append("&markers=color:");
						buf.append(MARKER_COLOR[k++%MARKER_COLOR.length]);
						buf.append("%7Clabel:");
						buf.append(key);
						buf.append("%7C");
						buf.append(_markersCoord.get(key));
					}
				}
			}
			buf.append("&sensor=false");
		
			urlMap = buf.toString();
		}
		catch( IOException e) {
			throw new SearchException(e.toString());
		}
		catch( JSONException e) {
			throw new SearchException(e.toString());
		}
		
		return urlMap;
	}
	
	
			/**
			 * <p>Generate a Javascript V3 based dynamic map for a physical location.</p>
			 * @param zoom  zoom level for the map.
			 * @return
			 * @throws SearchException
			 */
	protected String getUrlDynamicMap(final int zoom) throws SearchException {
		return null;
	}
}

// ----------------------------- EOF -------------------------
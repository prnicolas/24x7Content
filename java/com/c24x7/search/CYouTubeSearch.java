// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;

import com.c24x7.exception.SearchException;
import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.CProfile;
import com.c24x7.util.logs.CLogger;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


			/**
			 * <p>Class that control search for video relevant to a content</p>
			 * @author Patrick Nicolas
			 * @date 04/11/2011
			 */
public final class CYouTubeSearch extends AItemSearch {
	  public static final int   MAX_NUM_VIDEO_ENTRIES = 10;
		  /**
	   * The name of the server hosting the YouTube GDATA feeds
	   */
	  public static final String YOU_TUBE_SERVICE = "24x7 Content";

	  public static final String YOUTUBE_GDATA_SERVER 	= "http://gdata.youtube.com";
	  public static final String STANDARD_FEED_PREFIX 	= YOUTUBE_GDATA_SERVER + "/feeds/api/standardfeeds/";
	  public static final String WATCH_ON_MOBILE_FEED 	= STANDARD_FEED_PREFIX + "watch_on_mobile";
	  public static final String VIDEOS_FEED 			= YOUTUBE_GDATA_SERVER  + "/feeds/api/videos";
	  public static final String USER_FEED_PREFIX 		= YOUTUBE_GDATA_SERVER + "/feeds/api/users/";
	  public static final String UPLOADS_FEED_SUFFIX 	= "/uploads";
	  public static final String FAVORITES_FEED_SUFFIX 	= "/favorites";

	  public static class NMediaContent {
		  protected String _type 		= null;
		  protected String _url 		= null;
		  protected int 	 _duration 	= -1;
		  
		  public NMediaContent(final String type, final String url, final int duration) {
			  _type = type;
			  _url = url;
			  _duration = duration;
		  }
		  
		  public final int getDuration() {
			  return _duration;
		  }
		  
		  public final String getUrl() {
			  return _url;
		  }
		  
		  public final String getType() {
			  return _type;
		  }
		  
		  public String toString() {
			  StringBuilder buf = new StringBuilder();
			  buf.append("\nType=");
			  buf.append(_type);
			  buf.append("  Url=");
			  buf.append(_url);
			  buf.append("  Duration=");
			  buf.append(_duration);
			  
			  return buf.toString();
		  }
	  }
	  
	  public static class NVideoEntry {
		  public final static String THUMBNAIL_URL = "http://i.ytimg.com/vi/";
		  public final static String THUMBNAIL_TYPE = "/default.jpg";
		  public final static String FLASH_URL = "http://www.youtube.com/v/";
		  public final static String FLASH_SETUP = "?f=videos&c=24x7+Content&app=youtube_gdata";
		  protected String 			_title 			= null;
		  protected String 			_videoId		= null;
		//  protected String 		  	_mediaPlayerUrl = null;
	//	  protected String			_thumbnail		= null;
		//  protected NMediaContent[] 	_mediaContent 	= null;
		 
		  public NVideoEntry(final VideoEntry videoEntry) {
			  if( videoEntry == null ) {
				  throw new IllegalArgumentException("Video Entry undefined!");
			  }
			  
			  if(videoEntry.getTitle() != null) {
				  _title = videoEntry.getTitle().getPlainText();
			  }
		    
			  YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
			  
			  if(mediaGroup != null) {
				  _videoId = mediaGroup.getVideoId();
				
				  /*		  
				  MediaPlayer mediaPlayer = mediaGroup.getPlayer();
				  if( mediaPlayer != null) {
					  _mediaPlayerUrl = mediaPlayer.getUrl();
				  }
				  
				  int numContent = mediaGroup.getYouTubeContents().size(), count = 0;
				  
				  if( numContent > 0 ) {
					  _mediaContent = new CYouTubeSearch.NMediaContent[numContent];
					  for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
						  
						  _mediaContent[count++] = new NMediaContent(mediaContent.getType(), mediaContent.getUrl(),  mediaContent.getDuration());
					  }
					  
				  }
				  */
			  } 
		  }
		  
		  public final String getTitle() {
			  return _title;
		  }
		  
		  public final String getVideoId() {
			  return _videoId;
		  }
		  
		  public final String getFlashUrl() {
			  StringBuilder buf = new StringBuilder(FLASH_URL);
			  buf.append(_videoId);
			  buf.append(FLASH_SETUP);
			  return buf.toString();
		  }
		
		  
		  public final String getThumbnail() {
			  StringBuilder buf = new StringBuilder(THUMBNAIL_URL);
			  buf.append(_videoId);
			  buf.append(THUMBNAIL_TYPE);
			  return buf.toString();
		  }
		  
		  public String toString() {
			  StringBuilder buf = new StringBuilder("\nVideo Entry: ");
			  buf.append(_title);
			  buf.append(" id: ");
			  buf.append(_videoId);
			  
			  return buf.toString();
		  }
	   }
	  
	  
	  protected YouTubeService service = new YouTubeService(YOU_TUBE_SERVICE);
	  protected NVideoEntry[]  _videosEntries = null;
	  protected String 	     _keyword = null;
	  
	  

		/**
		 * <p>Create a proxy object to the YouTube service to extract a list of video content from 
		 * a list of search words. For this constructor, the search keywords are ordered by their
		 * frequency.</p>
		 * @param serviceName name of the YouTube service
		 * @param searchWordsMap <Search words, frequency> hash map for the keywords.
		 */
	  public CYouTubeSearch(final String keyword) {	
		  _keyword = keyword;
	  }
	  
	
	  		/**
	  		 * <p>Update the content model with videos entries..</p>
	  		 * @param model model for the content to update.
	  		 */
	  public void getItems(CSummaryModel model) {
		  CContentItemsMap searchResultsMap = model.getContentItemsMap();
		  searchResultsMap.add(_keyword, searchResultsMap.new NVideos(_videosEntries));
	  }
	  
	  public String toString() {
		  StringBuilder buf = new StringBuilder("\nVideos for ");
		  buf.append(_keyword);
		  for( NVideoEntry entry : _videosEntries) {
				buf.append("\n");
				buf.append(entry.toString());
		  }
		  
		  return buf.toString();
	  }
	  
	  /**
	   * <p> Searches the VIDEOS_FEED for search terms and print each resulting
	   * VideoEntry. </p>
	   * @param service a YouTubeService object.
	   * @throws ServiceException
	   *                     If the service is unable to handle the request.
	   * @throws IOException error sending request or reading the feed.
	   */
	  
	  /*
	  public void search(final String searchTerms) throws IOException, SearchException {
		  
		  try {
			  YouTubeQuery query = new YouTubeQuery(new URL(VIDEOS_FEED));
			  query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
			  query.setSafeSearch(YouTubeQuery.SafeSearch.STRICT);    
			  query.setFullTextQuery(URLEncoder.encode(searchTerms, "UTF-8"));
			  query.setMaxResults(MAX_NUM_VIDEO_ENTRIES);
			  
			  getVideoEntries(query);
		  }
		  
		  catch( ServiceException e) {
			  throw new SearchException(e.toString());
		  }
	  }
	  */
	  
	  
	  						// --------------------------
	  						// Private Supporting Methods
	  						// --------------------------
	  
	  protected void search() throws SearchException {
		  CProfile.getInstance().time("Videos search (S): ");
		  String searchTerms = _keyword;

		  try {
			  YouTubeQuery query = new YouTubeQuery(new URL(VIDEOS_FEED));
			  query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
			  query.setSafeSearch(YouTubeQuery.SafeSearch.STRICT);    
			  query.setFullTextQuery(URLEncoder.encode(searchTerms, "UTF-8"));
			  query.setMaxResults(MAX_NUM_VIDEO_ENTRIES);
			  
			  getVideoEntries(query);
			  CProfile.getInstance().time("Video search (E): ");
		  }
		  catch( IOException e) {
			  CLogger.error(e.toString());
		  }
		  catch( ServiceException e) {
			  CLogger.error(e.toString());
		  }
	  }
	  
	  
	  
	  /**
	   * Searches the VIDEOS_FEED for category keywords and prints each resulting
	   * VideoEntry.
	   *
	   * @param service a YouTubeService object.
	   * @throws ServiceException
	   *                     If the service is unable to handle the request.
	   * @throws IOException error sending request or reading the feed.
	   */
	  /*
	  public NVideoEntry[] search(final String[] keywords) throws IOException, SearchException {
		   Map<String, NVideoEntry> videoEntries = null;
		  
		  try {
			  YouTubeQuery query = new YouTubeQuery(new URL(VIDEOS_FEED));
			  query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
			  query.setSafeSearch(YouTubeQuery.SafeSearch.STRICT);
		//	  query.setFullTextQuery(URLEncoder.encode(searchTerms, "UTF-8"));
			  
			  Query.CategoryFilter categoryFilter = new Query.CategoryFilter();
			  Category category = null;
	    
			  for(String keywordTerm : keywords) {
				  category = new Category(YouTubeNamespace.KEYWORD_SCHEME, keywordTerm);
				  categoryFilter.addCategory(category);
			  }
	
			  query.addCategoryFilter(categoryFilter);
			  videoEntries = getVideoEntries(query);
		  }
		  catch( ServiceException e) {
			  throw new SearchException(e.toString());
		  }
		  
		  return videoEntries;
	  }
	  */
	  

	  
	  
	  					// -----------------
	  					//  Private Methods
	  					// ------------------
	  
	  
	  protected void getVideoEntries(YouTubeQuery query) throws ServiceException, IOException {

		  VideoFeed videoFeed = service.query(query, VideoFeed.class);
		  List<VideoEntry> videoList = videoFeed.getEntries();
		  
		  if( videoList != null ) {
			  _videosEntries = new  NVideoEntry[videoList.size()];
			  int k = 0;
			  for(VideoEntry video :  videoList) {
				  _videosEntries[k++] = new NVideoEntry(video);
			  }
		  } 
	  }
}

// ---------------------   EOF ------------------------------------
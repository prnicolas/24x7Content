// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.c24x7.output.html.CHTMLCollectionWriter;
import com.c24x7.output.html.CHTMLDescriptionWriter;
import com.c24x7.output.html.CHTMLKeywordWriter;
import com.c24x7.semantics.dbpedia.CDbpediaResources.NSummaryLabel;
import com.c24x7.search.CImagesSearch.NImageEntry;
import com.c24x7.search.CWikipediaSearch.NWikipediaEntry;
import com.c24x7.search.CWebSearch.NSearchItem;
import com.c24x7.search.CYouTubeSearch.NVideoEntry;
import com.c24x7.util.CIntMap;
import com.c24x7.util.comm.CConnectionTest;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;





				/**
				 * <p>Class to generate multi-media content such as images, videos, thesaurus...</p>
				 * @author Patrick Nicolas
				 * @date 05/25/2011
				 * @see com.c24x7.models.IContentItemsMap
				 */
public class CContentItemsMap extends HashMap<String, CContentItemsMap.NContentItem>  {

	protected static final long serialVersionUID = 2182124158181661596L;

			/**
			 * <p>Base class for all the elements of a content item which represents
			 * a syntactic and semantic structured information about a specific part of speech.</p>
			 * @author Patrick Nicolas
			 * @date 06/25/2011
			 */
	public abstract class NIElement {
		abstract public Object get();
		abstract public void toHtml(CHTMLDescriptionWriter htmlWriter);
		
		/**
		 * <p>Generate a textual representation for this NIElement</p>
		 * @param buf  StringBuilder buffer that collect the values of attributes
		 * @return true if at least one attribute is not null, false otherwise
		 */
		abstract protected boolean print(StringBuilder buf);
		
		abstract public void add(NContentItem contentItem);
		
				/**
				 * <p>Generate a textual representation for this NIElement</p>
				 * @return character string representation of the attributes of this element
				 */
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if( !print(buf) ) {
				buf.append("NULL");
			}
			return buf.toString();
		}
	}
	
		/**
		 * <p>Class to manage the extended abstract from DBPedia or Wikipedia entry.</p>
		 * @author Patrick Nicolas
		 * @date 07/14/2011
		 */
	public class NDbpediaRecord extends NIElement {
		protected NSummaryLabel _dbpediaRecord = null;
		
		public NDbpediaRecord(final NSummaryLabel dbpediaRecord) {
			_dbpediaRecord = dbpediaRecord;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _dbpediaRecord != null ) {
				contentItem.set(this);
			}
		}
		
		@Override
		public Object get() {
			return _dbpediaRecord;
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( _dbpediaRecord != null ) {
				htmlWriter.write(this);
			}
		}
		
		/**
		* <p>Generate a textual representation for this NIElement</p>
		* @param buf  StringBuilder buffer that collect the values of attributes
		* @return true if at least one attribute is not null, false otherwise
		*/
		@Override
		protected boolean print(StringBuilder buf) {
			if(_dbpediaRecord != null ) {	
				buf.append("\ndbpedia Entry:\n");
				buf.append(_dbpediaRecord.toString());
			}
			return (_dbpediaRecord != null );
		}
	}

			/**
			 * <p>Define an entry for a named entity as declared in Freebase. An named 
			 * entity has a name, article, image, type and multiple domains.</p>
			 * @author Patrick Nicolas
			 * @date 06/23/2011
			 */
	public static class NEntityEntry {
		
		public final static int MAX_TYPE_DOMAIN_ENTRY  = 3;
		public final static int NUM_SENTENCES_ABSTRACT = 2;
		
		protected final static Map<String, Object> typeExclusion = new HashMap<String, Object>();
		static {
			typeExclusion.put("base", null);
			typeExclusion.put("common", null);
			typeExclusion.put("freebase", null);
			typeExclusion.put("ontologies", null);
			typeExclusion.put("ontology_instance", null);
			typeExclusion.put("tagit", null);
			typeExclusion.put("topic", null);
			typeExclusion.put("type", null);
			typeExclusion.put("type_profile", null);
			typeExclusion.put("namespace", null);
			typeExclusion.put("random", null);
		}
		protected final static String[] attributes = { 
		"article", "image"
		};
		
		protected String[] _attributes = null;
		protected String   _article = null;
		protected String   _abstract = null;
		protected CIntMap  _types  = new CIntMap();
		protected CIntMap  _subTypes = new CIntMap();
		
		public NEntityEntry(final String article, final String image) {
			if( article == null || image == null ) {
				throw new IllegalArgumentException("Entity Entry has no article or image");
			}
			_attributes = new String[attributes.length];
			_attributes[0] = article;
			_attributes[1] = image;
		}
		
		
		public NEntityEntry(final String[] attributes) {
			if( attributes == null ) {
				throw new IllegalArgumentException("Entity Entry has no article or image");
			}
			_attributes = attributes;
		}	
		
		/**
		* <p>Add a type domain from a type id string extracted from Freebase schema
		* @param typeId 
		*/
		public void extractTypes(final String typeId, int rank) {
			String[] classes = typeId.trim().split("/");	
			String type = null;
			
			if( classes != null ) {
				if(classes[1] != null) {
					type = classes[1].trim();
					if(type.length() > 2 && !typeExclusion.containsKey(classes[1])) {
						_types.put(classes[1], rank);
					}
				}
				
				if( classes.length > 1) {
					for( String subtype : classes) {
						if( subtype != null) {
							subtype = subtype.trim();
						
							if( subtype.length() > 2 && !typeExclusion.containsKey(subtype)) {
								_subTypes.put(subtype, rank);
							}
						}
					}
				}
			}
		}
		
		
		/**
		* <p>Retrieve the abstract of an article from Freebase reference link. The content
		* of the article is cleaned up from phonetics, HTML tags, code and other undesirable characters.</p>
		* TO-DO: The first sentence can be replaced by the best sentence of the article.</p>
		* @return first sentence and content of article
		*/
		public final String getAbstract() {
			extractArticle();
			return _abstract;
		}
		
		/**
		* <p>Retrieve the content of an article from Freebase reference link. The content
		* of the article is cleaned up from phonetics, HTML tags, code and other undesirable characters.</p>
		* TO-DO: The first sentence can be replaced by the best sentence of the article.</p>
		* @return first sentence and content of article
		*/
		public final String getArticle() {
			extractArticle();
			return _article;
		}
		
		public final CIntMap getTypes() {
			return _types;
		}
		
		public final CIntMap getSubTypes() {
			return _subTypes;
		}
		
			/**
			 * <p>Retrieve the content of an article from Freebase reference link. The content
			 * of the article is cleaned up from phonetics, HTML tags, code and other undesirable characters.</p>
			 * TO-DO: The first sentence can be replaced by the best sentence of the article.</p>
			 * @return first sentence and content of article
			 */
		protected void extractArticle() {
			if( _attributes[0] != null && (_abstract == null || _article == null) ) {
				try {
					String article = CConnectionTest.get(_attributes[0]);
						/*
						 * Remove the HTML p tag and extract the first sentences
						 */
					if( (article != null && article.length() > 6) ) {
						
						/*
						 * Extract the ASCII version of the text.
						 */
						article = CStringUtil.removeHtmlTags(article);
						_article = CStringUtil.removePhonetic(article);
						
						/*
						 * Extract the different sentences from the article...
						 */
						/*
						CNLPAnalyzer sentenceAnalyzer = new CNLPAnalyzer();
						String[] sentences  = sentenceAnalyzer.extractSentences(article);
						if( sentences != null ) {
							StringBuilder buf = new StringBuilder();
							int lastSentence = sentences.length -1;
							for( int k = 0; k < lastSentence; k++) {
								buf.append(sentences[k]);
								if( k == NUM_SENTENCES_ABSTRACT) {
									_abstract = buf.toString();
								}
							}
							_article = buf.toString();
							if( _abstract == null) {
								_abstract = buf.toString();
							}
						}
						else {
							_article = article;
						}
					*/
					}
				}
				catch(IOException e) {
					CLogger.error(e.toString());
				}
			}
		}
		
		
		public final String getImage() {
		return _attributes[1];
		}
				
		
		@Override
		public String toString() {
		StringBuilder buf = new StringBuilder();
		for( int j = 0; j < attributes.length; j++) {
			buf.append(attributes[j]);
			buf.append(": ");
			buf.append(_attributes[j]);
			buf.append("  \n");
		}
		
		return buf.toString();	
		}
		}

	
					/**
					 * <p>Encapsulates the different named entities structure.</p>
					 * @author Patrick Nicolas
					 * @date 07/04/2011
					 */
	public class NEntityEntries extends NIElement {
		protected NEntityEntry[] 	_entityEntries = null;
		protected Set<String>		_types = null;
		
		public NEntityEntries(final NEntityEntriesInfo entityEntriesList) {
			if( entityEntriesList != null ) {
				_entityEntries = entityEntriesList.getEntityEntries();
		//		_types = entityEntriesList.getTypes().rank(100);
			}
		}
			
		@Override
		public void add(NContentItem contentItem) {
			if(_entityEntries != null ) {
				contentItem.set(this);
				String[] imagesEntries = new String[_entityEntries.length];
				for(int k = 0; k < _entityEntries.length; k++ ) {
					imagesEntries[k] = _entityEntries[k].getImage();
				}
				contentItem.set(new NImages(imagesEntries));
			}
		}
		
		public final Set<String> getTypes() {
			return _types;
		}
		
		public final boolean isType(final String type) {
			return _types.contains(type);
		}
		
		@Override
		public Object get() {
			return _entityEntries;
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if(_entityEntries != null ) {
				htmlWriter.write(this);
			}
		}
				/**
				 * <p>Generate a textual representation for this NIElement</p>
				 * @param buf  StringBuilder buffer that collect the values of attributes
				 * @return true if at least one attribute is not null, false otherwise
				 */
		@Override
		protected boolean print(StringBuilder buf) {
			if(_entityEntries != null ) {	
				buf.append("\n\nEntities:\n");
				for( NEntityEntry entry : _entityEntries) {
					buf.append(entry.toString());
					buf.append("\n");
				}
			}
			return (_entityEntries != null );
		}
	}
	
	public static class NEntityEntriesInfo {

		protected List<NEntityEntry>   _entityEntries = null;
		
		public NEntityEntriesInfo() {
			_entityEntries = new LinkedList<NEntityEntry>();
		}
	
		public void add(final NEntityEntry entityEntry) {
			_entityEntries.add(entityEntry);
		}
		
		public final NEntityEntry[] getEntityEntries() {
			return (_entityEntries.size() > 0 ? _entityEntries.toArray(new NEntityEntry[0]) : null);
		}
		
		public final NEntityEntry getFirstEntityEntry() {
			return (_entityEntries.size() > 0 ? _entityEntries.get(0) : null);
		}
		
		public final List<NEntityEntry> getEntityEntriesList() {
			return _entityEntries;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if( _entityEntries.size() > 0 ) {
				for( NEntityEntry entry : _entityEntries) {
					buf.append(entry);
				}
			}
			
			return buf.toString();
		}
	}


	
	public class NMaps extends NIElement {
		protected String _mapUrl = null;
		
		public NMaps(String mapUrl) {
			_mapUrl = mapUrl;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _mapUrl != null ) {
				contentItem.set(this);
			}
		}
		
		@Override
		public Object get() {
			return _mapUrl;
		}
		
	
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( _mapUrl != null ) {
				htmlWriter.write(this);
			}
		}
		
		public final void toHtmlFragment(CHTMLDescriptionWriter writer) {
			writer.writeMap(_mapUrl);
		}
		

		/**
		 * <p>Generate a textual representation for this NIElement</p>
		 * @param buf  StringBuilder buffer that collect the values of attributes
		 * @return true if at least one attribute is not null, false otherwise
		 */
		@Override
		protected boolean print(StringBuilder buf) {
			if( _mapUrl != null ) {
				buf.append("\nMaps:\n");
				buf.append(_mapUrl);
				buf.append("\n");
			}
			return ( _mapUrl != null );
		}
	}

	
					/**
					 * <p>Images structure that contains the list of images associated 
					 * to a part of a speech or keyword.</p>
					 * @author Patrick Nicolas
					 * @date 07/24/2011
					 */
	public class NImages extends NIElement {
		protected Map<String, NImageEntry> _imageEntriesMap = null;
		protected int _newImagesCursor = 0;
			
		public NImages(final NImageEntry[] imageEntries) {
			_imageEntriesMap = new LinkedHashMap<String, NImageEntry>();
			for( NImageEntry entry : imageEntries) {
				_imageEntriesMap.put(entry.getUrl(), entry);
			}
		}
		
		public NImages(final String[] imagesUrl) {
			_imageEntriesMap = new LinkedHashMap<String, NImageEntry>();
			for( String imageUrl : imagesUrl) {
				_imageEntriesMap.put(imageUrl,  new NImageEntry(imageUrl));
			}
		}
		
		public void add(final NImages images) {
			if(images != null ) {
				Map<String, NImageEntry> otherImageEntriesMap = images.getImageEntriesMap();
				_newImagesCursor = otherImageEntriesMap.size();
				if( _newImagesCursor > 0 ) {
					NImageEntry entry = null;
				
					for( String key : otherImageEntriesMap.keySet() ) {
						if( !_imageEntriesMap.containsKey( key)) {
							entry = otherImageEntriesMap.get(key);
							if( entry != null ) {
								_imageEntriesMap.put(key, otherImageEntriesMap.get(key));
							}
						}
					}
				}
			}				
		}
		
		public final Map<String, NImageEntry> getImageEntriesMap() {
			return _imageEntriesMap;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _imageEntriesMap.size() > 0  ) {
				contentItem.set(this);
			}
		}
		
		@Override
		public Object get() {
			return _imageEntriesMap.values();
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( htmlWriter !=null && _imageEntriesMap.size() > 0  ) {
				htmlWriter.write(this);
			}
		}
		
		public void toHtmlFragment(CHTMLDescriptionWriter writer) {
			if( writer != null && _newImagesCursor > 0 && _newImagesCursor < _imageEntriesMap.size()) {
				writer.writeImages(_imageEntriesMap, _newImagesCursor);
				_newImagesCursor = _imageEntriesMap.size();
			}
		}
		
		@Override
		protected boolean print(StringBuilder buf) {
			if( _imageEntriesMap.size()> 0 ) {
				buf.append("\n\nImages:\n");
				
				for( String imgUrl : _imageEntriesMap.keySet()) {
					buf.append(imgUrl);
					buf.append("\n");
				}
			}
			return (_imageEntriesMap.size()> 0 );
		}
	}
	
	public class NVideos extends NIElement {
		protected static final int MAX_NUM_VIDEOS 	= 5;
		protected NVideoEntry[] _videoEntries 	= null;
		
		public NVideos(final NVideoEntry[] videoEntries) {
			_videoEntries  = ( videoEntries.length > MAX_NUM_VIDEOS ) ?
			 				Arrays.copyOf(videoEntries, MAX_NUM_VIDEOS) :
			 				 videoEntries;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _videoEntries != null ) {
				contentItem.set(this);
			}
		}
		
		@Override
		public Object get() {
			return _videoEntries;
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( _videoEntries != null ) {
				htmlWriter.write(this);
			}
		}
		
		public final void toHtmlFragment(CHTMLDescriptionWriter writer) {
			writer.writeVideos(_videoEntries);
		}
	
		/**
		 * <p>Generate a textual representation for this NIElement</p>
		 * @param buf  StringBuilder buffer that collect the values of attributes
		 * @return true if at least one attribute is not null, false otherwise
		 */
		@Override
		protected boolean print(StringBuilder buf) {
			if( _videoEntries != null ) {
				buf.append("Videos:");
				for( NVideoEntry entry : _videoEntries) {
					buf.append(entry.toString());
					buf.append("\n");
				}
			}
			return ( _videoEntries != null );
		}
	}
	
	
				/**
				 * <p>Wikipedia entry for a keyword or part of speech.</p>
				 * @author Patrick Nicolas
				 * @date 07/21/2011
				 */
	public class NWikipedia extends NIElement {
		protected NWikipediaEntry _wikipediaEntry = null;
			
		public NWikipedia(final NWikipediaEntry wikipediaEntry) {
			_wikipediaEntry = wikipediaEntry;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _wikipediaEntry != null ) {
				contentItem.set(this);
			}
		}
		
		@Override
		public Object get() {
			return _wikipediaEntry;
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( _wikipediaEntry != null ) {
				htmlWriter.write(this);
			}
		}
		
		
		public final void toHtmlFragment(CHTMLDescriptionWriter writer) {
			writer.writeWikipedia(_wikipediaEntry);
		}
		
		/**
		 * <p>Generate a textual representation for this NIElement</p>
		 * @param buf  StringBuilder buffer that collect the values of attributes
		 * @return true if at least one attribute is not null, false otherwise
		 */
		@Override
		protected boolean print(StringBuilder buf) {
			if( _wikipediaEntry != null ) {
				buf.append("\n\nWikipedia:\n");
				buf.append(_wikipediaEntry.toString());
				buf.append("\n");
			}
			return ( _wikipediaEntry != null );
		}
	}
	
	
				/**
				 * <p>Class that manage the web search results..</p>
				 * @author Patrick Nicolas
				 * @date 08/01/2011
				 */
	public class NSearchResults extends NIElement {
		protected NSearchItem[] _searchItems = null;
		
		public NSearchResults(final NSearchItem[] searchItems) {
			_searchItems = searchItems;
		}
		public Object get() {
			return _searchItems;
		}
		
		@Override
		public void toHtml(CHTMLDescriptionWriter htmlWriter) {
			if( _searchItems  != null && _searchItems.length > 0) {
				htmlWriter.write(this);
			}
		}
		
		
		public final void toHtmlFragment(CHTMLDescriptionWriter writer) {
			writer.writeSearchResults(_searchItems);
		}
		
		/**
		 * <p>Generate a textual representation for this NIElement</p>
		 * @param buf  StringBuilder buffer that collect the values of attributes
		 * @return true if at least one attribute is not null, false otherwise
		 */
		@Override
		protected boolean print(StringBuilder buf) {
			boolean found = ( _searchItems  != null && _searchItems.length > 0);
			if( found) {
				for( NSearchItem item : _searchItems) {
					buf.append("\n");
					buf.append(item.toString());
				}
			}
			
			return found;
		}
		
		@Override
		public void add(NContentItem contentItem) {
			if( _searchItems  != null && _searchItems.length > 0) {
				contentItem.set(this);
			}
		}		
	}

	
	/**
	 * <p>Implement the HTML document for a content media item as defined
	 * by the IItem interface.</p>
	 * @author Patrick Nicolas
	 * @date 05/19/2011
	 * @see com.c24x7.models.IItem
	 */
	public class NContentItem  {
		public static final int	NUM_TYPES 	= 2;
		protected boolean			_misspelled		= false;
		protected	NEntityEntries	_entityEntries 	= null;
		protected NDbpediaRecord	_dbpediaEntry 	= null;
		protected NMaps			_maps 			= null;
		protected NImages			_images 		= null;
		protected NWikipedia		_wikipedia 		= null;
		protected NVideos			_videos 		= null;
		protected String			_searchEntry	= null;
		protected NSearchResults	_searchResults 	= null;
		
				/**
				 * <p>Initialize the list of entity entries..</p>
				 * @param entityEntries
				 */
		public void set(NEntityEntries entityEntries) {
			_entityEntries = entityEntries;
		}
		
		public void set(NDbpediaRecord dbpediaEntry) {
			_dbpediaEntry = dbpediaEntry;
		}
		
		public void set(NMaps maps) {
			_maps = maps;
		}
		
		public void set(NWikipedia wikipedia) {
			_wikipedia = wikipedia;
		}
		
		public void set(NImages images) {
			if( _images != null ) {
				_images.add(images);
			}
			else {
				_images = images;
			}
		}
		
		public void set(NVideos videos) {
			_videos = videos;
		}
		
		public void set(NSearchResults searchResults) {
			_searchResults = searchResults;
		}
		
		public final NMaps getMaps() {
			return _maps;
		}
		
		public final NVideos getVideos() {
			return _videos;
		}
		
		public final NImages getImages() {
			return _images;
		}
		
		public final NWikipedia getWikipedia() {
			return _wikipedia;
		}
		
		public final NEntityEntries getEntityEntries() {
			return _entityEntries;
		}
		
		public final NDbpediaRecord getDbpediaEntry() {
			return _dbpediaEntry;
		}
		
		public final NSearchResults getSearchResults() {
			return _searchResults;
		}
		
		public final String toHtmlImageFragment(final String keyword) {
			CHTMLDescriptionWriter writer = new CHTMLDescriptionWriter(keyword);
			_images.toHtmlFragment(writer);
			return writer.toString();
		}
		
		
				/**
				 * <p>Extract media content (images, maps, wikipedia and search results)
				 * for a specific keyword.</p>
				 * @param keyword keyword, term or part of speech used to extract knowledge.
				 * @return HTML document for the media 
				 */
		public final String toHtmlFragment(final String keyword) {
			CHTMLDescriptionWriter writer = new CHTMLDescriptionWriter(keyword);
			if( _maps != null ) {
				_maps.toHtmlFragment(writer);
			}
			if( _wikipedia != null ) {
				_wikipedia.toHtmlFragment(writer);
			}
			if( _videos != null ) {
				_videos.toHtmlFragment(writer);
			}
			if( _searchResults != null) {
				_searchResults.toHtmlFragment(writer);
			}
			
			return writer.toString();
		}
		
				/**
				 * <p>Test if this type is actually supported.</p>
				 * @param type type or class of the named entities.
				 * @return
				 */
		public final boolean isType(final String type) {
			return ( _entityEntries != null) ? _entityEntries.isType(type) : false;
		}
		
		
		public final String getSearchEntry() {
			if( _searchEntry == null ) {
				Set<String> results = null;
			
				if( _entityEntries != null) {
					results = _entityEntries.getTypes();
				}
				if( results != null ) {
					StringBuilder buf = new StringBuilder();
					int k = 0;
					for( String type : results ) {
						buf.append(" ");
						buf.append(type);
						if( k > NUM_TYPES ) {
							break;
						}
					}
					_searchEntry = buf.toString();
				}
			}
			return _searchEntry;
		}
		
		public void setMisspelled() {
			_misspelled = true;
		}
		
		public final boolean isMisspelled() {
			return _misspelled;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if( _misspelled ) {
				buf.append("Misspelled");
			}
			else {
				if( _dbpediaEntry != null ) {
					_dbpediaEntry.print(buf);
				}
				if( _entityEntries != null ) {
					_entityEntries.print(buf);
				}
				if( _maps != null ) {
					_maps.print(buf);
				}
				
				if( _images != null ) {
					_images.print(buf);
				}
				
				if( _wikipedia != null ) {
					_wikipedia.print(buf);
				}
				
				if(_videos != null) {
					_videos.print(buf);
				}
				if(_searchResults != null) {
					_searchResults.print(buf);
				}
			}
			return buf.toString();
		}

		
		/**
		 * <p>Generic interface for generating an HTML document for a keyword, field or
		 * variable and using a defined configuration.</p>
		 * @param htmlWriter HTML writer for this keyword.
		 */
		public void toHtml(CHTMLKeywordWriter htmlWriter) {
			htmlWriter.write(this);
		}
		
		
				/**
				 * <p>Print the different elements of this item/keyword.</p> 
				 * @param htmlWriter html writer for this content item.
				 */
		public void print(CHTMLDescriptionWriter htmlWriter) {
			if( _dbpediaEntry != null ) {	
				_dbpediaEntry.toHtml(htmlWriter);
			}
			if( _entityEntries != null ) {	
				_entityEntries.toHtml(htmlWriter);
			}
			if( _wikipedia != null ) {
				_wikipedia.toHtml(htmlWriter);
			}
			if( _maps != null ) {
				_maps.toHtml(htmlWriter);
			}
			if( _images != null ) {
				_images.toHtml(htmlWriter);
			}
			if( _videos != null ) {
				_videos.toHtml(htmlWriter);
			}
			if( _searchResults != null ) {
				_searchResults.toHtml(htmlWriter);
			}
		}
	}	
		
		
	protected Map<String, String> _searchWordsMap = null;

	
	public void toHtml(CHTMLCollectionWriter htmlWriter) { }
	
	
	
			/**
			 * <p>Create a map for content items (images, Wikipedia entries, videos, Freebase definition,..) for
			 * a list of parts of the speech extracted from an input text.</p>
			 * @param keywordsList list of keywords or part of the speeches extracted from analysis.
			 * @throws SQLException
			 * @throws IOException
			 */

	/*
	public void getDbpedia(List<NMatchingEntry> keywordsList) throws SQLException {
		
		_freebaseFinder = new CFreebase();
		CDbpediaResources dbpediaFinder = new CDbpediaResources();
		
		NContentItem curContent = null;
		NSummaryLabel dbpedia = null;
		String key = null;
			
		
		CDbUtil dbConnection = new CDbUtil();
		Statement stmt = dbConnection.create();
		
		CFreebaseRequest freebaseClient = null;
		for( NMatchingEntry keyword: keywordsList) {
			key = keyword.getKeyword();
			dbpedia = dbpediaFinder.getSummaryLabel(stmt, key, Character.valueOf('0'));
			curContent = add(key, new NDbpediaRecord(dbpedia)); 
			super.put(key, curContent);
			
			freebaseClient = new CFreebaseRequest();
			freebaseClient.setEntity(key);
			_freebaseFinder.add(freebaseClient);
			freebaseClient.start();
		}
		dbConnection.close();		
		
		Thread clientThread = new Thread(_freebaseFinder);
		clientThread.start();
	}
*/
	
		
			
		
	
				/**
				 * <p>Add a map of part of speech tag (POS tags) to the content items map.</p>
				 * @param posTagsMap part of speeches tags map extracted during analysis.
				 */
	/*
	public void create(NPosTagsMap posTagsMap)  {
		CPosFinder posFilter = posTagsMap.getPosFinder();
		
		NContentItem curContent = null;
		Set<String> entitiesSet = new HashSet<String>();
		for( NPosTag posTag  : posTagsMap.values()) {
			
			if( posTag.isMisspelled() ) {
				curContent = new NContentItem();
				curContent.setMisspelled();
				super.put(posTag.getString(), curContent);
			}
				
			else {
				entitiesSet.add(posTag.getString());
			}
		}
		
		if( entitiesSet.size() > 0) {
			
			Map<String, NEntityEntriesInfo> entityEntriesInfoMap = new HashMap<String, NEntityEntriesInfo>();
			Map<String, NDbpedia> entitydbpediaMap = new HashMap<String, NDbpedia>();
			posFilter.extract(entitiesSet, entityEntriesInfoMap, entitydbpediaMap);
			NEntityEntries entityEntries = null;
			NSummaryLabel dbpediaEntry = null;
			
			for(String entityName : entitiesSet) {
				
				NEntityEntriesInfo entityEntriesInfo = entityEntriesInfoMap.get(entityName);
				if( entityEntriesInfo != null) {
					entityEntries = new NEntityEntries(entityEntriesInfo);
					curContent = add(entityName, entityEntries);
				}
				
			
				NDbpedia dbpedia = entitydbpediaMap.get(entityName);
				
			
				if( dbpedia == null ) {
					NEntityEntry firstEntityEntry = entityEntriesInfo.getFirstEntityEntry();
					if( firstEntityEntry != null ) {
						dbpedia = new NDbpedia(entityName, firstEntityEntry.getAbstract());
						dbpedia.setLgAbstract(firstEntityEntry.getArticle());
					}
				}
				
				if( dbpedia != null ) {
					dbpediaEntry = new NSummaryLabel(dbpedia);
					curContent = add(entityName, dbpediaEntry);
				}
			
				else {
					posTagsMap.remove(entityName);
				}
			}
		}
	}
	*/
	
	
	public final List<String> getTypes(final String type) {
		List<String> typesList = new LinkedList<String>();
		for( String keyword : keySet()) {
			if( get(keyword).isType(type)) {
				typesList.add(keyword);
			}
		}
		
		return typesList;
	}
	
	
	public final Map<String, String> getSearchTerms() {
		if( _searchWordsMap == null ) {
			_searchWordsMap = new HashMap<String, String>();
			NContentItem item = null;
			for( String key : keySet()) {
				item = get(key);
				_searchWordsMap.put(key, item.getSearchEntry());
			}
		}
		
		return _searchWordsMap;
	}
	
	
				/**
				 * <p>Add an entry for a artifact associated with a keyword or a part of speech.</p>
				 * @param key keyword or part of the speech extracted from the text
				 * @param value artifact (Images, Semantic, Map, Videos....) generated for this keyword...
				 * @return new item entry.
				 */
	public NContentItem add(final String key, final NIElement value) {
		NContentItem curContent = null; 
				/*
				 * If the keyword or part of speech already exists then update the value..
				 */
		if( containsKey(key)) {
			curContent = get(key);
			value.add(curContent);
		}
				/*
				 * otherwise create a new content item...
				 */
		else {
			curContent = new NContentItem();
			value.add(curContent);
			super.put(key, curContent);
		}
		
		return curContent;
	}
		
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		NContentItem contentItem = null;
		
		for( String key : keySet()) {
			contentItem = get(key);
			if( contentItem != null ) {
				buf.append(contentItem.toString());
			}
		}
		return buf.toString();
	}
	
}

// --------------------  EOF -------------------------------------------------------
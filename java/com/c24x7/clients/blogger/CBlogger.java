// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients.blogger;


import com.google.gdata.client.blogger.BloggerService;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;


		/**
		 * <p>
		 * Generic class that encapsulate management, creation and deletion of Blog 
		 * posts and comments on Blogger.com</p>
		 * @author Patrick Nicolas
		 * @date 03/24/2011
		 */
public final class CBlogger {
	  protected static final String METAFEED_URL 			= "http://www.blogger.com/feeds/default/blogs";
	  protected static final String FEED_URI_BASE 		= "http://www.blogger.com/feeds";
	  protected static final String POSTS_FEED_URI_SUFFIX = "/posts/default";
	  protected static final String COMMENTS_FEED_URI_SUFFIX = "/comments/default";

	  protected String 		 _feedURI 	= null;
	  protected BloggerService _service	= null;
	  protected boolean		 _isPublish	= true;
	  
	  public enum EContentType {
		  TITLE, CONTENT
	  }

	  /**
	   * <p>Create a blogger client with a service name. User and password are required
	   * to authenticate the client.</p>
	   * @param serviceName  name of the service or blog
	   * @param user name of the user or blogger
	   * @param password  password for the user
	   */
	  public CBlogger(final String serviceName, 
			  		   final String user, 
			  		   final String password) throws BloggerException, IOException {
		  
		  _service = new BloggerService(serviceName);
		  try {
			_service.setUserCredentials(user, password);
			getFeedURI();
		  } 
		  catch (AuthenticationException e) {
			  throw new BloggerException( e.toString());
		  }
		  catch( ServiceException e) {
			  throw new BloggerException( e.toString());
		  }
	  }
	  
	  
	  public void setPublish(boolean publish) {
		  _isPublish = publish;
	  }
	  
	  
	  /**
	   * <p>Creates a new post on a blog. The method creates an entry for the new post using the title, content, authorName parameters. The added post will be returned.</p>
	   * @param title Text for the title of the post to create.
	   * @param content Text for the content of the post to create.
	   * @param authorName Display name of the author of the post.
	   * @param userName username of the author of the post.
	   * @return An Entry containing the newly-created post.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException If the URL is malformed.
	   */
	  
	  
	  public Entry createPost(final String title,
		      				  final String content, 
		      				  final String authorName, 
		      				  final String userName) throws BloggerException, IOException {
		   
		  			/*
		  			 * Create a Blog entry for this new post
		  			 */
		    Entry newEntry = new Entry();
		    newEntry.setTitle(new PlainTextConstruct(title));
		    newEntry.setContent(new PlainTextConstruct(content));
		    Person author = new Person(authorName, null, userName);
		    newEntry.getAuthors().add(author);
		    newEntry.setDraft(!_isPublish);

		    // Ask the service to insert the new entry
		    try {
		    	URL postUrl = new URL(_feedURI + POSTS_FEED_URI_SUFFIX);
		    	newEntry =  _service.insert(postUrl, newEntry);
		    	
		    }
		    catch( ServiceException e) {
		    	throw new BloggerException(e.toString());
		    }
		    
		    return newEntry;
	  }  
	  
	  /**
	   * Updates the title of the given post. The Entry object is updated with the
	   * new title, then a request is sent to the GoogleService. If the insertion is
	   * successful, the updated post will be returned.
	   * 
	   * Note that other characteristics of the post can also be modified by
	   * updating the values of the entry object before submitting the request.
	   * 
	   * @param myService An authenticated GoogleService object.
	   * @param entryToUpdate An Entry containing the post to update.
	   * @param newTitle Text to use for the post's new title.
	   * @return An Entry containing the newly-updated post.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException If the URL is malformed.
	   */
	  public Entry updatePost(Entry 		entry, 
			  				  final String 	newText, 
			  				  EContentType 	type) throws BloggerException, IOException {
		  
		  try {
			  PlainTextConstruct plainText = new PlainTextConstruct(newText);   
			  if( type == EContentType.TITLE)  { 
				  entry.setTitle(plainText);
			  }
			  else {
				  entry.setContent(plainText);
			  }
			  URL editUrl = new URL(entry.getEditLink().getHref());
			  entry = _service.update(editUrl, entry);
			  return entry;
		  }
		  catch(ServiceException e) {
			  throw new BloggerException(e.toString());
		  }
	  }
	  
	  /**
	   * <p>Removes the post specified by the given editor link</p>
	   * @param editLinkHref The URI given for editing the post.
	   * @throws BloggerException If the service is unable to handle the request.
	   * @throws IOException If there is an error communicating with the server.
	   */
	  public void deletePost(final String editorLink) throws BloggerException, IOException {
		  try {
			  URL deleteUrl = new URL(editorLink);
			  _service.delete(deleteUrl);
		  }
		  catch( ServiceException e) {
			  throw new BloggerException(e.toString());
		  }
		  catch( IOException e) {
			  throw new BloggerException(e.toString());
		  }
	  }

	  
	  		/**
	  		 * <p>Add a comment to an existing post.</p>
	  		 * @param postId  id of the existing post
	  		 * @param commentText comment test
	  		 * @return the comment entry
	  		 * @throws BloggerException if comment could not be added to the post.
	  		 */
	  public Entry addComment(final String postId,
		      				  final String commentText) throws BloggerException, IOException {
		    
		  	Entry newEntry = null;
		    String commentsFeedUri = _feedURI + "/" + postId + COMMENTS_FEED_URI_SUFFIX;
		    try {
		    	URL feedUrl = new URL(commentsFeedUri);

		    	// Create a new entry for the comment and submit it to the GoogleService
		    	newEntry = new Entry();
		    	newEntry.setContent(new PlainTextConstruct(commentText));
		    	newEntry = _service.insert(feedUrl, newEntry);	
		    }
		    catch( ServiceException e) {
		    	throw new BloggerException(e.toString());
		    }
		    
		    return newEntry;
      }
	  
	  
	  /**
	   * <p>Removes the comment specified by the given editor link.</p>
	   * @param editorLink The URI given for editing the comment.
	   * @throws BloggerException If the service is unable to handle the request.
	   * @throws IOException If there is an error communicating with the server.
	   */
	  public void deleteComment(final String editorLink) throws BloggerException, IOException {
		  try {
			  URL deleteUrl = new URL(editorLink);
			  _service.delete(deleteUrl);
		  }
		  catch( ServiceException e) {
		    	throw new BloggerException(e.toString());
		  }
	  }

	  
	  


	  /**
	   * Parses the meta feed to get the blog ID for the authenticated user's default
	   * blog.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException If the URL is malformed.
	   */
	  protected void getFeedURI() throws ServiceException, IOException  {
		  	/*
		  	 * Get the meta feed
		  	 */
		  final URL feedUrl = new URL(METAFEED_URL);
		  Feed resultFeed = _service.getFeed(feedUrl, Feed.class);

	    	/*
	    	 *  If the user has a blog then return the id (which comes after 'blog-')
	    	 */
		  if (resultFeed.getEntries().size() <= 0) {	
			  throw new IOException("Cannot extract Feed Resource");
		  }
		  Entry entry = resultFeed.getEntries().get(0);
		  String blogId = entry.getId().split("blog-")[1];
		  if(blogId != null) {
			  _feedURI = FEED_URI_BASE + "/" + blogId;
		  }
	  }


	  /**
	   * <p>
	   * Displays the titles of all the posts in a blog. First it requests the posts
	   * feed for the blogs and then is prints the results.</p>
	   */
	  public String toString() {
		  StringBuilder buf = new StringBuilder();
		  try {
			  URL feedUrl = new URL(_feedURI + POSTS_FEED_URI_SUFFIX);
			  Feed resultFeed = _service.getFeed(feedUrl, Feed.class);

			  buf.append("\nTitle:        ");
			  buf.append(resultFeed.getTitle().getPlainText());
			  Entry entry = null;
		  
			  for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				  entry = resultFeed.getEntries().get(i);
				  buf.append("\nPost Title:    ");
				  buf.append(entry.getTitle().getPlainText());
			  }
		  }
		  catch( IOException e) {
			  buf.append("error:" );
			  buf.append(e.toString());
		  }
		  catch( ServiceException e) {
			  buf.append("error:" );
			  buf.append(e.toString());
		  }
		  return buf.toString();
	  }

	  
}


// ---------------------  EOF ----------------------------------
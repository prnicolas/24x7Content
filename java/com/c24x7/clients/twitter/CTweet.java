// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.clients.twitter;

import java.util.Calendar;
import java.util.Date;


			/**
			 * <p>Implement the fields of a tweet. This class is cloneable in order
			 * to replicate the tweets across multiple users, if necessary.</p>
			 * @author Patrick Nicolas
			 */
public final class CTweet implements Cloneable {
	/**
	 * Attribute of social item: id
	 */
	private long _id = -1L;

		/**
		 * Attributes of social item: content
		 */
	private String _content = null;

		/**
		 * Attributes of social item: social network
		 */
	private String  _source = null;

		/**
		 * Attributes of social item: Time stamp
		 */
	private Date  _date = null;

		/**
		 * Attributes of social item: Author of the message
		 */
	private String  _author = null;
	
	private String  _link 	= null;


			/**
			 * <p>Create an unitialized tweet object</p>
			 */
	public CTweet() { }
	
		/**
		 * <p>Create a fully intialized tweet object</p>
		 * @param id Tweet id
		 * @param content Tweet content
		 * @param source Tweet source
		 * @param date Tweet date
		 * @param author Tweet author
		 */
	public CTweet(	long id, 
					final String content, 
					final String source, 
					final Date date, 
					final String author,
					final String link) {
		_id = id;
		_content = content;
		_source = source;
		_date = date;
		_author = author;
		_link = link;
	}

	/**
	 * initialize the id of this message
	 * @param id identifier for the message
	 */
	public void setId(long id) {
		_id = id;
	}
	
	public final long getId() {
		return _id;
	}
	
	
	
	/**
	 * Set up the author (person or organization) of this message
	 * @param author source of this message
	 */
	public void setAuthor(String author) {
		_author = author;
	}

	/**
	 * Retrieve the author of this message
	 * @return message author
	 */
	public final String getAuthor() {
		return _author;
	}

	/**
	 * Initialize the source (social network, blog, web site) for this message
	 * @param source network or site the message has been broadcasted
	 */
	public void setSource(final String source) {
		_source = source;
	}

	/**
	 * Retrieve the source (social network) for this social message
	 * @return source of the social message
	 */
	public final String getSource() {
		return _source;
	}
	
	
	/**
	 * Set the time stamp for this social message
	 * @param date textual representation of the time stamp
	 */
	public void setDate(final Date date) {
		_date = date;
	}
	
	public final Date getDate() {
		return _date;
	}

	
	public void setContent(final String content) {
		_content = content;
	}
	
	public final String getContent() {
		return _content;
	}
	
	
	public void setLink(final String link) {
		_link = link;
	}
	
	public final String getLink() {
		return _content;
	}
	


	public CTweet clone() {
		return new CTweet(	_id, 
							new String(_content), 
							new String(_source),
							new Date(_date.getTime()),
							new String(_author),
							new String(_link));
	}


	public final Date getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
	
		return cal.getTime();
	}


	/**
	 * <p>Dump the context of this social message</p>
	 * @return the textual context of this message
	 */
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("\nid: ");
		buf.append(_id);
		buf.append("  Author: ");
		buf.append(_author);
		buf.append("  Source: ");
		buf.append(_source);
		buf.append(" Date: ");
		buf.append(_date);
		buf.append("\nMessage:----------\n");
		buf.append(_content);
	
		buf.append("\n Link:");
		buf.append(_link);
		return buf.toString();
	}

}
// -----------------------  EOF -------------------
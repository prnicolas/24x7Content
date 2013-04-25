// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.users;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import com.c24x7.models.CSummaryModel;
import com.c24x7.util.CDbUtil;
import com.c24x7.util.logs.CLogger;



		/**
		 * <p>Map of users currently in sessions.</p>
		 * @author Patrick Nicolas
		 * @date 09/24/2011
		 */
public class CUsersManager extends ConcurrentHashMap<Long, CUsersManager.CUser> {

	protected static final long serialVersionUID = 1171410653233041274L;
	protected static CUsersManager instance = new CUsersManager();
	
	public static CUsersManager getInstance() {
		return instance;
	}
	
	public CUser put(long userId, CUser user) {
		CUser newUser = null;
		if( userId != -1L && user != null ) {
			newUser = super.put(new Long(userId), user);
		}
		
		return newUser;
	}
	
	
	public boolean containsKey(long userId) {
		return super.containsKey(new Long(userId));
	}
	
	public CUser get(long userId) {
		return super.get(new Long(userId));
	}
	
	
	/**
	 * <p>Class that define a user with the list of documents and scrapbooks,</p> 
	 * @author Patrick Nicolas
	 * @date 09/26/2011
	 */
	public static class CUser {
		protected long 			_id = -1L;
		protected String 			_login = null;
		protected String 			_password = null;

		protected CSummaryModel		_model = null;

		public CUser(final String login, final String password, long id) {
			_login = login;
			_password = password;
			_id = id;
		}


		public final String getLogin() {
			return _login;
		}

		public final String getPassword() {
			return _password;
		}

		public final long getId() {
			return _id;
		}
		
		public final CSummaryModel getModel() {
			return _model;
		}

		
		public void setModel(CSummaryModel model) {
			_model = model;
		}


	}
	
	
	/**
	 * <p>Create a user object by extracting the information from the database.</p>
	 * 	@param login_str login string as entered by the user
	 * @param pwd_str password string as entered by the user
	 * @return a new user object if credentials are valid, null otherwise.
	 */
	public CUser create(String login_str, String pwd_str) {
		CUser newUser = null;

		if( login_str != null && pwd_str != null ) {
			login_str = login_str.trim();
			pwd_str = pwd_str.trim();

			try {
				CDbUtil database = new CDbUtil();
				Statement stmt = database.create();
				long userId = -1L;
				String cookieStr = null;
				
				ResultSet res = stmt.executeQuery(sqlGetUserIds(login_str, pwd_str));
				while (res.next() ) {
					userId = res.getInt("id");
					cookieStr = res.getString("cookie");
						/*
						 * Create a new user and add to the user/session manager.
						 */
					if (userId != -1L) {
						newUser = new CUser(login_str, pwd_str, userId);
						put(userId, newUser);
					}
				}
	
				if( newUser != null ) {
		//			CDocument doc = null;
					String title = null;
					String content = null;
					String date = null;
		
					res = stmt.executeQuery(sqlGetPastDocuments(userId));
					while (res.next() ) {
						title = res.getString("title");
						content = res.getString("content");
						/*
						try {
							
							doc = new CDocument(URLDecoder.decode(title, "Latin1"),
								    			URLDecoder.decode(content, "Latin1"),
								    			res.getLong("id"));
							date = res.getDate("created_at").toString();
							doc.setCreatedAt(date);
							newUser.addDocument(doc);
						
						}
						catch( UnsupportedEncodingException e) {
							CLogger.error(e.toString());
						}
							*/
					}
				}
				database.close();
				database = null;
			}
			catch( SQLException e) {
				CLogger.error(e.toString());
			}
		}

		return newUser;
	}
	
	// ---------------------------
	// Private Supporting Methods
	// --------------------------

	protected static String sqlGetUserIds(final String login_str, final String pwd_str) {
		StringBuilder buf = new StringBuilder("SELECT id, cookie FROM 24x7c.account WHERE login =\'");
		buf.append(login_str);
		buf.append("\' AND pwd =\'");
		buf.append(pwd_str);
		buf.append("\';");

		return buf.toString();
	}

	protected static String sqlGetPastDocuments(long userId) {

		StringBuilder buf = new StringBuilder("SELECT title, content, created_at, id FROM 24x7c.recent_docs WHERE user_id=");
		buf.append(userId);
		buf.append(";");

		return buf.toString();
	}
}

// -----------------------------------  EOF ---------------------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.JSONException;
import org.json.JSONStringer;

import com.c24x7.models.CContentItemsMap;
import com.c24x7.models.CSummaryModel;
import com.c24x7.nlservices.CMediaExtractionService;
import com.c24x7.search.CImagesSearch;
import com.c24x7.search.CMapSearch;
import com.c24x7.search.CYouTubeSearch;
import com.c24x7.exception.SearchException;
import com.c24x7.users.CUsersManager;
import com.c24x7.users.CUsersManager.CUser;
import com.c24x7.util.logs.CLogger;


				/**
				 * <p>Servlet to extract images, videos, maps and search results as AJAX GET HTTP requests.</p>
				 * @author Patrick Nicolas
				 * @data 08/29/2011
				 */
public class CSearchItemServlet extends HttpServlet {

	protected static final int MIN_SEARCH_RESULTS_LENGTH = 8;
	protected static final long serialVersionUID = -1796548983708698325L;

	public void doGet(HttpServletRequest request, 
					  HttpServletResponse response) throws ServletException, IOException {
		
		CLogger.info("Search item request");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String jsonString = null;
		
		try {
			String keyword = request.getParameter("input_val");
			String userIdStr = request.getParameter("user_identity");
			long userId = ( userIdStr != null && userIdStr.length() > 0) ? Long.parseLong(userIdStr) : -1L;

			CLogger.info("Get update for " + keyword + " and userid =" + userId);
		
			CUsersManager usersManager = CUsersManager.getInstance();
			
			if( usersManager.containsKey(userId)) {
				CUser thisUser = usersManager.get(userId);
				CSummaryModel model = thisUser.getModel();
				
				CLogger.info("Get update from " + thisUser.getLogin());
				if( model != null ) {
					CMediaExtractionService search = new CMediaExtractionService(keyword);
					search.add(new CMapSearch(keyword));
					search.add(new CImagesSearch(keyword));
					search.add(new CYouTubeSearch(keyword));
					
					search.execute(model);
				
					while( !search.isCompleted() ) {
						try {
							Thread.sleep(250);
						}
						catch (InterruptedException e) {
							CLogger.error(e.toString());
						}	
					}
				
					CContentItemsMap itemsMap = model.getContentItemsMap();
					CContentItemsMap.NContentItem item = itemsMap.get(keyword);
					if( item != null ) {
						String newHTMLMediaFragment = item.toHtmlFragment(keyword);
						if( newHTMLMediaFragment.length() < MIN_SEARCH_RESULTS_LENGTH) {
							newHTMLMediaFragment = "none";
						}
						String updateHTMLImageFragment = item.toHtmlImageFragment(keyword);
						if( updateHTMLImageFragment.length() < MIN_SEARCH_RESULTS_LENGTH) {
							updateHTMLImageFragment = "none";
						}
						jsonString = new JSONStringer().object().
											    	key("status").value("ok").
											    	key("media").value(newHTMLMediaFragment).
											    	key("images").value(updateHTMLImageFragment).
											    	endObject().
											    	toString();
						out.println(jsonString);
					}
				}
				
					/*
					 * If data could not be retrieved.... the AJAX call should return a error..
					 */
				if( jsonString == null ) {
					jsonString = new JSONStringer().object().
											key("status").value("err").
											key("media").value("Could not extract media files").endObject().toString();
					out.println(jsonString);
				}
			}
		}
		catch( JSONException e) {
			CLogger.error(e.toString());
		}
		
		catch( SearchException e) {
			try {
				jsonString = new JSONStringer().object().key("status").value("err").endObject().toString();
			}
			catch( JSONException ex) {
				CLogger.error(ex.toString());
			}
		}
		finally {
			if(jsonString == null ) {
				CLogger.error("Cannot create a JSON stream");
			}
			out.close();
		}
	}

}

// ------------------------------  EOF ------------------------------------
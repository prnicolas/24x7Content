// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.c24x7.exception.InitException;
import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.models.CSummaryModel;
import com.c24x7.output.html.CHTMLPage;
import com.c24x7.nlservices.CTextSemanticService;
import com.c24x7.users.CUsersManager;
import com.c24x7.users.CUsersManager.CUser;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;



				/**
				 * <p>Servlet that analyzed content and provide basic information.</p>
				 * @author Patrick Nicolas
				 * @date 10/05/2011
				 */
public class CAnalysisServlet extends HttpServlet {

	protected static final long serialVersionUID = -179654863708698325L;


	public void doPost(	HttpServletRequest request, 
  						HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		String outputHtml = null;

		try {
			String seed = request.getParameter("seed");
			String userIdStr = request.getParameter("user_id");
			long userId = ( userIdStr != null && userIdStr.length() > 0) ? Long.parseLong(userIdStr) : -1L;

			CEnv.init();    
			System.out.println("userID=" + userIdStr);
			CUsersManager usersManager = CUsersManager.getInstance();
				
					/*
					 * If the user is currently logged in ....
					 */
			if( usersManager.containsKey(userId)) {
				CUser thisUser = usersManager.get(userId);
				System.out.println("Create model for " + thisUser.getLogin());
				
					/*
					 * Analyze the content...
					 */
				CTextSemanticService analyzer = new CTextSemanticService();	
				CSummaryModel model = new CSummaryModel(null, seed);

			//	analyzer.execute(model);		
	
					/*
					 * Create the HTML page from the model..
					 */
				CHTMLPage htmlPage = new CHTMLPage(thisUser);	
				htmlPage.write(model);
				outputHtml = htmlPage.toString();
					/*
					 * Update the persistent cache (Database)
					 */
				thisUser.setModel(model);
			}
			System.out.println("Done");
		}
		catch( InitException e) {
			outputHtml = "Error: " + e.toString();
		}
		catch( SemanticAnalysisException e) {
			CLogger.error(e.toString());
		}

		catch( IllegalArgumentException e) {
			outputHtml = "Error: " + e.toString();
		}
					/*
					 * If no error then sent the entire page using
					 * compression scheme if possible...
					 */
		if( outputHtml != null) {
			sendOutputStream(request, response, outputHtml);
		}
	}


		// --------------------------
		//  Supporting Private Methods
		// ----------------------------

	protected void sendOutputStream(HttpServletRequest request, 
						  HttpServletResponse response, 
						  final String outputHtml) throws IOException {

		String encodingStr = request.getHeader("Accept-Encoding");
				/*
				 * If the browser accepts compressed files, then compresses using the
				 * default GZIP utilities
				 */
		if( encodingStr != null && encodingStr.indexOf("gzip") != -1) {
				/*
				 * Should notify the browser that this is a GZIP compressed stream..
				 */
			response.setHeader("Content-Encoding", "gzip");
			ServletOutputStream servletOut = response.getOutputStream();

			GZIPOutputStream gzipOut = new GZIPOutputStream(servletOut);
			gzipOut.write(outputHtml.getBytes());
			gzipOut.finish();
			gzipOut.close();
			servletOut.close();
		}
				/*
				 * Otherwise send the HTML page as it is, uncompressed//
				 */
		else {
			PrintWriter out = response.getWriter();
			out.println(outputHtml);
			out.close();
		}
	}
}


// ------------------------------  EOF ------------------------------------
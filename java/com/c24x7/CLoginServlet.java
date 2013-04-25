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

import com.c24x7.exception.InitException;
import com.c24x7.output.html.CHTMLInputWriter;
import com.c24x7.users.CUsersManager.CUser;
import com.c24x7.users.CUsersManager;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;

				/**
				 * <p>Servlet that processes the login information and generates the list of favorites,
				 * and most recent requests for analysis.</p> 
				 * @author Patrick Nicolas
				 * @date 08/22/2011
				 */

public class CLoginServlet extends HttpServlet {	

	protected static final long serialVersionUID = -179654863708681325L;
	

	public void doGet(HttpServletRequest request, 
	  		   		  HttpServletResponse response) throws ServletException, IOException {
		
		CLogger.info("Login Ajax");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String jsonString = null;
			
		try {
			String login_str = request.getParameter("account");
			String pwd_str = request.getParameter("password");
			
			CUsersManager usersManager = CUsersManager.getInstance();
			/*
			 * Attempt match the login and password
			 */
			CUser newUser = usersManager.create(login_str, pwd_str);
				
			
			/*
			 * If the user is identified and validated...
			 */
			if( newUser != null ) {

						/*
						 * Create a HTML document for this user...
						 */
				CHTMLInputWriter inputWriter = new CHTMLInputWriter();
				inputWriter.write(newUser.getId());
				
				jsonString = new JSONStringer().object().
												key("status").value("ok").
												key("content").value(inputWriter.toString()).
												endObject().toString();
				
			}
			else {
				jsonString = new JSONStringer().object().
												key("status").value("err").
												key("content").value("&nbsp;&nbsp;Incorrect login or password!&nbsp;&nbsp;").
												endObject().toString();		
			}	
			out.println(jsonString);
			CEnv.init();
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
		catch( JSONException e) {
			CLogger.error(e.toString());
		}			
		finally {
			out.close();
		}
	}
	
}

// ------------------------------  EOF ------------------------------------
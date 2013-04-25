// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;

import com.c24x7.util.CEnv;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

import java.net.URLEncoder;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;


import java.net.URL;
import java.io.IOException;
import java.io.OutputStreamWriter;


public final class CFusionTables {
	protected static final String GOOGLE_SERVICE 			= "fusiontables";
	protected static final String GOOGLE_SERVICE_TABLE 	= "fusiontables.content24x7c";
	protected static final String FUSION_TABLES_LOGIN 	= "prn24x7c@gmail.com";
	protected static final String FUSION_TABLES_PASSWORD 	= "NLG24x7C";
	protected static final String SERVICE_URL 			="https://www.google.com/fusiontables/api/query";
	protected static final String TABLE_ID 				= "722462";
	protected static final Pattern CSV_VALUE_PATTERN =
	      Pattern.compile("([^,\\r\\n\"]*|\"(([^\"]*\"\")*[^\"]*)\")(,|\\r?\\n)");
	  
	  protected GoogleService _service = null;
	  	

	  		/**
	  		 * <p>Create a client to Fusion Tables with default authentication</p>
	  		 * @throws AuthenticationException
	  		 */
	  public CFusionTables() throws AuthenticationException  {
		  this(FUSION_TABLES_LOGIN, FUSION_TABLES_PASSWORD);
	  }
	
	
	  		/**
	  		 * <p>Create a client to Fusion Tables with specified authentication</p>
	  		 * @param email Google email used to access the table fusion service
	  		 * @param password Google password used to access the service
	  		 * @throws AuthenticationException
	  		 */
	  
	  public CFusionTables(	final String email, 
			  				final String password) throws AuthenticationException {
		  _service = new GoogleService(GOOGLE_SERVICE, GOOGLE_SERVICE_TABLE);
		  _service.setUserCredentials(email, password, ClientLoginAccountType.GOOGLE);
	  }


	  
	  			/**
	  			 * <p>Query the content of a Fusion Table.</p>
	  			 * @param params fields to retrieve
	  			 * @param condition SQL Where condition used to filter the query
	  			 * @return list of fields
	  			 * @throws IOException if connectivity fails
	  			 * @throws ServiceException if TableFusion service is down
	  			 */
	  public String select(	final String params, 
			  				final String condition) throws IOException, ServiceException {
		  
		  StringBuilder buf = new StringBuilder("select ");
		  buf.append(params);
		  buf.append(" from ");
		  buf.append(TABLE_ID);
		  buf.append(" where ");
		  buf.append(condition);
		  
		  URL url = new URL(SERVICE_URL + "?sql=" + URLEncoder.encode(buf.toString(), "UTF-8"));
		  GDataRequest request = _service.getRequestFactory().getRequest(RequestType.QUERY, url, 
														   ContentType.TEXT_PLAIN);

		  request.execute();
		  return getResponse(request);
	  }
	  
	  
	  			/**
	  			 * <p>Insert a new row into the default fusion table.</p>
	  			 * @param insertParams parameters to insert
	  			 * @return row index of insertion
	  			 * @throws IOException if connectivity fails
	  			 * @throws ServiceException if TableFusion service is down
	  			 */
	  public String insert(final String insertParams) throws IOException,ServiceException {
		  URL url = new URL(SERVICE_URL);
		  GDataRequest request = _service.getRequestFactory().getRequest(RequestType.INSERT, 
				  														 url,
				  														 new ContentType("application/x-www-form-urlencoded"));
		  OutputStreamWriter writer = new OutputStreamWriter(request.getRequestStream());
		  StringBuilder buf = new StringBuilder("insert into ");
		  buf.append(TABLE_ID);
		  buf.append(" (Name, City) values ");
		  buf.append(insertParams);
		  writer.append("sql=" + URLEncoder.encode(buf.toString(), "UTF-8"));
		  writer.flush();

		  request.execute();
		  return getResponse(request);
	  }
	  
	  
	  protected String getResponse(GDataRequest request) throws IOException { 
		  StringBuilder buf = new StringBuilder();
		  Scanner scanner = new Scanner(request.getResponseStream(),"UTF-8");
		  
		  while (scanner.hasNextLine()) {
			  scanner.findWithinHorizon(CSV_VALUE_PATTERN, 0);
			  MatchResult match = scanner.match();
			  String quotedString = match.group(2);
			  String decoded = quotedString == null ? match.group(1) : quotedString.replaceAll("\"\"", "\"");
			  buf.append("|");
			  buf.append(decoded);		  
			  if (!match.group(4).equals(CEnv.FIELD_DELIM)) {
				  buf.append("|");
			  }
		  }
		  return buf.toString();
	  }
}


// ------------------  EOF -------------------------------
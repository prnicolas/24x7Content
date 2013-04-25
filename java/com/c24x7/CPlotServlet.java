package com.c24x7;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONStringer;

import com.c24x7.exception.InitException;
import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.util.CEnv;
import com.c24x7.helper.CPlotHelper;



public final class CPlotServlet extends HttpServlet {

	private static final long serialVersionUID = -7770473847198385068L;

	public void doPost(	HttpServletRequest 	request, 
  						HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		String status = "OK";
		
		try {
			/*
			 * Initialize the environment to extract the
			 * semantic topography from a list of documents.
			 */
			CEnv.init();    
			System.out.println("Start execution");
			StringBuilder matrixDimBuf = new StringBuilder();
			StringBuilder topicPointsBuf = new StringBuilder();
			StringBuilder sentencesBuf = new StringBuilder();
			StringBuilder topicsBuf = new StringBuilder();
			
			new CPlotHelper().benchmarkTestSingle(matrixDimBuf, topicPointsBuf, sentencesBuf, topicsBuf);
			
						/*
						 * If no error then sent the entire page using
						 * compression scheme if possible...
						 */

			try {
				String jsonString =  new JSONStringer().object().
													key("status").value(status).
			    									key("dim").value(matrixDimBuf.toString()).
			    									key("topicpoints").value(topicPointsBuf.toString()).
			    									key("sentences").value(sentencesBuf.toString()).
			    									key("topics").value(topicsBuf.toString()).
			    									endObject().
			    									toString();
			
				sendOutputStream(request, response, jsonString);
			}
			catch( JSONException e) {
				System.out.println(e.toString());
			}
		}
		catch( InitException e) {
			status = "ERR";
			System.out.println(e.toString());
		}
		catch( SemanticAnalysisException e) {
			status = "ERR";
			System.out.println(e.toString());
		}
		catch( IllegalArgumentException e) {
			status = "ERR";
			System.out.println(e.toString());
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

// -----------------------------------  EOF ---------------------------------------
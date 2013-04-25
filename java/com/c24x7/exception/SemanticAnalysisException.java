/*
 *  Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.exception;

		/**
		 * <p>Class that manage exceptions occurring during the 
		 * execution of analysis of text, documents.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 19, 2012 7:13:03 PM
		 */
public final class SemanticAnalysisException extends Exception {
	private static final long serialVersionUID = 2298002697174250878L;


		/**
		 * <pCreate a semantic analysis exception with a predefined message.</p>
		 * @param message message of the error or exception
		 */
	public SemanticAnalysisException(final String message) {
		super(message);
	}
	
		/**
		 * <p>Create an semantic analysis exception from an existing exception.</p>
		 * @param e exception caught by this handler
		 */
	public SemanticAnalysisException(Exception e) {
		super(e.toString());
	}
	
	
		/**
		 * <p>Display the stack trace and description of the exception.</p>
		 * @return stack trace and exception message.
		 */
	@Override
	public String toString() {
		printStackTrace();
		return super.toString();
	}

}

// -------------------------  EOF -----------------------------

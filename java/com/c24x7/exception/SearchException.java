/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.exception;


			/**
			 * <p>Generic exception for search related classes and methods</p>
			 * @author Patrick Nicolas
			 * @date 04/22/2011
			 */
public class SearchException extends Exception {

	private static final long serialVersionUID = 6853181912931123543L;

	public SearchException(final String description) {
		super(description);
	}
	
		/**
		 * <p>Create a search exception from an existing exception.</p>
		 * @param e exception caught by this handler
		 */
	public SearchException(Exception e) {
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

// ---------------------  EOF ------------------------------------
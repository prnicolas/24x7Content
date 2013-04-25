/*
 * Copyright (C) 2010-2012 Patrick Nicolas
 */
package com.c24x7.exception;


			/**
			 * <p>Generic class to handle static initialization exception
			 * or errors.</p>
			 * @author Patrick Nicolas         24x7c 
			 * @date June 11, 2012 5:20:35 PM
			 */
public final class InitException extends Exception {
	private static final long serialVersionUID = 6925428287013843748L;

		/**
		 * <pCreate a initialization exception with a predefined message.</p>
		 * @param message message of the error or exception
		 */
	public InitException(final String message) {
		super(message);
	}
	
		/**
		 * <p>Create an initialization exception from an existing exception.</p>
		 * @param e exception caught by this handler
		 */
	public InitException(Exception e) {
		super(e.toString());
	}
	
	
		/**
		 * <p>Display the stack trace and description of the exception.</p>
		 * @return stack trace and exception message.
		 */
	@Override
	public String toString() {
		return super.toString();
	}
}

// --------------- EOF -----------------------------------
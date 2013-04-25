package com.c24x7.exception;



		/**
		 * <p>Class that defined exceptions thrown during execution of
		 * any classification training, validation or testing operations.</p>
		 * 
		 * @author Patrick Nicolas         24x7c 
		 * @date June 16, 2012 2:42:04 PM
		 */
public final class  ClassifierException extends Exception {

	private static final long serialVersionUID = -8370601953966266444L;


		/**
		 * <pCreate an exception instance to be thrown by any 
		 * classifier method during training, validation or testing phases.</p>
		 * @param message message of the error or exception
		 */
	public ClassifierException(final String message) {
		super(message);
	}
	
		/**
		 * <p>Create an initialization exception from an existing exception.</p>
		 * @param e exception caught by this handler
		 */
	public ClassifierException(Exception e) {
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

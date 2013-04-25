// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.analytics;


		/**
		 * <p>Exception for processing Analytics Results, thrown in case of failure of extracting
		 * analytics data.</p>
		 * @author Patrick Nicolas
		 * @date 03/22/2011
		 */
public final class AnalyticsException extends Exception {
	static final long serialVersionUID = 5254245L;
	
		/**
		 * <p>Create an instance for an exception thrown in case of failure of extracting
		 * analytics data.</p>
		 * @param msg Description of the exception
		 */
	public AnalyticsException(final String msg) {
		super(msg);
	}
}

// -------------------------  EOF ----------------------------------

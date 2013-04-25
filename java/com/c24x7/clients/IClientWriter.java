// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.clients;


		/**
		 * <p>Remote Writer class.</p>
		 * @author Patrick Nicolas
		 * @date 03/08/2011
		 */
public interface IClientWriter extends Runnable {
	public IClientWriter create(String content);
	public String getError();
}

// ---------------------- EOF ------------------------------------------

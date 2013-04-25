// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.search;

import com.c24x7.exception.SearchException;
import com.c24x7.models.CSummaryModel;
import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Generic class for searching for items such as Wikipedia,
			 * images, videos.. on the web.</p>
			 * @author Patrick Nicolas
			 * @date 07/12/2011
			 */
public abstract class AItemSearch extends Thread {
	
	public interface NIEntry { }
	/**
	 * <p>Update model with the item discovered during a search.</p>
	 * @param model model to be built or updated..
	 */
	public abstract void getItems(CSummaryModel model);
	
	protected abstract void search() throws SearchException;
	
	
			/**
			 * <p>Main routine to search for images..</p>
			 */
	public void run() {
		try {
			search();
		}
		catch( SearchException e) {
			CLogger.error(e.toString());
		}
	}
}

// ------------------------------- EOF ------------------------------------
// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.nlservices;

import java.util.LinkedList;
import java.util.List;

import com.c24x7.models.CSummaryModel;
import com.c24x7.search.AItemSearch;
import com.c24x7.exception.SearchException;
import com.c24x7.util.logs.CLogger;


		/**
		 * <p>Basic search services that generate maps, images, videos and search results...</p>
		 * @author Patrick Nicolas
		 * @date 07/22/2011
		 */
public final class CMediaExtractionService extends Thread {
	protected List<AItemSearch> _itemSearchList	= null;
	protected CSummaryModel  	 _model 			= null;
	protected boolean			 _completed			= false;
	
	
	
			/**
			 * <p>Create a search request object.</p>
			 * @param keyword keyword or part of speech 
			 */
	public CMediaExtractionService( final String keyword) {
		if( keyword == null) {
			throw new IllegalArgumentException("Cannot search for undefined part of speech.");
		}
		_itemSearchList = new LinkedList<AItemSearch>();	
	}
	
	
	public void add(AItemSearch itemSearch) {
		_itemSearchList.add(itemSearch);
	}
	

				/**
				 * <p>Execute the search for a specific model or context.</p>
				 * @param model model for the document..
				 * @throws SearchException
				 */
	public void execute(CSummaryModel model) throws SearchException {
		if(_itemSearchList.size() > 0) {
			_model = model;
				/*
				 * Launches all the search threads for concurrency...
				 */
			for( AItemSearch itemSearch : _itemSearchList) {
				itemSearch.start();
			}
				/*
				 * Start this listening threads.
				 */
			this.start();
		}
	}
	
	
			/**
			 * <p>Retrieve the reference to the updated model..
			 * @return model for this analysis of this document.
			 */
	public final CSummaryModel getModel() {
		return _model;
	}
	
	
			/**
			 * <p>Test if the search is completed..</p>
			 * @return true if the search is completed, false otherwise.
			 */
	public final boolean isCompleted() {
		return _completed;
	}
	

	
				/**
				 * <p>Execute the different threads that extracts images, maps, 
				 * Wikipedia and videos from the web.</p>
				 */
	public void run() {
		try {
					/*
					 * This listening thread joins all search threads and returns the respective
					 * search results into the model for the search..
					 */
			for( AItemSearch itemSearch : _itemSearchList) {
				itemSearch.join();
				itemSearch.getItems(_model);
			}
			_completed = true;
		}
		catch( InterruptedException e) {
			CLogger.error("Search thread interrupted: " + e.toString());
		}
	}

}

// ----------------------------  EOF --------------------------------
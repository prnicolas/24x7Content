// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.analytics;

import com.c24x7.analytics.data.CReport;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.AccountEntry;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.net.URL;
import java.io.IOException;


		/**
		 * <p>Class that manage Google Analytics results for a specific site.</p>
		 * @author Patrick Nicolas
		 * @date 03/22/2011
		 */
public final class CGoogleAnalytics implements IAnalytics {
	protected static final String GOOGLE_ANALYTICS_ACCOUNT_URL = "https://www.google.com/analytics/feeds/accounts/default?max-results=10";
	protected static final String GOOGLE_ANALYTICS_DATA_URL 	= "https://www.google.com/analytics/feeds/data";
	
	protected AnalyticsService _analyticsService 	= null;
	protected String[] 		 _tableIds 			= null;
	
	
			/**
			 * <p>Create a Google Analytics instance for a specific application, user.</p>
			 * @param appName  name of the application
			 * @param userName  name of the user
			 * @param userPwd  password of the user.
			 * @throws AnalyticsException  if authentication fails or analytics are not properly setup
			 * @throws IOException if connectivity to Google analytics fails.
			 */
	public CGoogleAnalytics(final String appName, 
							final String userName, 
							final String userPwd) throws AnalyticsException, IOException {
		
		_analyticsService = new AnalyticsService(appName);
		try {
			authenticate(userName, userPwd);
			getTableIds();
		}
		catch( AuthenticationException e) {
			throw new AnalyticsException(e.toString());
		}
		catch(ServiceException e ) {
			throw new AnalyticsException(e.toString());
		}
	}
	

	 	/**
	 	 * <p>Retrieve the Analytics data for a specific period of time.</p>
	 	 * @param startDate  Start date for the period of statistics collection
	 	 * @param endDate  Ending date for the period of statistics collection
	 	 * @throws AnalyticsException  if authentication fails or analytics are not properly setup
	     * @throws IOException if connectivity to Google analytics fails.
	 	 */
	public CReport getData(final String startDate, 
							final String endDate) throws IOException, AnalyticsException {
		CReport report = null;
		
		if(_tableIds != null) {
			try {
				URL[] urls = getFeed(startDate, endDate);
				String curPageTitle = null;
				
				for(int j = 0; j < urls.length; j++) {
					DataFeed feed = _analyticsService.getFeed(urls[j], DataFeed.class);
					report = new CReport(feed.getTitle().getPlainText());
					report.setNumRecords(feed.getTotalResults());
					report.setPeriod(feed.getStartDate().getValue(), feed.getEndDate().getValue());
					
					for( DataEntry entry : feed.getEntries() ) {
						curPageTitle = entry.stringValueOf("ga:pageTitle");
						for (Metric metric : entry.getMetrics() ) {
							report.add(curPageTitle, metric.getName(), metric.getValue(), metric.getType());
						}
					}
				}
			}
			catch( ServiceException e) {
				throw new AnalyticsException(e.toString());
			}
		}
		
		return report;
	}
		
		
 	/**
 	 * <p>Generate a string for Analytics data for a specific period of time.</p>
 	 * @param startDate  Start date for the period of statistics collection
 	 * @param endDate  Ending date for the period of statistics collection
 	 * @throws AnalyticsException  if authentication fails or analytics are not properly setup
     * @throws IOException if connectivity to Google analytics fails.
 	 */

	public String print(final String startDate, 
						final String endDate) throws IOException, AnalyticsException {
		String content = "No data available";
		
		if(_tableIds != null) {
			try {
				URL[] urls = getFeed(startDate, endDate);
				StringBuilder buf = new StringBuilder();
			
				for(int j = 0; j < urls.length; j++) {
					DataFeed feed = _analyticsService.getFeed(urls[j], DataFeed.class);
					
					buf.append("\nFeed Title            = ");
					buf.append(feed.getTitle().getPlainText());
					buf.append("\nTotal Results         = ");
					buf.append(feed.getTotalResults());
					buf.append("\nStart Date            = ");
					buf.append(feed.getStartDate().getValue());
					buf.append("\nEnd Date              = ");
					buf.append(feed.getEndDate().getValue());
				
					for( DataEntry entry : feed.getEntries() ) {
						buf.append("\nPage title = ");
						buf.append(entry.stringValueOf("ga:pageTitle"));
						buf.append("\nNum visits = ");
						buf.append( entry.stringValueOf("ga:visits"));
						buf.append("\nPage views = ");
						buf.append(entry.stringValueOf("ga:pageviews"));
					}
				}		
				content = buf.toString();
			}
			catch( ServiceException e) {
				throw new AnalyticsException(e.toString());
			}
		}
		return content;
	}
	
		
	
				// ------------------
				//  Private Methods
				// ------------------
	
	protected void authenticate(final String name, 
							  final String password) throws AuthenticationException {
		_analyticsService.setUserCredentials(name, password);
	}
	
	
	protected void getTableIds() throws ServiceException, IOException {
		URL queryUrl = new URL(GOOGLE_ANALYTICS_ACCOUNT_URL);
		
		AccountFeed accountFeed = _analyticsService.getFeed(queryUrl, AccountFeed.class);
		int numTableIds = accountFeed.getEntries().size();
		
		if( numTableIds > 0) {
			_tableIds = new String[numTableIds];
			int count = 0;
			for (AccountEntry entry : accountFeed.getEntries()) {
				_tableIds[count++] =  entry.getTableId().getValue();
			}
		}
	}
	
		/**
		 * <p>Retrieve the feed for a specific web property.
		 * @param startDate starting date for the metrics"2011-03-21"
		 * @param endDate end date for the metrics the format should be "2011-03-21"
		 */
	protected URL[] getFeed(final String startDate, final String endDate) throws ServiceException, IOException  {
		DataQuery query = new DataQuery(new URL(GOOGLE_ANALYTICS_DATA_URL));
		
		query.setDimensions("ga:source,ga:pageTitle,ga:pagePath");	
		query.setMetrics("ga:visits,ga:pageviews,ga:bounces");
	//	query.setClass("gaid::-11");
	//	query.setFilters("ga:medium==referral");
		query.setSort("-ga:visits");
		query.setMaxResults(10);
		query.setStartDate(startDate);
		query.setEndDate(endDate);

		URL[] urls = new URL[_tableIds.length];
		
		for( int j = 0; j < _tableIds.length; j++) {
			query.setIds(_tableIds[j]);
			urls[j] = query.getUrl();
		}

		    // Send our request to the Analytics API and 
			// wait for the results to come back.
		return urls;
	}
	
}

// ------------------------  EOF ----------------------

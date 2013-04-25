// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.analytics;

import java.io.IOException;
import com.c24x7.analytics.data.CReport;


	/**
	 * <p>Interface that manages analytics results for a specific site.</p>
	 * @author Patrick Nicolas
	 * @date 03/22/2011
	 */
public interface IAnalytics {
 	/**
 	 * <p>Retrieve the Analytics report for a specific period of time.</p>
 	 * @param startDate  Start date for the period of statistics collection
 	 * @param endDate  Ending date for the period of statistics collection
 	 * @throws AnalyticsException  if authentication fails or analytics are not properly setup
     * @throws IOException if connectivity fails
 	 */
	public CReport getData(final String startDate, 
							final String endDate) throws IOException, AnalyticsException;
}

// ----------------------------  EOF ----------------------------

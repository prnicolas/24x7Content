// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import com.c24x7.util.logs.CLogger;


			/**
			 * <p>Utility class that compute the performance of execution of tasks as well as
			 * memory consumption.
			 * The total duration of the process is computed from the first invocation of the 
			 * time method and does not depend on the start time argument.</p>
			 * @author Patrick Nicolas
			 * @date 04/23/2011
			 */
public final class CProfile {
	public static int NUM_RECORDS = 6;
	
	protected static CProfile profile = null;
	protected long _val 		= 0L;
	protected long _total 		= 0L;
	protected long _memoryUsage	= 0L;

	
			/**
			 * <p>Implements the singleton method for a profile class.</p>
			 * @return the unique instance of this profile class
			 */
	public static CProfile getInstance() {
		if( profile == null) {
			profile = new CProfile();
		}
		return profile;
	}
	


			/**
			 * <p>Record time stamp with a comment or description to be added into a log.
			 * The duration of the task is computed as the difference between the current processor 
			 * clock and the start time as entered by the caller.<br>
			 * The total duration of the process is computed from the first invocation of the 
			 * time method and does not depend on the start time argument.</p>
			 * @param description comments to be added to the performance record in log
			 */
	public void time(final String description) {
		StringBuilder buf = new StringBuilder(description);	
		if( _val == 0L) {
			_val = System.currentTimeMillis();
			buf.append(" (start timer) ");
		}
		else {
			long val = System.currentTimeMillis();
			buf.append(" (end timer) [");
			buf.append(String.valueOf((val- _val)*0.001) );
			buf.append(" ], Total=");
			_total += val - _val;
			buf.append(_total*0.001);
			buf.append(" seconds");
			
			_val = val;
		}
		System.out.println(buf.toString());
		CLogger.info(buf.toString());
	}

	
	public void memoryUsage(final String description) {
		StringBuilder buf = new StringBuilder(description);
		if(_memoryUsage == 0L) {
			_memoryUsage = CProfile.getMemoryUsage();
			buf.append(" (start memory) ");
		}
		else {
			long memUsage = CProfile.getMemoryUsage();
			buf.append(" ((end memory) M=");
			buf.append(String.valueOf(memUsage*9.5E-7F));
			buf.append(" Mb, consumed ");
			buf.append(String.valueOf((memUsage-  _memoryUsage)*9.5E-7F));
			buf.append(" Mb");
			_memoryUsage = memUsage;
		}
		
		CLogger.info(buf.toString());
	}
	
	
	
	
	
	
					// --------------------------
					// Private supporting methods
					// ---------------------------
	
	protected CProfile() { 
		_val = 0L;;
		_total = 0L;
		_memoryUsage = 0L;
	}
	
	
	protected static long getMemoryUsage() {
		long memUsage = -1L;
		try {
	      System.gc();
	      System.gc();
	      Thread.yield();
	      System.gc();
	      System.gc();
	      Thread.sleep(50);
	      System.gc();
	      System.gc();
	      memUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	    }
	    catch (Exception e) {}
	    return memUsage;
	}

}

// ---------------------------------------  EOf ---------------------------------------------------

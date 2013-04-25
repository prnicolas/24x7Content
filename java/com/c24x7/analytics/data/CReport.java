// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.analytics.data;

import java.util.Map;
import java.util.HashMap;


		/**
		 * <p>Class that encapsulates the result of analytics computation for a site
		 * or a blog.</p>
		 * @author Patrick Nicolas
		 * @date 03/22/2011
		 */
public final class CReport {
	
	public static class NType {
		protected String _value= null;
		protected String _type = null;
		
		public NType(final String value, final String type) {
			_value = value;
			_type = type;
		}
		
		public final String getValue() {
			return _value;
		}
		
		public final String getType() {
			return _type;
		}
	}
	
	protected String _title 		= null;
	protected String _startDate 	= null;
	protected String _endDate 	= null;
	protected int	   _numRecords	= 0;
	protected Map<String, Map<String, NType>> _records = null;
	
	
			/**
			 * <p>Creates an instance for a analytics results.</p>
			 * @param title Name of the results.
			 */
	public CReport(final String title) {
		_title = title;
		_records = new HashMap<String, Map<String, NType>>();
	}
	
			/**
			 * <p>Set the starting and ending date for the analytics report
			 * @param startDate
			 * @param endDate
			 */
	public void setPeriod(final String startDate, final String endDate) {
		_startDate = startDate;
		_endDate = endDate;
	}
	
	public void setNumRecords(final int numRecords) {
		_numRecords = numRecords;
	}
	
	public void add(final String recordName, 
					final String name, 
					final String value, 
					final String type) {
		
		if( _records.containsKey(recordName)) {
			Map<String, NType> record = _records.get(recordName);
			record.put(name, new NType(value, type));
		}
		
		else {
			Map<String, NType> record = new HashMap<String, NType>();
			record.put(name, new NType(value, type));
			_records.put(recordName, new HashMap<String, NType>());
		}
	}
	
	public String toString(){
		StringBuilder buf = new StringBuilder();
		Map<String, NType> record = null;
		NType type = null;
		
		buf.append("\nTitle       =");
		buf.append(_title);
		buf.append("\nNum Records =");
		buf.append(_numRecords);
		buf.append("\nStart date  =");
		buf.append(_startDate);
		buf.append("\nEnd date    =");
		buf.append(_endDate);
		
		for( String recordKey : _records.keySet()) {
			buf.append("\nRecord = ");
			buf.append(recordKey);
			record = _records.get(recordKey);
			if( record != null )  {
				for( String propName : record.keySet()) {
					type = record.get(propName);
					if( type != null ) {
						buf.append("\nName  =");
						buf.append(propName);
						buf.append("\nValue =");
						buf.append(type.getValue());
						buf.append("\nType  =");
						buf.append(type.getType());
					}
				}
			}
		}
		
		return buf.toString();
	}
}

// ------------------------  EOF -----------------------------------

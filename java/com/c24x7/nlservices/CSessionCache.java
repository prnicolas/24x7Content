// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.nlservices;

import java.util.concurrent.ConcurrentHashMap;
import com.c24x7.models.AModel;

public class CSessionCache extends ConcurrentHashMap<String, AModel> {
	/**
	 * 
	 */
	protected static final long serialVersionUID = 7508031199549344849L;
	protected static CSessionCache sessionCache = null;
	
	public static CSessionCache getInstance() {
		if( sessionCache == null ) {
			sessionCache = new  CSessionCache();
		}
		return sessionCache;
	}
	
	
	public AModel get(final String sessionKey) {
		CSessionCache session = getInstance(); 
		return session.get(sessionKey);
	}
	
	protected CSessionCache() {
		super();
	}
	
}

// ----------------------------  EOF ---------------------------------
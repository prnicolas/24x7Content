// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;

import java.util.concurrent.ConcurrentHashMap;

				/**
				 * <p>Singleton class that manage the cache of AModel objects.</p>
				 * @author Patrick Nicolas
				 * @date 08/25/2011
				 */
public class CModelManager extends ConcurrentHashMap<Long, AModel> {

	private static final long serialVersionUID = -1890218502762906580L;
	private static CModelManager _modelManager = new CModelManager();
	
	public final static CModelManager getInstance() {
		return _modelManager;
	}
	
	protected CModelManager() {
	}
}

// ------------------  EOF -----------------------------------

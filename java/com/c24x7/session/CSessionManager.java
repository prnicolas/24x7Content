package com.c24x7.session;

import java.util.concurrent.ConcurrentHashMap;

public class CSessionManager extends ConcurrentHashMap<Long, CSession> {
	protected static CSessionManager sessionManager = new CSessionManager();
	
	protected static final long serialVersionUID = 503297420253373598L;

	public static CSessionManager getInstance() {
		return sessionManager;
	}
	
	public void put(CSession session) {
		super.put(new Long(session.getId()), session);
	}
	
	protected CSessionManager() { }
}

// --------------------------  EOF -----------------------------------

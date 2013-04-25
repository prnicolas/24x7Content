package com.c24x7.session;

import com.c24x7.users.CUsersManager.CUser;



public final class CSession {
	protected long _id = -1L;
	protected long _userId = -1L;
	
	public CSession(CUser user) {
		_id = System.currentTimeMillis();
		_userId = user.getId();
	}
	
	public final long getUserId() {
		return _userId;
	}
	
	public final long getId() {
		return _id;
	}
}

// ------------------------  EOF --------------------------------

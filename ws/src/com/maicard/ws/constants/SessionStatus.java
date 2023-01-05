package com.maicard.ws.constants;

public enum SessionStatus {
	LOGGED_IN(1),
	
	READY_FOR_MESSAGE(2);
	
	
	public int id;
	
	private SessionStatus(int id){
		this.id = id;
	}
	
}

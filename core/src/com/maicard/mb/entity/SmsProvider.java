package com.maicard.mb.entity;

import java.io.Serializable;

public class SmsProvider implements Serializable {


	private static final long serialVersionUID = -2152020352989798718L;
	public long ownerId;
	public String username;
	public String password;
	public String prefixSign;
	
	public SmsProvider(){
		
	}

	public SmsProvider(String username, String password, String prefixSign, long ownerId){
		this.username = username;
		this.password = password;
		this.prefixSign = prefixSign;
		this.ownerId = ownerId;
		
	}

}

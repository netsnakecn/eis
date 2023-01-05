package com.maicard.tx.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maicard.core.entity.BaseEntity;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@NeedJmsDataSyncP2P
public class AddressBook extends BaseEntity {

	@Override
	public boolean isCacheable(){
		return true;
	}


	protected long uuid;	
	
	private String username;
	
	protected String country;
	
	protected String province;
	
	protected String city;
	
	protected String district;
	
	protected String address;
	
	protected String contact;
	
	protected String phone;
	
	protected String mobile;
	
	protected String postcode;
	

	private Date createTime;

	private Date lastUseTime;
	
	private int useCount;
	
	private String identify;



	@JsonIgnore
	protected int lockStatus;
	
	public AddressBook() {
	}


	




}

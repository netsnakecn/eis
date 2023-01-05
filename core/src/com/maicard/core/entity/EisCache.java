package com.maicard.core.entity;

import java.util.HashMap;

public class EisCache extends BaseEntity {
	private String name;
	private String cacheSize;
	private String cacheCount;

	public EisCache(){


	}
	public EisCache(String name){
		this.name=name;
	}

	public String getCacheCount() {
		return cacheCount;
	}

	public void setCacheCount(String cacheCount) {
		this.cacheCount = cacheCount;
	}



	public String getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(String i) {
		this.cacheSize = i;
	}


	protected HashMap<String,String> operate;

	public HashMap<String, String> getOperate() {
		return operate;
	}
	public void setOperate(HashMap<String, String> operate) {
		this.operate = operate;
	}


	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}

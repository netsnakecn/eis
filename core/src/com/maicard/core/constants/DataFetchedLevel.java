package com.maicard.core.constants;

public enum DataFetchedLevel {
	
	/**
	 * 未获取任何扩展和关联数据
	 */
	NONE(0),
	
	/**
	 * 已获取扩展数据还没有获取关联数据
	 */
	EXTRA_DATA(1),
	
	/**
	 * 已获取扩展和关联数据
	 */
	RELATE_DATA(2) ;
	
	public int id;
	
	private DataFetchedLevel(int id){
		this.id = id;
	}
 
}

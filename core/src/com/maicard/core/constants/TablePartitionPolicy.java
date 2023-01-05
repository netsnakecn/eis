package com.maicard.core.constants;

/**
 * 表分区规则
 *
 *
 * @author NetSnake
 * @date 2015年12月13日
 *
 */
public enum TablePartitionPolicy {
	operateLog(""),
	pay("MM"),
	pointExchangeLog("MM"),
	itemHistory("MM"),
	itemLog(""),
	userData("1"), 
	notifyLogLog("");
	private final String name;
	private TablePartitionPolicy( String name){
		this.name = name;
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString(){
		return name;
	}		
}

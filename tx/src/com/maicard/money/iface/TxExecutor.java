package com.maicard.money.iface;


import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;

public interface TxExecutor {

	//开始交易，处理资金冻结、事物分发等工作
	EisMessage begin(Object object) throws Exception;

	//查询交易结果尝试结束交易，并根据交易结果进行后续处理，如扣除或返还冻结资金
	EisMessage end(Object object) throws Exception;	
	
	EisMessage lock(CriteriaMap criteria);
	
	void unlock(Object... object);

	void onMessage(EisMessage eisMessage);




}

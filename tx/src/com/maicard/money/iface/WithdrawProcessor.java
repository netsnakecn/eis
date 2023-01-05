package com.maicard.money.iface;

import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.Withdraw;

//支付接口定义
public interface WithdrawProcessor {
	

	/**
	 * 开始一笔付款，必须返回一个EisMessage，不允许返回空
	 * @param withdraw
	 * @return
	 */
	EisMessage onWithdraw(Withdraw withdraw);//开始支付
	
	EisMessage onQuery(Withdraw withdraw);
	
	Withdraw onResult(String resultString);//返回对应的交易实例

	/**
	 * 返回一个提现处理器的简单说明
	 */
	String getDesc();

	
	


}

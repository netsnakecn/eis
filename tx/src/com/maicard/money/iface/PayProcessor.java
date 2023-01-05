package com.maicard.money.iface;

import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.Pay;

import javax.servlet.http.HttpServletRequest;

//支付接口定义
public interface PayProcessor {
	
	/*
	 * 如果是直接返回或需要跳转的支付方式，如网银，则返回跳转HTML代码
	 * 如果是需要较长处理时间的，如充值卡，则返回当时处理结果的JSON
	 */
	EisMessage onPay(Pay pay);//开始支付


	Pay onResult(String transactionId, String resultString, HttpServletRequest request);

	EisMessage onQuery(Pay pay);
	
	EisMessage onRefund(Pay pay);	//即时退款
	

	String getDesc();
	

	


}

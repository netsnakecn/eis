package com.maicard.core.service;

import java.util.Date;

import com.maicard.core.annotation.IgnoreDs;



@IgnoreDs
public interface GlobalOrderIdService {



	/**
	 * 根据订单号，从订单中的时间部分，返回对应的时间
	 * @param transactionId
	 * @return
	 */
	Date getDateByTransactionId(String transactionId);

	/**
	 * 生成一个指定类型的订单号
	 * @param transactionTypeId
	 * @return
	 */
	String generate(int transactionTypeId);

	/**
	 * 生成一个指定类型和订单时间的订单号
	 * @param transactionTypeId
	 * @param orderDate
	 * @return
	 */
	String generate(int transactionTypeId, Date orderDate);

	static	int idLength(){
		return 15;
	}

}

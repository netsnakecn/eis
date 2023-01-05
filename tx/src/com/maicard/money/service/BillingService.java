package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.money.entity.Billing;
 

public interface BillingService extends IService<Billing> {



	/**
	 * 根据指定的条件，处理支付订单并生成结算单<br>
	 * 把指定范围内的支付订单更新为对应的结算ID
	 */
	Billing billing(CriteriaMap billingCriteria);


}

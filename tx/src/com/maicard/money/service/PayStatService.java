package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.money.entity.PayStat;
 

public interface PayStatService extends IService<PayStat> {


	/**
	 * 请求计算毛利
	 */
	void calculateProfit();

	/**
	 * 执行指定时间段内的统计
	 * @param payStatCriteria
	 */
	void statistic(CriteriaMap payStatCriteria);

}

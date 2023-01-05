package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.WithdrawStat;
 

public interface WithdrawStatService {

	List<WithdrawStat> listOnPage(CriteriaMap withdrawStatCriteria);
	
	int count(CriteriaMap withdrawStatCriteria);

	List<WithdrawStat> list(CriteriaMap withdrawStatCriteria);

	/**
	 * 请求计算毛利
	 */
	void calculateProfit();

	/**
	 * 执行指定时间段内的统计
	 * @param withdrawStatCriteria
	 */
	void statistic(CriteriaMap withdrawStatCriteria);
}

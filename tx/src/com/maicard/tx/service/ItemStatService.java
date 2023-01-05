package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.tx.entity.ItemStat;
 

public interface ItemStatService extends IService<ItemStat> {


	/**
	 * 请求计算毛利
	 */
	void calculateProfit();

	/**
	 * 执行指定时间段内的统计
	 * @param itemStatCriteria
	 */
	void statistic(CriteriaMap itemStatCriteria);
}

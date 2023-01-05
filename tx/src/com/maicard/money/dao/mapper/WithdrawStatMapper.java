package com.maicard.money.dao.mapper;

import java.util.List;


import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.money.entity.WithdrawStat;
 

public interface WithdrawStatMapper extends IDao<WithdrawStat> {


	void calculateProfit();

	void statistic(CriteriaMap withdrawStatCriteria);
}

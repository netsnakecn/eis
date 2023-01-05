package com.maicard.money.dao.mapper;

import java.util.List;


import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.money.entity.PayStat;
 

public interface PayStatMapper extends IDao<PayStat> {

	
 

	void calculateProfit();

	void statistic(CriteriaMap payStatCriteria);

    PayStat sum(CriteriaMap statCriteria);
}

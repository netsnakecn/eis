package com.maicard.tx.dao.mapper;

import java.util.List;


import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.tx.entity.ItemStat;
 

public interface ItemStatMapper extends IDao<ItemStat> {


	void calculateProfit();

	void statistic(CriteriaMap itemStatCriteria);
}

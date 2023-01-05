package com.maicard.tx.dao.mapper;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.tx.entity.Order;

public interface OrderMapper extends IDao<Order> {
    Order sum(CriteriaMap cartCriteria);

    int updateBatch(CriteriaMap approveCriteria);
}

package com.maicard.money.dao.mapper;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.money.entity.MoneyFlow;

import java.util.Map;

/**
 * @author: iron
 * @description: 账户流水表
 * @date : created in  2018/1/3 16:45.
 */
public interface MoneyFlowMapper extends IDao<MoneyFlow> {

    /**
     * 更新账务流水 状态
     * @param v
     * @return
     */
    public int updateStatusById(MoneyFlow v);

    /**
     * 更新清算状态
     * @param map
     * @return
     */
    public int updateClearStatus(Map<String, Object> map);
    
	public long getDayRemainMoney(Map<String, Object> map);

    long sum(CriteriaMap sumCriteria);
}

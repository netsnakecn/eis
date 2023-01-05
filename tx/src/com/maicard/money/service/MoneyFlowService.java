package com.maicard.money.service;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.money.entity.MoneyFlow;

import java.util.Map;

/**
 * @author: iron
 * @description: 账户流水service
 * @date : created in  2018/1/3 16:48.
 */
public interface MoneyFlowService extends IService<MoneyFlow> {

    public int updateStatusById(MoneyFlow accountFlowEntity);

    public int updateClearStatus(Map<String,Object> map);



//	public long getDayRemainMoney(Map<String, Object> map);

    long sum(CriteriaMap sumCriteria);
}

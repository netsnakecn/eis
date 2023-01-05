package com.maicard.money.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.ds.DataSourceMapper;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.dao.mapper.MoneyFlowMapper;
import com.maicard.money.entity.MoneyAccount;
import com.maicard.money.entity.MoneyFlow;
import com.maicard.money.service.MoneyAccountService;
import com.maicard.money.service.MoneyFlowService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@DataSourceMapper(Constants.DB_MONEY)
public class MoneyFlowServiceImpl extends AbsGlobalSyncService<MoneyFlow,MoneyFlowMapper> implements MoneyFlowService {
 


    @Override
    public int updateStatusById(MoneyFlow accountFlowEntity) {
        return mapper.updateStatusById(accountFlowEntity);
    }

	 


    @Override
    public int updateClearStatus(Map<String, Object> map) {
        return mapper.updateClearStatus(map);
    }
/*
	@Override
	public long getDayRemainMoney(Map<String, Object> map) {
        long money = mapper.getDayRemainMoney(map);
        if(money <= 0) {
        	//按照商户余额
        	String accountNo = map.get("accountNo").toString();
        	MoneyAccount accountInfo = accountManageService.queryByAccountNo(accountNo);
        	if(accountInfo != null) {
        		money = accountInfo.getAvailableAmount();
        	}
        }
        return money;

	}*/

	@Override
	public List<MoneyFlow> list(CriteriaMap criteria) {
		List<MoneyFlow> accountFlowEntityList = mapper.list(criteria);
        if(accountFlowEntityList == null) {
        	return Collections.emptyList();
        }
        return accountFlowEntityList;
	}



    @Override
    public long sum(CriteriaMap sumCriteria) {
        return mapper.sum(sumCriteria);
    }
}

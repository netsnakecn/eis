package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.money.entity.Settlement;
 
public interface SettlementService extends GlobalSyncService<Settlement> {

	int insert(CriteriaMap settlementCriteria);

	int update(CriteriaMap settlementCriteria);


	int countrecentbilling(CriteriaMap settlementCriteria);

}

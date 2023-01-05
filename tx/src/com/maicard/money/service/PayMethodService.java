package com.maicard.money.service;

import java.util.List;
import java.util.Map;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.money.entity.PayMethod;

public interface PayMethodService extends GlobalSyncService<PayMethod> {


	Map<Long, PayMethod> list4IdKeyMap(CriteriaMap payMethodCriteria);

	void combineExtraData(PayMethod payMethod, String displayLevel, int columnSize);


}

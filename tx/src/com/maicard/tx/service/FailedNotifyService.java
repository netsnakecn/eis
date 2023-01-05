package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.tx.entity.FailedNotify;
 
public interface FailedNotifyService extends IService<FailedNotify> {
	
	int delete(String transactionId);
	int replace(FailedNotify failedNotify);

}

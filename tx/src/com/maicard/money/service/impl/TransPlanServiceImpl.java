package com.maicard.money.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.maicard.core.constants.DataName;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.money.entity.TransPlan;
import com.maicard.money.iface.TxExecutor;
import com.maicard.money.service.TransPlanService;
 
@Service
public class TransPlanServiceImpl implements TransPlanService {
	
	@Resource
	private ConfigService configService;
	
	@Resource
	private ApplicationContextService applicationContextService;
	
	
	String defaultTransactionExecutor = null;
	
	@PostConstruct
	public void init(){
		defaultTransactionExecutor = configService.getValue(DataName.defaultTransactionExecutor.toString(),0);
	}

	@Override
	public TransPlan select(int transPlanId) {
		TransPlan defaultTransPlan = new TransPlan();
		defaultTransPlan.setProcessClass(defaultTransactionExecutor);
		return defaultTransPlan;
	}

	@Override
	public int getTransactionTypeIdFromTransactionid(String resultString) {
		if(resultString == null || resultString.length() < 5){
			return 0;
		}
		return Integer.parseInt(resultString.substring(2,4));
	}

	@Override
	public TransPlan selectByProductId(int productId) {
		TransPlan defaultTransPlan = new TransPlan();
		defaultTransPlan.setProcessClass(defaultTransactionExecutor);
		return defaultTransPlan;
	}

	@Override
	public TxExecutor getTransactionExecutor(String objectType, long objectId) {
		return applicationContextService.getBeanGeneric(defaultTransactionExecutor);
	}


}

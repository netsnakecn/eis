package com.maicard.money.service;

import com.maicard.money.entity.TransPlan;
import com.maicard.money.iface.TxExecutor;

public interface TransPlanService {
	
	public TransPlan select(int transPlanId);
	
	public TransPlan selectByProductId(int productId);

	public int getTransactionTypeIdFromTransactionid(String resultString);

	public TxExecutor getTransactionExecutor(String objectType, long objectId);

}

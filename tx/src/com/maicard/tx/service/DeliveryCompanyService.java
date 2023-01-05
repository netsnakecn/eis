package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.GlobalSyncService;
import com.maicard.security.entity.User;
import com.maicard.tx.entity.DeliveryOrder;
 

public interface DeliveryCompanyService  {
	
	public List<User> list();

	public void checkInfo(DeliveryOrder deliveryOrder);

	long getBestDeliveryCompanyId(DeliveryOrder deliveryOrder);

}

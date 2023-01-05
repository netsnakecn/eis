package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.money.entity.Price;
import com.maicard.tx.entity.DeliveryOrder;
import com.maicard.tx.entity.DeliveryPrice;
 

public interface DeliveryPriceService extends GlobalSyncService<DeliveryPrice> {
	

	public Price calculatePrice(DeliveryOrder deliveryOrder);

	public int loadBatch(List<String> lines, long deliveryPartnerId, String identify, long ownerId);





}

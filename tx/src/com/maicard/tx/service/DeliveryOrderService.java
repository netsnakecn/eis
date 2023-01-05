package com.maicard.tx.service;

import java.util.List;
import java.util.Map;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.BaseEntity;
import com.maicard.security.entity.User;
import com.maicard.tx.entity.AddressBook;
import com.maicard.tx.entity.DeliveryOrder;
import com.maicard.tx.entity.Item;
import com.maicard.tx.entity.Order;


public interface DeliveryOrderService extends GlobalSyncService<DeliveryOrder> {
	

	/**
	 * 根据一组订单和快递地址计算费用
	 * 快递费用和快递减免信息放入feeMap中
	 * 并返回EisError或OperateResult

	 */
	DeliveryOrder generateDeliveryOrder(long addressBookId, Item[]items, String refOrderId, String transactionType,	String identify) throws Exception;



	String getFromArea(Item item);

	String getFromArea(BaseEntity targetObject);

	DeliveryOrder generateDeliveryOrder(Item item, AddressBook addressBook);


    DeliveryOrder trace(Order order);

	Map<String, String> getCompany();

}

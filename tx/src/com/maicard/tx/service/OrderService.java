package com.maicard.tx.service;

import java.util.HashMap;
import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.exception.EisException;
import com.maicard.mb.constants.SyncMode;
import com.maicard.security.entity.User;
import com.maicard.tx.entity.Item;
import com.maicard.tx.entity.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService extends GlobalSyncService<Order> {


	Item select(long uuid, String transactionId);

	void delete(long uuid, String transactionId) throws Exception;


	void clear(long uuid) throws Exception;

	HashMap<String, Item> map(CriteriaMap itemCriteria);


	

	int count(long uuid, int status);

	

	long createNewOrderId();

	/**
	 * 根据条件得到用户当前的购物车
	 * 如果没有就创建一个新的
	 * 
	 */
	Order getCurrentOrder(long uuid, String priceType, String orderType, String identify, long ownerId,boolean createNewOrder);


	Order add(Item item, boolean createNewOrder, String identify, int cartId) throws EisException, Exception;

	void update(Item item, float changeCount) throws Exception;


	int recycle(Order order);


	void finish(Order cart);


	public int managerApprove(CriteriaMap criteriaMap, Order managerOrder) ;

	//根据条件计算总额和总数量
    Order sum(CriteriaMap cartCriteria);


    @Transactional
    int beginDelivery(Order order, String companyCode, String outOrderId);

    int beginBatchDelivery(Order manageOrder);

	//@Async
   // void createAsync(Order order);

   // @Async
   // void updateAsync(Order order);

   // void createSync(Order order);

	//void updateSync(Order order);

    List<Item> listItem(Order order);


	int assignDriver(Order order, User driver);

	int unAssignDriver(Order order, User driver);
}

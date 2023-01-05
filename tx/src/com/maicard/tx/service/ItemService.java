package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.IService;

import com.maicard.base.CriteriaMap;
import com.maicard.tx.entity.Item;
 

public interface ItemService extends IService<Item> {


	Item select(String transactionId);


	Item fetchWithLock(CriteriaMap itemCriteria);
		


	int changeStatus(Item item);



	boolean recycle(Item item);

	int lock(Item item);


 

}

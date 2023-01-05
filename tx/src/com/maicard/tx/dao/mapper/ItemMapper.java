package com.maicard.tx.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.tx.entity.Item;
 
public interface ItemMapper extends IDao<Item> {


	List<Item> listAll(CriteriaMap itemCriteria) throws DataAccessException;
	
	Item fetchWithLock(CriteriaMap itemCriteria);



	int lockUpdateAndRelaseAdditinalFrozenMoney(Item item);

	int releaseItemWithFrozenMoney(Item item);

	int releaseItemWithFrozenMoney2(Item item);

	int plusItemMoneyWithFrozenMoney(Item item);

	int plusItemMoneyWithoutFrozenMoney(Item item);

	List<Item> listProcessTimeout(CriteriaMap itemCriteria);


	int changeStatus(Item item);


	List<Item> listFrozenDeadAccount(CriteriaMap itemCriteria);

	int totalFailItem(int productID);


	int delete(String transactionId);

	/**
	 * 将一个Item的currentStatus设置为afterLockStatus，保证在设置前它的currentStatus必须是beforeLockStatus
	 * 
	 */
	int lock(Item item);

	boolean exist(String transactionId);




}

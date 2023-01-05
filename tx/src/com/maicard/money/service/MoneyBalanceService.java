package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.MoneyBalance;


public interface MoneyBalanceService {

	int insert(MoneyBalance moneyBalance);

	int update(MoneyBalance moneyBalance);

	int delete(int moneyBalanceId);
		
	MoneyBalance select(int moneyBalanceId);
	
	List<MoneyBalance> list(CriteriaMap moneyBalanceCriteria);

	List<MoneyBalance> listOnPage(CriteriaMap moneyBalanceCriteria);

	int count(CriteriaMap moneyBalanceCriteria);
	
	
	
}

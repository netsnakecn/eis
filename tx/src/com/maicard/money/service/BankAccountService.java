package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.money.entity.BankAccount;


public interface BankAccountService extends IService<BankAccount> {


	int setDefaultAdd(BankAccount bankAccount);

	int createIfNotExist(BankAccount bankAccount);

}

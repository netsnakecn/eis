package com.maicard.money.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.BankAccount;


public interface BankAccountMapper extends IDao<BankAccount> {

	int setDefaultAdd(BankAccount bankAccount) throws DataAccessException;
}

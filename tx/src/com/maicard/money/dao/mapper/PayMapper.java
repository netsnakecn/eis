package com.maicard.money.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.Pay;

public interface PayMapper extends IDao<Pay> {


	int delete(String transactionId) throws DataAccessException;

	Pay select(String transactionId) throws DataAccessException;


	int countByPartner(CriteriaMap payCriteria) throws DataAccessException;
	

	List<Pay> lock(CriteriaMap payCriteria) throws DataAccessException;

	Pay lock(Pay pay) throws DataAccessException;
 


}

package com.maicard.money.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.Billing;
 

public interface BillingMapper extends IDao<Billing> {

	


	List<Billing> listRecentBilling(CriteriaMap billingCriteria) throws DataAccessException;
	
 	
	int countrecentbilling(CriteriaMap billingCriteria) throws DataAccessException;

	int create(CriteriaMap billingCriteria);


	Billing billing(Billing billing);

}
